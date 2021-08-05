package ch.mfactory.svalert.shared.kafka.producer

import cats.MonadError
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Sync}
import ch.mfactory.svalert.shared.config.SharedConfig.ProducerConfig
import fs2.kafka._
import io.circe.syntax._
import cats.implicits._
import ch.mfactory.svalert.shared.kafka.KafkaService
import io.circe.{Encoder, Json}

trait JsonProducerDsl[F[_]] {

  def produce[K : Encoder, V: Encoder](key: K, value: V): F[Unit]

}

object JsonProducerDsl {

  def apply[F[_]](implicit ev: JsonProducerDsl[F]): JsonProducerDsl[F] = ev

  def partitionCount[F[_]: Concurrent](config: ProducerConfig, cs: ContextShift[F]): F[Int] = {
    implicit val badImplicit1: ContextShift[F] = cs

    val resource = KafkaAdminClient.resource(
      AdminClientSettings[F]
        .withBootstrapServers(config.common.bootstrapServers)
        .withProperties(KafkaService.getProperties(config.common))
    )

    resource.use(_.describeTopics(List(config.common.topic))
      .flatMap (_
        .values.headOption.map(_.partitions().size())
        .map(_.pure[F])
        .getOrElse(
          new Exception(s"Could not get partition count for topic ${config.common.topic}!").raiseError[F, Int]
        )
      )
    )
  }


  def createProducer[
    F[_] : ConcurrentEffect
  ](config: ProducerConfig,
    contextShift: ContextShift[F]): Resource[F, KafkaProducer[F, Json, Json]] =
  {

    implicit lazy val circeSerializer: Serializer[F, Json] =
      Serializer.lift[F, Json]{ x: Json =>
        Sync[F].delay(x.noSpaces.getBytes("UTF-8"))
      }

    val producerSettings = ProducerSettings[F, Json, Json]
      .withBootstrapServers(config.common.bootstrapServers)
      .withProperties(KafkaService.getProperties(config.common))


    implicit lazy val badImplicit = contextShift

    KafkaProducer.resource(producerSettings)
  }

  private def getPartitionIndex(key: Json,  partitionCount: Int): Int =
    key.noSpaces.hashCode.abs % partitionCount

  def interpreter[
    F[_] : MonadError[*[_], Throwable]
  ](config: ProducerConfig,
    producer: KafkaProducer[F, Json, Json],
    partitionCount: Int
  ): JsonProducerDsl[F] = new JsonProducerDsl[F] {
    override def produce[K : Encoder, V: Encoder](key: K, value: V): F[Unit] = {

      val keyJ = key.asJson
      val valueJ = value.asJson

      val records = ProducerRecords.one(
        ProducerRecord(config.common.topic, keyJ, valueJ)
          .withPartition(getPartitionIndex(keyJ, partitionCount))
      )

      producer.produce(records)
    }.flatten.map(_.passthrough)
  }

}

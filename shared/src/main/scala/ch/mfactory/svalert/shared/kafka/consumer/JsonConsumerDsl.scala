package ch.mfactory.svalert.shared.kafka.consumer

import cats.ApplicativeError
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import ch.mfactory.svalert.shared.config.SharedConfig.ConsumerConfig
import fs2.kafka._
import io.circe.{Decoder, Json, parser}
import cats.implicits._
import ch.mfactory.svalert.shared.kafka.KafkaService
import org.apache.kafka.clients.consumer

import java.nio.charset.StandardCharsets
import scala.concurrent.duration.DurationInt

trait JsonConsumerDsl[F[_]] {
  def consumeF[K: Decoder, V: Decoder](callback: ((K, V)) => F[Unit]): F[Unit]
}

object JsonConsumerDsl {



  def apply[F[_]](implicit ev: JsonConsumerDsl[F]): JsonConsumerDsl[F] = ev

  def createConsumer[
    F[_] : ConcurrentEffect
  ](config: ConsumerConfig, cs: ContextShift[F], timer: Timer[F]): Resource[F, KafkaConsumer[F, Json, Json]] = {

    implicit lazy val circeSerializer: Deserializer[F, Json] =
      Deserializer.lift{ value: Array[Byte] =>
        parser
          .parse(new String(value, StandardCharsets.UTF_8))
          .fold(
            error => (new Exception(error.show)).raiseError[F, Json],
            _.pure[F]
          )
      }

    val consumerSettings =
      ConsumerSettings[F, Json, Json]
        .withBootstrapServers(config.common.bootstrapServers)
        .withProperties(KafkaService.getProperties(config.common))
        .withProperty(consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset)
        .withEnableAutoCommit(false)
        .withGroupId(config.groupId)

    implicit val badImplicit0: ContextShift[F] = cs
    implicit val badImplicit1: Timer[F] = timer

    KafkaConsumer.resource(consumerSettings)
  }

  private def fromJson[
    F[_]: ApplicativeError[*[_], Throwable],
    X: Decoder
  ](value: Json): F[X] =
    value
      .as[X]
      .fold(
        e => {
          println(e)
          (new Exception(e.show + value.noSpacesSortKeys)).raiseError[F, X]
        },
        x => x.pure[F]
      )

  def interpreter[
    F[_] : Concurrent
  ](config: ConsumerConfig,
    consumer: KafkaConsumer[F, Json, Json],
    timer: Timer[F]): JsonConsumerDsl[F] = new JsonConsumerDsl[F]
  {



    override def consumeF[
      K: Decoder, V: Decoder
    ](callback: ((K, V)) => F[Unit]): F[Unit] =
    {

      implicit lazy val badImplicit: Timer[F] = timer

      val stream: fs2.Stream[F, Unit] =
        consumer
          .stream
          .evalMap(_.bitraverse(fromJson[F, K], fromJson[F, V]))
          .evalMap{ cr =>
            callback(cr.record.key, cr.record.value)
              .map(_ => cr.offset)
          }

          .through{ in =>
            if (config.commitOffset)
              in.through(commitBatchWithin(500, 15.seconds))
            else in.void
          }

      consumer.subscribeTo(config.common.topic) >>
        stream
          .handleErrorWith{ t =>
             fs2.Stream
                .emit(println(t.getLocalizedMessage))
                .append(stream)

          }
          .compile
          .drain
    }

  }
}



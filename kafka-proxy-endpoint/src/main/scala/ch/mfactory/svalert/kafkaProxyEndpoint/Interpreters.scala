package ch.mfactory.svalert.kafkaProxyEndpoint

import cats.MonadError
import ch.mfactory.svalert.shared.config.SharedConfig.ProducerConfig
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import fs2.kafka.KafkaProducer
import io.circe.Json

trait Interpreters[F[_]] {

  implicit def producerDslInterpreter: JsonProducerDsl[F]

}

object Interpreters {

  def apply[
    F[_] : MonadError[*[_], Throwable]
  ](config: ProducerConfig,
    producer: KafkaProducer[F, Json, Json],
    partitionCount: Int): Interpreters[F] = new Interpreters[F]
  {
    override implicit def producerDslInterpreter: JsonProducerDsl[F] = {
      JsonProducerDsl.interpreter(config, producer, partitionCount)
    }
  }

}

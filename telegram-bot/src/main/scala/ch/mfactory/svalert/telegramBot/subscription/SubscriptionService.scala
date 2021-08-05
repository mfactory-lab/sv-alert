package ch.mfactory.svalert.telegramBot.subscription

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import ch.mfactory.svalert.shared.kafka.consumer.JsonConsumerDsl
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import ch.mfactory.svalert.telegramBot.config.Config
import ch.mfactory.svalert.telegramBot.model.Subscriptions
import fs2.kafka.{KafkaConsumer, KafkaProducer}
import io.circe.Json

object SubscriptionService {

  def buildSubscriptionDsl[
    F[_]: Concurrent
  ](config: Config,
    producer: KafkaProducer[F, Json, Json],
    partitionCount: Int,
    consumer: KafkaConsumer[F, Json, Json],
    timer: Timer[F]): SubscriptionDsl[F] =
  {
    implicit val producerDsl: JsonProducerDsl[F] =
      JsonProducerDsl.interpreter(config.subscriptionProducer, producer, partitionCount)
    implicit val consumerDsl: JsonConsumerDsl[F] =
      JsonConsumerDsl.interpreter(config.subscriptionConsumer, consumer, timer)

    SubscriptionDsl.interpreter()
  }


  def consumeSubscriptionEvents[
    F[_] : SubscriptionDsl
  ](subscriptionsR: Ref[F, Subscriptions]): F[Unit] = {

    SubscriptionDsl[F].consume{ case(chatId, subscription) =>
      subscription.fold(
        onSubscribe = event => subscriptionsR.update(_.add(chatId, event.subscription)),
        onUnsubscribe = event => subscriptionsR.update(_.remove(chatId, event.subscription))
      )
    }

  }

}

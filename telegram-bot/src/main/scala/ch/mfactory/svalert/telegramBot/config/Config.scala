package ch.mfactory.svalert.telegramBot.config

import ch.mfactory.svalert.shared.config.ConfigImplicits
import ch.mfactory.svalert.shared.config.SharedConfig._
import pureconfig.generic.semiauto.deriveReader

final case class Config(bot: TelegramBot,
                        subscriptionProducer: ProducerConfig,
                        subscriptionConsumer: ConsumerConfig,
                        requestsProducer: ProducerConfig,
                        responsesConsumer: ConsumerConfig,
                        notificationsConsumer: ConsumerConfig)

object Config extends ConfigImplicits {
  implicit lazy val mainConfigReader = deriveReader[Config]
}


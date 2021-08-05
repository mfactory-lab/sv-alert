package ch.mfactory.svalert.shared.config


import pureconfig.{CamelCase, ConfigFieldMapping}
import pureconfig.generic.ProductHint
import pureconfig.generic.semiauto.deriveReader

object SharedConfig {
  final case class BasicCredentials(user: String, password: String)

  final case class HttpConfig(host: String, port: Int, credentials: BasicCredentials)

  final case class KafkaCommonConfig(bootstrapServers: String, saslJaasConfig: String, topic: String)

  final case class TelegramBot(token: String)

  final case class ConsumerConfig(common: KafkaCommonConfig,
                                  groupId: String,
                                  autoOffsetReset: String,
                                  commitOffset: Boolean)

  final case class ProducerConfig(common: KafkaCommonConfig)

}

trait ConfigImplicits {
  import ch.mfactory.svalert.shared.config.SharedConfig._

  implicit def defaultHint[A]: ProductHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))
  implicit lazy val credentialsReader = deriveReader[BasicCredentials]
  implicit lazy val httpReader = deriveReader[HttpConfig]
  implicit lazy val kafkaReader = deriveReader[KafkaCommonConfig]
  implicit lazy val producerReader = deriveReader[ProducerConfig]
  implicit lazy val consumerReader = deriveReader[ConsumerConfig]
  implicit lazy val telegramBotReader = deriveReader[TelegramBot]
}

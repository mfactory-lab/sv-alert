package ch.mfactory.svalert.shared.kafka

import ch.mfactory.svalert.shared.config.SharedConfig.KafkaCommonConfig
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.common.config.SaslConfigs.{SASL_JAAS_CONFIG, SASL_MECHANISM}
import org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG

object KafkaService {

  def getProperties(config: KafkaCommonConfig): Map[String, String] =
    Map[String, String](
      SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG -> "https",
      SECURITY_PROTOCOL_CONFIG -> "SASL_SSL",
      SASL_MECHANISM -> "PLAIN",
      SASL_JAAS_CONFIG -> config.saslJaasConfig,
    )

}

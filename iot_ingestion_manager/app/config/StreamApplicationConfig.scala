/*
 * Copyright 2017 TEAM PER LA TRASFORMAZIONE DIGITALE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import it.gov.daf.common.config.Read

case class StreamApplicationConfig(catalogUrl: String,
                                   validator: String,
                                   kafkaConfig: KafkaConfig,
                                   kyloConfig: KyloConfig)

object StreamApplicationConfig {

  private val readKafka = KafkaConfig.reader

  private val readKylo  = KyloConfig.reader

  def reader = for {
    catalogUrl  <- Read.string { "daf.catalog_url"      }.!
    validator   <- Read.string { "daf.stream.validator" } default "none"
    kafkaConfig <- readKafka
    kyloConfig  <- readKylo
  } yield StreamApplicationConfig(
    catalogUrl  = catalogUrl,
    validator   = validator,
    kafkaConfig = kafkaConfig,
    kyloConfig  = kyloConfig
  )

}

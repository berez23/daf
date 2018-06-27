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

package daf.dataset.query.jdbc

import cats.effect.IO
import cats.syntax.traverse.toTraverseOps
import cats.syntax.monad.catsSyntaxMonad
import cats.instances.list.catsStdInstancesForList
import cats.instances.stream.catsStdInstancesForStream
import daf.dataset.query.Query
import doobie._
import doobie.implicits._
import org.slf4j.LoggerFactory

import scala.util.Try

class JdbcQueryService(transactor: Transactor[IO]) {

  private val logger = LoggerFactory.getLogger("it.gov.daf.QueryService")

  private def exec(fragment: Fragment) = fragment.execWith {
    HPS.executeQuery { JdbcQueryOps.result }
  }

  def exec(query: Query, table: String): Try[JdbcResult] = for {
    fragment <- Writers.sql(query, table).write
    _        <- Try { logger.debug(s"Executing query fragment: [$fragment]") }
    result   <- Try { exec(fragment).transact(transactor).unsafeRunSync }
  } yield result

}

object JdbcQueryOps {

  val header: ResultSetIO[Header] = FRS.getMetaData.map { metadata =>
    1 to metadata.getColumnCount map { metadata.getColumnName }
  }

  val genericRow: ResultSetIO[Row] = FRS.getMetaData.flatMap { metadata =>
    List.range(1, metadata.getColumnCount + 1).traverse[ResultSetIO, AnyRef] { FRS.getObject }
  }

  val genericStream: ResultSetIO[Stream[Row]] = genericRow.whileM[Stream] { HRS.next }

  val result: ResultSetIO[JdbcResult] = for {
    h      <- header
    stream <- genericStream
  } yield JdbcResult(h, stream)

}

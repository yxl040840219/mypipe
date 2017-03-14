package mypipe.mysql

import com.github.shyiko.mysql.binlog.event.{Event ⇒ MEvent, _}
import com.typesafe.config.Config
import mypipe.api.event.{Event}

object MySQLBinaryLogConsumer {
  def apply(config: Config) = new MySQLBinaryLogConsumer(config)
}

class MySQLBinaryLogConsumer(override val config: Config)
    extends AbstractMySQLBinaryLogConsumer
    with ConfigBasedConnectionSource
    with ConfigBasedErrorHandlingBehaviour[MEvent]
    with ConfigBasedEventSkippingBehaviour
    with CacheableTableMapBehaviour {

}
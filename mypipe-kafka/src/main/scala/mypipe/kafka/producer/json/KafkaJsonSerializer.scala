package mypipe.kafka.producer.json

import java.util
import java.util.UUID

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import mypipe.api.data.{Column, Row}
import mypipe.api.event.Mutation
import org.apache.kafka.common.serialization.Serializer

case class KafkaJson(timestamp: Long, database: String, tableId: Long, tableName: String, txid: UUID, mutationType: String,
                     current: Option[Map[String, Column]], prev: Option[Map[String, Column]])

/** Created by yxl on 17/3/8.
 */
class KafkaJsonSerializer extends Serializer[(Mutation, Either[Row, (Row, Row)])] {

  private val encoding = "UTF8"

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: (Mutation, Either[Row, (Row, Row)])): Array[Byte] = {
    val mutation = data._1
    val rowOrTupleRows = data._2

    val database = mutation.database
    val table = mutation.table
    val txid = mutation.txid
    val mutationType = Mutation.typeAsString(mutation)
    val timestamp = mutation.timestamp

    val kafkaJson = mutationType match {
      case Mutation.InsertString ⇒ KafkaJson(timestamp, database, table.id, table.name,
        txid, mutationType, Some(rowOrTupleRows.left.get.columns), None)
      case Mutation.UpdateString ⇒ KafkaJson(timestamp, database, table.id, table.name, txid, mutationType,
        Some(rowOrTupleRows.right.get._2.columns), Some(rowOrTupleRows.right.get._1.columns))
      case Mutation.DeleteString ⇒ KafkaJson(timestamp, database, table.id, table.name, txid, mutationType,
        Some(rowOrTupleRows.left.get.columns), None)
      case _ ⇒
    }

    val objectMapper = new ObjectMapper() with ScalaObjectMapper
    objectMapper.registerModule(DefaultScalaModule)
    val json = objectMapper.writeValueAsString(kafkaJson)
    json.getBytes(encoding)
  }

  override def close(): Unit = {}
}

package mypipe.kafka.producer.json

import com.typesafe.config.Config
import mypipe.api.event.{UpdateMutation, SingleValuedMutation, Mutation, AlterEvent}
import mypipe.kafka.KafkaUtil
import mypipe.kafka.producer.KafkaMutationAvroProducer.MessageType
import mypipe.api.producer.Producer
import mypipe.kafka.producer.KafkaProducer
import org.apache.avro.io.EncoderFactory
import org.apache.kafka.common.serialization.{Serializer}
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

/** Created by yxl on 17/3/8.
 */

object KafkaMutationJsonProducer {
  def apply(config: Config) = new KafkaMutationJsonProducer(config)
}

class KafkaMutationJsonProducer[T <: Serializer[MessageType]](config: Config, producerProperties: Map[AnyRef, AnyRef] = Map.empty)
    extends Producer(config = config) {

  protected val metadataBrokers = config.getString("metadata-brokers")

  protected val producer = new KafkaProducer(metadataBrokers, classOf[KafkaJsonSerializer], producerProperties)

  protected val logger = LoggerFactory.getLogger(getClass)

  protected val encoderFactory = EncoderFactory.get()

  def this(config: Config) = this(config, Map.empty)

  protected def getKafkaTopic(mutation: Mutation): String = KafkaUtil.genericTopic(mutation)

  override def getIncludeTableConfig: Option[List[String]] = {
    if (config.hasPath("include-tables")) {
      Option(config.getStringList("include-tables").asScala.toList)
    } else {
      None
    }
  }

  override def queue(input: Mutation): Boolean = {

    try {
      val tableName = input.table.name
      if (!skipEvent(tableName)) {
        logger.info(s"skip event with not contain table:$tableName")
        true
      } else {
        if (input.isInstanceOf[SingleValuedMutation]) {
          val mut = input.asInstanceOf[SingleValuedMutation]
          mut.rows foreach { row ⇒ producer.queue(getKafkaTopic(input), (mut, Left(row))) }
          true
        } else {
          val mut = input.asInstanceOf[UpdateMutation]
          mut.rows foreach { row ⇒ producer.queue(getKafkaTopic(input), (mut, Right(row._1, row._2))) }
          true
        }
      }
    } catch {
      case e: Exception ⇒
        logger.error(s"failed to queue: ${e.getMessage}\n${e.getStackTraceString}")
        false
    }

  }

  override def flush(): Boolean = {
    try {
      producer.flush
      true
    } catch {
      case e: Exception ⇒
        logger.error(s"Could not flush producer queue: ${e.getMessage} -> ${e.getStackTraceString}")
        false
    }
  }

  override def queueList(inputList: List[Mutation]): Boolean = {
    inputList.dropWhile(queue).isEmpty
  }

  override def handleAlter(event: AlterEvent): Boolean = {
    true
  }
}

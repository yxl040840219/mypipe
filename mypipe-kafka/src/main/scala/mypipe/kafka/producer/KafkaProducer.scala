package mypipe.kafka.producer

import java.util
import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Logger

import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer}
import org.apache.kafka.clients.producer.{KafkaProducer ⇒ KProducer, ProducerConfig, ProducerRecord}
import KafkaMutationAvroProducer.MessageType

import scala.collection.JavaConverters._

class KafkaProducer[T <: Serializer[MessageType]](metadataBrokers: String, serializerClass: Class[T], producerProperties: Map[AnyRef, AnyRef] = Map.empty) {

  type KeyType = Array[Byte]

  val log = Logger.getLogger(getClass.getName)

  val properties = new Properties()
  properties.put(ProducerConfig.ACKS_CONFIG, "1")
  properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, metadataBrokers)
  properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getName)
  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass.getName)
  properties.putAll(producerProperties.asJava)

  val producer = new KProducer[KeyType, MessageType](properties)

  val queue = new LinkedBlockingQueue[ProducerRecord[KeyType, MessageType]]()

  def queue(topic: String, message: MessageType) {
    queue.add(new ProducerRecord[KeyType, MessageType](topic, message))
  }

  def queue(topic: String, messageKey: Array[Byte], message: MessageType) {
    queue.add(new ProducerRecord[KeyType, MessageType](topic, messageKey, message))
  }

  def flush: Boolean = { // 发送数据到 kafka
    val s = new util.ArrayList[ProducerRecord[KeyType, MessageType]]
    queue.drainTo(s)
    log.info(s"send record to kafka: ${s.size()} records")
    val a = s.toArray[ProducerRecord[KeyType, MessageType]](Array[ProducerRecord[KeyType, MessageType]]())
    val futures = a.map(record ⇒ {
      val future = producer.send(record)
      future
    })
    futures.map { println }
    true
  }
}


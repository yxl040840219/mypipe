package mypipe.api.producer

import com.typesafe.config.Config
import mypipe.api.event.{AlterEvent, Mutation}

abstract class Producer(config: Config) extends ConfigBaseTableContainBehaviour {
  /** 添加 mutation
   *
   *  @param mutation
   *  @return
   */
  def queue(mutation: Mutation): Boolean

  /** 批量添加 mutation
   *
   *  @param mutation
   *  @return
   */
  def queueList(mutation: List[Mutation]): Boolean

  /** 清空 queue
   *
   *  @return
   */
  def flush(): Boolean

  /** 处理修改事件
   *
   *  @param event
   *  @return
   */
  def handleAlter(event: AlterEvent): Boolean

  /** 获取 producer 配置的包含 Table
   *  @return
   */
  override def getIncludeTableConfig: Option[List[String]] = None

}


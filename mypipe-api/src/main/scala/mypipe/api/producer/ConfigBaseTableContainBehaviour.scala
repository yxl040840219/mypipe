package mypipe.api.producer

import mypipe.util.Eval

/** Created by yxl on 17/3/9.
 */
trait ConfigBaseTableContainBehaviour {

  def getIncludeTableConfig: Option[List[String]]

  val containFn: (String) ⇒ Boolean = {
    if (getIncludeTableConfig.isDefined)
      getIncludeTableConfig.get.contains(_)
    else
      (_) ⇒ true // 默认包含所有
  }

  protected def skipEvent(table: String): Boolean = {
    val boolean = containFn(table)
    boolean
  }

}

package org.dagobuh.api

sealed trait DagobuhError

case class DagBuilderUnionError(msg: String) extends DagobuhError

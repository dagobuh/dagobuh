# Dagobuh
Graph DSL for defining streaming dataflows.

More detailed usage to come, but a simple example is outlined below

## Example Usage
```scala
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.dagobuh.api.graph.{Dag, DagBuilder}
import org.dagobuh.api.inputstream.ConvertToInputStream
import org.dagobuh.api.streamlets.{DataGenerator, ForEach, Streamlet, Transformer}

import scala.language.higherKinds

case class Person(name: String, age: Int)

class AgeLimit[F[_]](limit: Int) extends Transformer[F, Person, Person] {
  override def run(in: InputStream[F, Person]): InputStream[F, Person] = {
    in.filter(_.age < limit)
  }
}

object AgeLimit {
  def apply[F[_]](limit: Int): AgeLimit[F] = new AgeLimit(limit)
}

object Main extends App {
  val people = Person("Elizabeth", 15) ::
    Person("Mary", 24) ::
    Person("Jane", 21) :: Nil

  /***
    * Creates the following DAG:
    *
    *
    *                +--filterOne--+
    * DataGenerator--|             +--printerOne
    *                +--filterTwo--+
    * @param data input data stream
    * @tparam F stream type for backend
    * @return list of dags
    */
  def pipeline[F[_]: ConvertToInputStream](data: F[Person]): List[Dag] = {
    val datagen = DataGenerator[F, Person](data)
    val filterOne = AgeLimit[F](22)
    val filterTwo = AgeLimit[F](16)
    val printerOne = ForEach[F, Person](x => println("PrinterOne: " + x))

    val in = DagBuilder(datagen)

    val part = in ~> filterOne
    val part2 = in ~> filterTwo
    part ~> printerOne
    part2 ~> printerOne
    in.build()
  }

  {
    import org.dagobuh.impl.list.ListImplicits._

    pipeline(people)
        .foreach(_.run())
  }
  {
    import org.dagobuh.impl.flink.FlinkImplicits._

    val env = StreamExecutionEnvironment.createLocalEnvironment()
    val dataStream = env.fromCollection(people)

    pipeline(dataStream)
      .foreach(_.run())

    env.execute()
  }
}
```

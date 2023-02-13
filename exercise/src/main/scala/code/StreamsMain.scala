package code

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.alpakka.slick.scaladsl.SlickSession
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.alpakka.slick.scaladsl._

object StreamsMain {
  val config = DatabaseConfig.forConfig[JdbcProfile]("slick.docker_employees")

  implicit val system = ActorSystem.create()
  implicit val session = SlickSession.forConfig(config)

  val streamsStore = new StreamsExample(config, new EmployeeStore(config))

  val graph: RunnableGraph[Future[Done]] =
    streamsStore
      .employeeStream
      .flatMapConcat { emp =>
        streamsStore.salaryStream(emp.employeeNumber)
          .fold(0)(_ + _.amount)
          .map(amt => emp.lastName + " " + amt)
      }
      .toMat(Sink.foreach(println))(Keep.right)

  def main(args: Array[String]): Unit = {
    val result: Nothing =
      Await.result(graph.run(), 60.seconds)

    println(result)
  }
}

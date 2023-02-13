package code

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main {
  val config = DatabaseConfig.forConfig[JdbcProfile]("slick.docker_employees")
  val store = new EmployeeStore(config)

  def slickExample: Future[Option[Long]] =
    config.db.run(store.employeeBirthdayTimestamp(EmployeeNumber(500000)))

  def main(args: Array[String]): Unit = {
    val result: Option[Long] =
      Await.result(slickExample, 60.seconds)

    println(result)
  }
}

package code

import slick.jdbc.MySQLProfile.api._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  val database = Database.forConfig("database.docker_employees")

  val isEverythingSetUpOk: DBIO[String] =
    sql"""select count(*) from employees;""".as[Int].head
      .map(num => s"Yay! There are $num employees in the database!")

  def main(args: Array[String]): Unit = {
    println(Await.result(database.run(isEverythingSetUpOk), 2.seconds))
  }
}

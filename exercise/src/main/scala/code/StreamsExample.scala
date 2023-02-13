package code

import akka.{Done, NotUsed}
import akka.stream.alpakka.slick.scaladsl._
import akka.stream.scaladsl._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class StreamsExample(
  config: DatabaseConfig[JdbcProfile],
  store: EmployeeStore
)(implicit session: SlickSession) {
  import store._
  import config.profile.api._

  def employeeStream: Source[Employee, NotUsed] =
    Slick.source(EmployeeTable.result)

  def salaryStream(employeeNumber: EmployeeNumber): Source[Salary, NotUsed] =
    Slick.source(
      SalaryTable
        .filter(_.employeeNumber === employeeNumber)
        .result
    ).log("salary")

  def employeeSink: Sink[Employee, Future[Done]] =
    Slick.sink(emp => EmployeeTable += emp)
}

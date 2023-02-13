package code

import enumeratum._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.sql.FixedSqlAction

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class EmployeeStore(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  import config.profile.api._

  case class Email(address: String)

  implicit def emailColumnType: ColumnType[Email] =
    MappedColumnType.base(_.address, Email.apply)

  implicit def enumeratumColumnType[A <: EnumEntry: ClassTag](implicit e: Enum[A]): ColumnType[A] =
    MappedColumnType.base(_.entryName, e.withNameInsensitive)

  class EmployeeTable(tag: Tag) extends Table[Employee](tag, "employees") {
    val birthDate = column[LocalDate]("birth_date")
    val firstName = column[String]("first_name")
    val lastName = column[String]("last_name")
    val gender = column[Gender]("gender")
    val hireDate = column[LocalDate]("hire_date")
    val employeeNumber = column[EmployeeNumber]("emp_no", O.PrimaryKey, O.AutoInc)

    val * = (
      birthDate,
      firstName,
      lastName,
      gender,
      hireDate,
      employeeNumber,
    ).mapTo[Employee]
  }

  val EmployeeTable = TableQuery[EmployeeTable]

  class SalaryTable(tag: Tag) extends Table[Salary](tag, "salaries") {
    val employeeNumber = column[EmployeeNumber]("emp_no")
    val amount = column[Int]("salary")
    val fromDate = column[LocalDate]("from_date")
    val toDate = column[LocalDate]("to_date")

    val pkey = primaryKey("salaries_pkey", (employeeNumber, fromDate))
    val employeeFKey = foreignKey("salaries_employees_fkey", employeeNumber, EmployeeTable)(_.employeeNumber)

    val * = (
      employeeNumber,
      amount,
      fromDate,
      toDate,
    ).mapTo[Salary]
  }

  val SalaryTable = TableQuery[SalaryTable]

  def findEmployeesByLastName(name: String): DBIO[Vector[Employee]] =
    EmployeeTable
      .filter((emp: EmployeeTable) => (emp.lastName > name): Rep[Boolean])
      .to[Vector]
      .result

  def findEmployeesBornIn(year: Int): DBIO[Vector[Employee]] =
    EmployeeTable
      .filter { table =>
        table.birthDate >= LocalDate.of(year, 1, 1) &&
        table.birthDate < LocalDate.of(year + 1, 1, 1)
      }
      .to[Vector]
      .result

  def listEmployeesByName(str: String, offset: Int, limit: Int): DBIO[Vector[Employee]] =
    EmployeeTable
      .filter { table =>
        val pattern = "%" + str.toLowerCase.replaceAll("([%_])", "\\\\$1") + "%"
        table.firstName.toLowerCase.like(pattern) ||
        table.lastName.toLowerCase.like(pattern)
      }
      .drop(offset)
      .take(limit)
      .to[Vector]
      .result

  def countEmployeesByName(str: String): DBIO[Int] =
    EmployeeTable
      .filter { table =>
        val pattern = "%" + str.toLowerCase.replaceAll("([%_])", "\\\\$1") + "%"
        table.firstName.toLowerCase.like(pattern) ||
        table.lastName.toLowerCase.like(pattern)
      }
      .length
      .result

  def allEmployeeNumbers: DBIO[Vector[EmployeeNumber]] =
    EmployeeTable
      .map(table => table.employeeNumber)
      .to[Vector]
      .result

  def allEmployeeNames: DBIO[Vector[(String, String)]] =
    EmployeeTable
      .sortBy(table => (table.lastName, table.firstName))
      .map(table => (table.firstName, table.lastName))
      .to[Vector]
      .result

  def findEmployeesBornIn(year: Int, asc: Boolean = true): DBIO[Vector[Employee]] =
    EmployeeTable
      .filter { table =>
        table.birthDate >= LocalDate.of(year, 1, 1) &&
        table.birthDate < LocalDate.of(year + 1, 1, 1)
      }
      .sortBy(table => if (asc) table.birthDate.asc else table.birthDate.desc)
      .to[Vector]
      .result

  def allEmployeeNamesPaged(pageNum: Int, pageSize: Int): DBIO[Vector[(String, String)]] =
    EmployeeTable
      .sortBy(table => (table.lastName, table.firstName))
      .map(table => (table.firstName, table.lastName))
      .drop(pageNum * pageSize)
      .take(pageSize)
      .to[Vector]
      .result

  def findFirstNamesForLastName(lastName: String): DBIO[Vector[String]] =
    EmployeeTable
      .filter(table => table.lastName.toLowerCase === lastName.toLowerCase)
      .map(table => table.firstName)
      .to[Vector]
      .result

  def findEmployee(num: EmployeeNumber): DBIO[Option[Employee]] =
    EmployeeTable
      .filter(table => table.employeeNumber === num)
      .take(1)
      .result
      .headOption

  def hireGarfieldTheCat: DBIO[EmployeeNumber] =
    EmployeeTable.returning(EmployeeTable.map(_.employeeNumber)) += Employee(
      LocalDate.of(1975, 1, 1),
      "Garfield",
      "Arbuckle",
      Gender.Male,
      LocalDate.now,
    )

  def monadicJoin: Query[(EmployeeTable, SalaryTable), (Employee, Salary), Seq] = {
    val allTheTables: Query[(EmployeeTable, SalaryTable), (Employee, Salary), Seq] =
      for {
        x <- EmployeeTable
        y <- SalaryTable if x.employeeNumber === y.employeeNumber
      } yield (x, y)

    allTheTables.sortBy { case (emp, sal) => emp.lastName }
  }

  def applicativeJoin: Query[(EmployeeTable, SalaryTable), (Employee, Salary), Seq] = {
    val allTheTables: Query[(EmployeeTable, SalaryTable), (Employee, Salary), Seq] =
      EmployeeTable
        .join(SalaryTable)
        .on((emp, sal) => emp.employeeNumber === sal.employeeNumber)

    allTheTables.sortBy { case (emp, sal) => emp.lastName }
  }

  def sumOfAllSalaryRecords: FixedSqlAction[Option[Long], NoStream, Effect.Read] =
    SalaryTable
      .map(_.amount.asColumnOf[Long])
      .sum
      .result

  def totalPayrollOnDate(date: LocalDate): FixedSqlAction[Option[Long], NoStream, Effect.Read] =
    SalaryTable
      .filter(sal => sal.fromDate <= date && sal.toDate > date)
      .map(_.amount.asColumnOf[Long])
      .sum
      .result

  def maxSalaryByEmployeeNumber: Query[(Rep[EmployeeNumber], Rep[Option[Int]]), (EmployeeNumber, Option[Int]), Seq] =
    SalaryTable
      .groupBy(sal => sal.employeeNumber)
      .map { case (empNo, sal) => (empNo, sal.map(_.amount).max) }

  def salaryIncreaseByEmployeeName: Query[(Rep[String], Rep[Option[Int]]), (String, Option[Int]), Seq] = {
    val joined: Query[(EmployeeTable, SalaryTable), (Employee, Salary), Seq] =
      EmployeeTable
        .join(SalaryTable)
        .on((emp, sal) => emp.employeeNumber === sal.employeeNumber)

    joined
      .groupBy { case (emp, sal) => (emp.firstName, emp.lastName) }
      .map { case ((firstName, lastName), selectedRows) =>
        val selectedSalaries = selectedRows.map { case (emp, sal) => sal.amount }
        val maxSalary: Rep[Option[Int]] = selectedSalaries.max
        val minSalary: Rep[Option[Int]] = selectedSalaries.min
        val diffSalary: Rep[Option[Int]] = maxSalary - minSalary
        (firstName ++ " " ++ lastName, diffSalary)
      }
  }

  def employeeBirthday(employeeNumber: EmployeeNumber, age: Int): DBIOAction[Option[LocalDate], NoStream, Effect.Read] =
    // There are many uses of the name `map` below:
    EmployeeTable
      .filter(emp => emp.employeeNumber === employeeNumber)
      // Here we're mapping over a query.
      // The semantics are selecting columns in the SQL:
      .map(emp => emp.birthDate)
      .result
      .headOption
      // Here we're mapping over a DBIO.
      // The semantics are transforming the entire result dataset into a new result:
      .map { optBirthDate =>
        optBirthDate
          // Here we're mapping over an Option (the zero-or-one rows in our dataset).
          // The semantics are transforming an optional value:
          .map(_.plusYears(age))
      }

  def allEmployeeBirthdays(employeeNumbers: Set[EmployeeNumber], age: Int): DBIO[Map[EmployeeNumber, LocalDate]] =
    EmployeeTable
      .filter(emp => emp.employeeNumber.inSet(employeeNumbers))
      .map(emp => (emp.employeeNumber, emp.birthDate))
      .to[List]
      .result
      .map { results =>
        results
          .map { case (empNo, birthDate) => (empNo, birthDate.plusYears(age)) }
          .toMap
      }

  def capitaliseName(employeeNumber: EmployeeNumber): DBIO[Int] = {
    val query: Query[(Rep[String], Rep[String]), (String, String), Seq] =
      EmployeeTable
        .filter(emp => emp.employeeNumber === employeeNumber)
        .map(emp => (emp.firstName, emp.lastName))

    val action = for {
      option <- query.result.headOption
      _ = println("READ IN " + option)
      _ <- DBIO.failed(new Exception("ARGH!"))
      result <- option match {
                  case Some((firstName, lastName)) =>
                    query.update((firstName.toLowerCase, lastName.toLowerCase))

                  case None =>
                    DBIO.successful(0)
                }
    } yield result

    action.transactionally
  }

  def capitaliseNames: DBIO[Seq[Int]] = {
    val query: Query[(Rep[String], Rep[String]), (String, String), Seq] =
      EmployeeTable
        .map(emp => (emp.firstName, emp.lastName))

    val action = query.take(10).result.flatMap { employees: Seq[(String, String)] =>
      val updates: Seq[DBIO[Int]] =
        employees.map {
          case (firstName, lastName) =>
            query.update((firstName.toLowerCase, lastName.toLowerCase))
        }

      val action: DBIO[Seq[Int]] =
        DBIO.sequence(updates)

      action
    }

    action.andThen(DBIO.failed(new Exception("OH NO!"))).transactionally
  }

  def pagedEmployeesByName(pattern: String, offset: Int, limit: Int): DBIO[(Seq[Employee], Int)] =
    for {
      employees <- listEmployeesByName(pattern, offset, limit)
      total     <- countEmployeesByName(pattern)
    } yield (employees, total)

  // Scala mirror for the MySQL function `unix_timestamp(date) -> int`:
  val unixTimestamp: Rep[LocalDate] => Rep[Long] =
    SimpleFunction.unary[LocalDate, Long]("unix_timestamp")

  def employeeBirthdayTimestamp(employeeNumber: EmployeeNumber): DBIO[Option[Long]] =
    EmployeeTable
      .filter(_.employeeNumber === employeeNumber)
      .map(emp => unixTimestamp(emp.birthDate))
      .result
      .headOption
}

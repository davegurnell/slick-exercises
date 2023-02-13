package code

import java.time.LocalDate

case class Employee(
  birthDate: LocalDate,
  firstName: String,
  lastName: String,
  gender: Char,
  hireDate: LocalDate,
  employeeNumber: Int = -1,
)

package code

import java.time.LocalDate

case class Employee(
  birthDate: LocalDate,
  firstName: String,
  lastName: String,
  gender: Gender,
  hireDate: LocalDate,
  employeeNumber: EmployeeNumber = EmployeeNumber(-1),
)

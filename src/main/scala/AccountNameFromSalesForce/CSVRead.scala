package AccountNameFromSalesForce

import java.io.File

import AccountNameFromSalesForce.model._

import scala.annotation.tailrec
import scala.io.Source

abstract class CSVRead {
  case class CustomException(ex: String) extends Exception(ex)

  def fileExists(filepath: String): Boolean={
    if (new File(filepath).exists() && new File(filepath).isFile) true
    else throw CustomException(s"File $filepath doesn't exist.")
  }

  def isFileEmpty(filepath: String): Boolean = {
    val sourcefile = Source.fromFile(filepath)
    if (!sourcefile.getLines().hasNext) throw CustomException(s"File $filepath is empty.")
    else if (sourcefile.getLines().hasNext) {sourcefile.close; false}
    else throw CustomException(s"File $filepath doesn't have any valid data.")
  }

  def headerExists: Boolean

  def getHeaderFromFile(filepath: String): Option[String]

  def getColumnNumberWithCases(filepath: String): Int = {
    val sourcefile = io.Source.fromFile(filepath)
    if (headerExists) {
      sourcefile.getLines().take(1).next
    }
    val firstline = sourcefile.getLines().take(1).next.replace("\"", "").split(",")
    sourcefile.close
    val index=firstline
      .toList
      .indexWhere(_ matches "(C|c)ase.*")

    if (index == -1) throw CustomException("There is no column name with cases.")
    else index
  }

  def getListOfCasesFromFile(filepath: String, column: Int): List[VeeamCase] = {
    val sourcefile = io.Source.fromFile(filepath)
    if (headerExists) {
      sourcefile.getLines().take(1).next
    }
    val listOfCasesInColumn = sourcefile.getLines()
      .map(_.split(",")(column)
        .replace("\"", ""))
      .toList

    @tailrec
    def checkCaseNumberFormat(in: List[String], acc: List[Option[VeeamCase]]): List[Option[VeeamCase]] = {
      in match {
        case Nil => acc
        case head :: tail => checkCaseNumberFormat(tail, validateCaseFormat(head) :: acc)
      }
    }

    sourcefile.close
    checkCaseNumberFormat(listOfCasesInColumn, List()).flatten
  }
}

case object CSVwithoutHeader extends CSVRead{
  def headerExists: Boolean = false

  def getHeaderFromFile(filepath: String): Option[String] = None
}

case object CSVwithHeader extends CSVRead{
  def headerExists: Boolean = true

  def getHeaderFromFile(filepath: String): Option[String] = {
    val sourcefile = io.Source.fromFile(filepath)
    Some(sourcefile.getLines().take(1).next)
  }
}
package AccountNameFromSalesForce

import java.io.PrintWriter
import java.util.Calendar

case object HTMLWrite {
  def isListOfAccountsNotEmpty(lisOfAccounts: Option[List[String]]): Boolean = {
    lisOfAccounts match {
      case Some(listOfAccounts) => if (listOfAccounts.nonEmpty) true else false
      case None => false
    }
  }

  def writeToHTMLFile(lisOfAccounts: Option[List[String]], filename: Option[String]): Unit = {
    if (isListOfAccountsNotEmpty(lisOfAccounts)) {
      val listWithURL=lisOfAccounts.toList.flatten.map { el => el.replace("\"", "")}
        .map{ el => (el splitAt (el lastIndexOf ','))._1+" <a href=\"https://na62.salesforce.com/"+(el splitAt (el lastIndexOf ','))._2.drop(1)+"\">Link to SalesForce</a>"}

      val combinedList = listWithURL.drop(1)
        .map {
          el => s"<tr><td>$el</td></tr>"
        }
        .mkString.replace(",", " ")

      val preparedOutput = s"<!DOCTYPE HTML><html><head><title>Case Number, Account Name, Account.Id</title></head><body><table>$combinedList</table></body></html>"

      val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss")
      val now = dateFormat.format(Calendar.getInstance.getTime)

      val file = filename match {
        case None => s"Output$now.html"
        case Some(name) => s"$name$now.html"
      }

      new PrintWriter(file) {
        write(preparedOutput)
        close()
      }
    }
    else {
      println("List of accounts was not valid for file output.")
    }
  }

}

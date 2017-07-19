package AccountNameFromSalesForce

import AccountNameFromSalesForce.model._
import com.sforce.async.{BatchInfo, BulkConnection, JobInfo}
import com.sforce.ws.ConnectorConfig

import scala.util.Try

object EntryPoint{
  def main(args: Array[String]): Unit = {

    if (args.length == 3) {
      //From source .csv file we get header, column number with cases and list of cases
      val filepath = args(0)
      val userName = args(1)
      val passwordSecurityToken = args(2)

      CSVwithoutHeader.fileExists(filepath)
      CSVwithoutHeader.isFileEmpty(filepath)
      CSVwithoutHeader.headerExists
      val header: Option[String] = CSVwithoutHeader.getHeaderFromFile(filepath)
      val column: Int = CSVwithoutHeader.getColumnNumberWithCases(filepath)
      val listOfCases: List[VeeamCase] = CSVwithoutHeader.getListOfCasesFromFile(filepath, column)

      //Login to SalesForce: create partner connection and bulk connection
      val partner: ConnectorConfig = SFConnect.getPartnerConnection(userName, passwordSecurityToken)

      if (SFConnect.tryPartnerConnection(partner).isSuccess) {

        val partnerModified: ConnectorConfig = SFConnect.modifyPartnerConnection(partner)
        val bulk: BulkConnection = SFConnect.getBulkConnection(partnerModified)

        //Prepare and create job
        val preparedJob: JobInfo = SFQuery.prepareJob(bulk)
        val triedJob: Try[JobInfo] = SFQuery.tryJobCreation(bulk, preparedJob)

        if (triedJob.isSuccess) {

          val job: JobInfo = SFQuery.createJob(bulk, triedJob)
          //Check that number of cases is > 1 and < 9999 and build query
          val query: String = SFQuery.queryString(listOfCases)
          //Create batch, get list of accounts from SalesForce and close the job
          val triedBatch: Try[BatchInfo] = SFQuery.tryBatchCreation(bulk, job, query)

          if (triedBatch.isSuccess) {

            val batch: Option[BatchInfo] = SFQuery.createBatch(bulk, job, triedBatch)
            val listOfAccounts: Option[List[String]] = SFQuery.getQueryResults(bulk, job, batch)
            SFQuery.closeJob(job, bulk)

            //Check that list of accounts is not empty and write list of cases and list of accounts into file
            HTMLWrite.isListOfAccountsNotEmpty(listOfAccounts)
            HTMLWrite.writeToHTMLFile(listOfAccounts, header)
          }
          else println("Failed to create batch.")
        }
        else println("The job was not created successfully.")
      }
      else println("Unfortunately, connect to SalesForce failed.")
    }

    else {
      println(s"\nWrong parameters! Please specify\n\n" +
        "- path to .csv file\n" +
        "- username\n" +
        "- passwordSecurityToken (no spaces)\n\n" +
        "Examples:\n\n" +
        """[root@localhost]$ java -jar "home/user/Downloads/AccountNameFromSF.jar" "home/user/Downloads/cases.csv" "username@domain.com" 'PasswordSecurityToken' """ +
        "\n\n" +
        """PS C:\Users\Administrator> java -jar "C:\temp\AccountNameFromSF.jar" "D:\cases.csv" "username@domain.com" "PasswordSecurityToken" """ +
        "\n")
    }
  }

}

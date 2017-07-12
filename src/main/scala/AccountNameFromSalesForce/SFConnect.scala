package AccountNameFromSalesForce

import scala.util.{Failure, Success, Try}
import com.sforce.async.BulkConnection
import com.sforce.soap.partner.PartnerConnection
import com.sforce.ws.ConnectorConfig

object SFConnect {
  def getPartnerConnection(userName: String, password: String): ConnectorConfig = {
    val partnerConfig = new ConnectorConfig
    partnerConfig.setUsername(userName)
    partnerConfig.setPassword(password)
    partnerConfig.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/40.0.0")
    // Creating the connection automatically handles login and stores the session in partnerConfig
    partnerConfig
  }

  def tryPartnerConnection(partnerConfig: ConnectorConfig): Try[PartnerConnection] = {
    Try(new PartnerConnection(partnerConfig)) match {
      case Success(v) => Success(v)
      case Failure(ex) => println("Invalid username, password, security token; or user locked out.")
        Failure(ex)
    }
  }

  def modifyPartnerConnection(partnerConfig: ConnectorConfig): ConnectorConfig={
    val config = new ConnectorConfig
    config.setSessionId(partnerConfig.getSessionId)
    val soapEndpoint = partnerConfig.getServiceEndpoint
    val apiVersion = "37.0"
    val restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion
    config.setRestEndpoint(restEndpoint)
    // This should only be false when doing debugging.
    config.setCompression(true)
    // Set this to true to see HTTP requests and responses on stdout
    config.setTraceMessage(false)
    config
  }

  def getBulkConnection(config: ConnectorConfig): BulkConnection={
    new BulkConnection(config)
  }

}

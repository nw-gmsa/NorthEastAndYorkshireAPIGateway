package uk.nhs.nwgenomics.ney.providers

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException
import org.apache.commons.io.IOUtils
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Resource
import org.slf4j.LoggerFactory
import uk.nhs.nwgenomics.ney.NEYRESTfulServer
import uk.nhs.nwgenomics.ney.model.ResponseObject

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class ISCReader(var ctx: FhirContext) {

    @Throws(Exception::class)
    fun readFromUrl(path: String, queryParams: String?): Resource? {
        val responseObject = ResponseObject()
        val url: String = "https://hapi.fhir.org/baseR4"
        var myUrl: URL? = null
        myUrl = if (queryParams != null) {
            URL("$url$path?$queryParams")
        } else {
            URL(url + path)
        }
        val conn = myUrl.openConnection() as HttpURLConnection

       // conn.setRequestProperty("apikey", messageProperties.getNHSEnglandAPIKey())
        conn.setRequestProperty("X-Request-ID", UUID.randomUUID().toString())
        conn.setRequestMethod("GET")
        var response : Resource? = null
        var retry = 2
        while (retry > 0) {
            try {
                conn.connect()
                val `is` = InputStreamReader(conn.inputStream)
                try {
                    val rd = BufferedReader(`is`)
                    responseObject.responseCode = 200
                    val resource = ctx!!.newJsonParser().parseResource(IOUtils.toString(rd)) as Resource

                    response = resource
                    retry = 0
                } finally {
                    `is`.close()
                }
            } catch (ex: FileNotFoundException) {
                retry--
                null
            } catch (ex: Exception) {
                log.warn(ex.message)
                retry--
                if (ex.message != null) {
                    if (ex.message!!.contains("401") || ex.message!!.contains("403")) {

                       // conn.setRequestProperty("apikey", messageProperties.getNHSEnglandAPIKey())
                        conn.disconnect()
                    }
                } else {
                    retry=0
                }
                if (retry == 0) {
                    throw UnprocessableEntityException(getErrorStreamMessage(conn, ex))
                }
            }
        }
        return response
    }
    private fun getErrorStreamMessage(conn: HttpURLConnection, ex: Exception) : String? {
        try {
            if (conn.errorStream == null) {
                if (ex.message == null) return "Unknown Error"
                return ex.message
            }
            val `is` = InputStreamReader(conn.errorStream)
            try {
                val rd = BufferedReader(`is`)
                val resource: Resource = ctx.newJsonParser().parseResource(IOUtils.toString(rd)) as Resource
                if (resource != null && resource is OperationOutcome) {
                    return resource.issueFirstRep.diagnostics
                }
            } catch (exOther: Exception) {
                throw ex
            } finally {
                `is`.close()
            }
        } catch (ex: Exception) {
            log.info(ex.message)
        }
        return ex.message
    }
    companion object {
        private val log = LoggerFactory.getLogger(NEYRESTfulServer::class.java)
    }
}

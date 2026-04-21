package uk.nhs.nwgenomics.ney.providers

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.annotation.Operation

import ca.uhn.fhir.rest.api.server.RequestDetails

import jakarta.servlet.http.HttpServletRequest


import org.hl7.fhir.r4.model.*

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.nhs.nwgenomics.ney.configuration.FHIRServerProperties



@Component
class RPCProvider(
    val ctx: FhirContext,
    val fhirServerProperties: FHIRServerProperties

) {
    var icsReader = ISCReader(ctx)
    companion object {
        private val log = LoggerFactory.getLogger(RPCProvider::class.java)
    }


    @Operation(name = "\$find-test-metadata", idempotent = true, canonicalUrl = "http://hl7.org/fhir/OperationDefinition/MessageHeader-process-message")
    fun processMessage(
        servletRequest: HttpServletRequest,
        theRequestDetails : RequestDetails,

             ): Bundle? {

        // basic checks complete, now validate
        val resource = icsReader.readFromUrl("/ServiceRequest", "requester=131888757&_revinclude=DiagnosticReport:based-on")
        if (resource is Bundle) {
           return resource
        }
        val responseBundle = Bundle()
        //responseBundle.setIdentifier(bundle.identifier)
        responseBundle.setType(Bundle.BundleType.COLLECTION)

        return responseBundle
    }


}

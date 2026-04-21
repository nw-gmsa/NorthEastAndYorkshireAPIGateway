package uk.nhs.nwgenomics.ney.providers

import ca.uhn.fhir.rest.annotation.Operation
import ca.uhn.fhir.rest.annotation.ResourceParam
import ca.uhn.fhir.rest.api.server.RequestDetails
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException
import jakarta.servlet.http.HttpServletRequest


import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.nhs.nwgenomics.ney.configuration.FHIRServerProperties
import java.util.*


@Component
class RPCProvider(

    val fhirServerProperties: FHIRServerProperties

) {

    companion object {
        private val log = LoggerFactory.getLogger(RPCProvider::class.java)
    }


    @Operation(name = "\$GenomicTestMetadata", idempotent = true, canonicalUrl = "http://hl7.org/fhir/OperationDefinition/MessageHeader-process-message")
    fun processMessage(
        servletRequest: HttpServletRequest,
        theRequestDetails : RequestDetails,

             ): Bundle? {

        // basic checks complete, now validate

        val responseBundle = Bundle()
        //responseBundle.setIdentifier(bundle.identifier)
        responseBundle.setType(Bundle.BundleType.COLLECTION)


        return responseBundle
    }


}

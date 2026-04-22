package uk.nhs.nwgenomics.ney.providers


import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.annotation.Operation
import ca.uhn.fhir.rest.annotation.OperationParam
import ca.uhn.fhir.rest.api.server.RequestDetails
import jakarta.servlet.http.HttpServletRequest
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Organization
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.Specimen
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

    var serverBase: String = "https://hapi.fhir.org/baseR4"
    var client = ctx.newRestfulGenericClient(serverBase)

    @Operation(name = "\$find-test-metadata", idempotent = true, canonicalUrl = "http://hl7.org/fhir/OperationDefinition/MessageHeader-process-message")
    fun processMessage(
        servletRequest: HttpServletRequest,
        theRequestDetails : RequestDetails,
        @OperationParam(name = "ICSODSCode") icsODSCode: List<String>,
             ): Bundle? {
        val orgList = mutableSetOf<String>()

        if (icsODSCode.size >0) {
            val icsResults: Bundle = client.search<Bundle>()
                .forResource(Organization::class.java)
                .where(Organization.IDENTIFIER.exactly().systemAndCode("https://fhir.nhs.uk/Id/ods-organization-code",icsODSCode.get(0)))
                .execute()

            if (icsResults.entry.size > 0) {
                val orgResults: Bundle = client.search<Bundle>()
                    .forResource(Organization::class.java)
                    .where(Organization.PARTOF.hasId(icsResults.entry.get(0).resource.idElement))
                    .and(Organization.TYPE.exactly().code("RO197"))
                    .execute()
                if (orgResults.entry.size > 0) {
                    for (orgEntry in orgResults.entry) {
                        orgList.add(orgEntry.resource.idElement.idPart)
                    }
                }
            }
            log.info("Found ${icsResults.entry.size} results for ${icsODSCode.get(0)}")

        }
        // basic checks complete, now validate

        val ordersResults = client.search<Bundle>()
            .forResource(ServiceRequest::class.java)
            .where(ServiceRequest.REQUESTER.hasAnyOfIds(orgList))
            .revInclude(DiagnosticReport.INCLUDE_BASED_ON)
            .include(ServiceRequest.INCLUDE_SPECIMEN)
            .execute()

        return ordersResults
    }


}

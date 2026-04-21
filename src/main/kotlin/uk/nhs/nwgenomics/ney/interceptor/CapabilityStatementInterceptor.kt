package uk.nhs.nwgenomics.ney.interceptor

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.interceptor.api.Hook
import ca.uhn.fhir.interceptor.api.Interceptor
import ca.uhn.fhir.interceptor.api.Pointcut
import org.hl7.fhir.instance.model.api.IBaseConformance
import org.hl7.fhir.r4.model.*
import uk.nhs.nwgenomics.ney.configuration.FHIRServerProperties

@Interceptor
class CapabilityStatementInterceptor(
    fhirContext: FhirContext,
    private val fhirServerProperties: FHIRServerProperties
) {

    @Hook(Pointcut.SERVER_CAPABILITY_STATEMENT_GENERATED)
    fun customize(theCapabilityStatement: IBaseConformance) {

        // Cast to the appropriate version
        val cs: CapabilityStatement = theCapabilityStatement as CapabilityStatement


        cs.name = fhirServerProperties.server.name
        cs.software.name = fhirServerProperties.server.name
        cs.software.version = fhirServerProperties.server.version
        cs.publisher = "NHS North West Genomics"
        cs.implementation.url =  fhirServerProperties.server.baseUrl + "/FHIR/R4"
        cs.implementation.description = fhirServerProperties.server.name
        if (fhirServerProperties.oauth2.enabled) {
            val oauth2 = CodeableConcept().addCoding(Coding().setSystem("http://terminology.hl7.org/CodeSystem/restful-security-service").setCode("SMART-on-FHIR"))
            val security = CapabilityStatement.CapabilityStatementRestSecurityComponent()
            security.service.add(oauth2)
            security.description = "See OAS specification (swagger page)"
            cs.restFirstRep.setSecurity(security)
        }
    }

    fun getResourceComponent(type : String, cs : CapabilityStatement ) : CapabilityStatement.CapabilityStatementRestResourceComponent? {
        for (rest in cs.rest) {
            for (resource in rest.resource) {
                // println(type + " - " +resource.type)
                if (resource.type.equals(type))
                    return resource
            }
        }
        return null
    }


}

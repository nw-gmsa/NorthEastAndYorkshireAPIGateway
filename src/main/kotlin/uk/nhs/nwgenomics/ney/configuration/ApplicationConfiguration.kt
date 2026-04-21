package uk.nhs.nwgenomics.ney.configuration

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.LenientErrorHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

import uk.nhs.nwgenomics.ney.NEYRESTfulServer


@Configuration
open class ApplicationConfiguration(

) {
    private val logger = LoggerFactory.getLogger(NEYRESTfulServer::class.java)
    @Bean("R4")
    @Primary
    open fun fhirR4Context(): FhirContext {
        val fhirContext = FhirContext.forR4()
        fhirContext.setParserErrorHandler(LenientErrorHandler())
        return fhirContext
    }


    @Bean
    open fun restTemplate(): RestTemplate {
        return RestTemplate()
    }







}

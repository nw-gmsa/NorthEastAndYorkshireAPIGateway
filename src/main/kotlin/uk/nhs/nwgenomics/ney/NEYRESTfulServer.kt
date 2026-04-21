package uk.nhs.nwgenomics.ney

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.api.EncodingEnum
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider

import ca.uhn.fhir.rest.server.IResourceProvider
import ca.uhn.fhir.rest.server.RestfulServer
import ca.uhn.fhir.util.VersionUtil
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import uk.nhs.nwgenomics.ney.configuration.FHIRServerProperties
import uk.nhs.nwgenomics.ney.interceptor.CapabilityStatementInterceptor
import java.util.ArrayList
import java.util.TimeZone

@WebServlet("/FHIR/R4/*", loadOnStartup = 1)
class NEYRESTfulServer internal constructor(
    private val appCtx: ApplicationContext,
    private val ctx: FhirContext,
    val fhirServerProperties: FHIRServerProperties,
) : RestfulServer() {
    override fun addHeadersToResponse(theHttpResponse: HttpServletResponse) {
        theHttpResponse.addHeader("X-Powered-By", "HAPI FHIR " + VersionUtil.getVersion() + " RESTful Server")
    }

    @Throws(ServletException::class)
    override fun initialize() {
        super.initialize()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))


        val fhirVersion = FhirVersionEnum.R4
        fhirContext = FhirContext(fhirVersion)

        defaultResponseEncoding =  EncodingEnum.JSON

        val resourceProviders: MutableList<IResourceProvider?> = ArrayList()


        registerProviders(resourceProviders)

        fhirContext = appCtx.getBean(FhirContext::class.java)

        val plainProviders: List<Any?> = ArrayList()

        // plainProviders.add(appCtx.getBean(ServerProcessMessageProvider.class));
        registerProviders(plainProviders)

        registerInterceptor(CapabilityStatementInterceptor(this.fhirContext,fhirServerProperties))
        if (fhirServerProperties.server.pagingenabled) {
            val pp = FifoMemoryPagingProvider(fhirServerProperties.server.pageSize)
            pp.setDefaultPageSize(fhirServerProperties.server.pageSize)
            pp.setMaximumPageSize(fhirServerProperties.server.pageSizeMax)
            pagingProvider = pp
        }

        isDefaultPrettyPrint = true
        defaultResponseEncoding = EncodingEnum.JSON
    }


    companion object {
        private const val serialVersionUID = 1L
    }
}
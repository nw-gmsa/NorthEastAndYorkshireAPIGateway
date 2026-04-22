package uk.nhs.nwgenomics.ney.configuration

import ca.uhn.fhir.context.FhirContext
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter

import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfig(val ctx : FhirContext) {

    val securitySchemeName = "SMART-on-FHIR";
    lateinit var fhirServerProperties: FHIRServerProperties

    val NEY = "GMSA FHIR API"
    fun getSecurity(): ArrayList<SecurityRequirement> {
        val array = ArrayList<SecurityRequirement>()

        if (fhirServerProperties.oauth2.enabled) {
            val security = SecurityRequirement()
            security.addList(securitySchemeName)
            array.add(security)
        }
        return array
    }

    @Bean
    open fun customOpenAPI(
        xfhirServerProperties: FHIRServerProperties,
        // restfulServer: FHIRR4RestfulServer
    ): OpenAPI? {
        fhirServerProperties = xfhirServerProperties


        var oas = OpenAPI()
            .info(
                Info()
                    .title(fhirServerProperties.server.name)
                    .version(fhirServerProperties.server.version)
                    .description(
                    ""        )
                    .termsOfService("http://swagger.io/terms/")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )

        oas.addServersItem(
            Server().description(fhirServerProperties.server.name).url(fhirServerProperties.server.baseUrl)
        )
        if (fhirServerProperties.oauth2.enabled) {
            val scheme =  SecurityScheme()
                .name(securitySchemeName)
                .scheme("bearer")
                .type(SecurityScheme.Type.OAUTH2)
                .bearerFormat("JWT")
            val scopes = Scopes()
            scopes.put("aws.cognito.signin.user.admin","")
            scopes.put("email","")
            scopes.put("launch/patient","When launching outside the EHR, ask for a patient to be selected at launch time.")
            scopes.put("patient/*.rs","Permission to read and search any resource for the current patient")
            scopes.put("user/*.cruds","Permission to read and write all resources that the current user can access")
            scopes.put("system/*.*","Read/write access to all data")
            val flow = OAuthFlow()
                .authorizationUrl(fhirServerProperties.oauth2.authorizationUrl)
                .tokenUrl(fhirServerProperties.oauth2.tokenUrl)
            flow.scopes = scopes
            scheme.flows = OAuthFlows().authorizationCode(flow            )
            val component = Components().addSecuritySchemes(securitySchemeName,scheme)
            oas.components = component
        }

        oas.path(
            "/FHIR/R4/metadata", PathItem()
                .get(
                    Operation()
                        .addTagsItem(NEY)
                        .summary("server-capabilities: Fetch the server FHIR CapabilityStatement")
                        .responses(getApiResponses())
                )
        )
        val validateItem = PathItem()
            .post(
                Operation()
                    .addTagsItem(NEY)
                    .summary(
                        "Return Genomic Testing Metadata for a ICS ODS Code.")
                    .responses(getApiResponsesJSON())
                    .addParametersItem(Parameter()
                        .name("ICSODSCode")
                        .`in`("query")
                        .required(true)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("ODS of ICS. Multiple values are allowed, use comma to separate.")
                        // Removed example profile
                        .schema(StringSchema().format("string"))
                        .example("QOP"))
                    .addParametersItem(Parameter()
                        .name("_lastUpdated")
                        .`in`("query")
                        .required(false)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("Date since last Updated")
                        // Removed example profile
                        .schema(StringSchema().format("string"))
                        .example("gt2024-04-22")
                    ))

        oas.path("/FHIR/R4/\$find-test-metadata",validateItem)


        return oas
    }
    fun getApiResponses(): ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content =
            Content().addMediaType("application/fhir+json", MediaType().schema(StringSchema()._default("{}")))
                .addMediaType("application/fhir+xml", MediaType().schema(StringSchema()._default("")))
        return ApiResponses().addApiResponse("200", response200)
    }

    fun getApiResponsesJSON(): ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content =
            Content().addMediaType("application/json", MediaType().schema(StringSchema()._default("{}")))
        return ApiResponses().addApiResponse("200", response200)
    }

}

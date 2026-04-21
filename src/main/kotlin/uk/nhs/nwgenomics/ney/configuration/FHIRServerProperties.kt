package uk.nhs.nwgenomics.ney.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fhir")
data class FHIRServerProperties(
    var oauth2: OAuth2,
    var server: Server
) {
    data class Server(
        var baseUrl: String,
        var name: String,
        var version: String,
        var pagingenabled: Boolean,
        var pageSize: Int,
        var pageSizeMax: Int
    )
    data class OAuth2(
        var enabled: Boolean,
        var authorizationUrl: String,
        var tokenUrl: String
    )
}

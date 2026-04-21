package uk.nhs.nwgenomics.ney

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import uk.nhs.nwgenomics.ney.configuration.FHIRServerProperties

@SpringBootApplication
@ServletComponentScan
@EnableConfigurationProperties(FHIRServerProperties::class)
class apigateway

fun main(args: Array<String>) {
    runApplication<apigateway>(*args)
}
package uk.nhs.nwgenomics.ney.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager

import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*

@Configuration
@EnableWebSecurity
open class SecurityConfiguration  {
    @Bean
    public fun configure(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/FHIR/**").permitAll()
                    //.requestMatchers("/R4/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/**").permitAll()
                    .anyRequest().authenticated()
                    //.anyRequest().permitAll()
            }
            .formLogin { }
            .csrf { csrf ->
                csrf
                    .disable()
            }
            .cors{ cors -> corsConfigurationSource()}
        return http.build()
    }

    @Bean
    fun userDetailsService(loader: ResourceLoader): UserDetailsService {
        val users: Resource = loader.getResource("classpath:/users.properties")

        return InMemoryUserDetailsManager(
            User.builder()
                .username("s3cr3t!").password("{noop}56c3044a-8592-4469-93c6-567b927b3d3c")
                .roles("admin", "viewer").build()
        )

    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.addAllowedMethod("GET")
        configuration.addAllowedMethod("PUT")
        configuration.addAllowedMethod("PUT")
        configuration.addAllowedMethod("POST")
        configuration.addAllowedMethod("OPTIONS")
        configuration.allowedOrigins = Arrays.asList("*")
        //configuration.allowedMethods = Arrays.asList("*")
        configuration.allowedHeaders = Arrays.asList("*")
        configuration.maxAge = 3600
        configuration.exposedHeaders = Arrays.asList(
            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
            HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
            HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
            HttpHeaders.ACCESS_CONTROL_MAX_AGE,
            HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

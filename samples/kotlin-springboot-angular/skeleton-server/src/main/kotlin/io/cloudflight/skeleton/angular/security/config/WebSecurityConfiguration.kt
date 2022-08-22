package io.cloudflight.skeleton.angular.security.config

import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.header.writers.HstsHeaderWriter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.header.writers.XContentTypeOptionsHeaderWriter
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .httpBasic()
            .and()
            .logout().permitAll()

        http.headers()
            .addHeaderWriter(XContentTypeOptionsHeaderWriter())
            .addHeaderWriter(XFrameOptionsHeaderWriter())
            .addHeaderWriter(ReferrerPolicyHeaderWriter())
            .addHeaderWriter(XXssProtectionHeaderWriter())
            .addHeaderWriter(HstsHeaderWriter())
    }

    override fun configure(webSecurity: WebSecurity) {
        webSecurity.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations())
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

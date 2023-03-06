package io.cloudflight.skeleton.angular.controller

import io.cloudflight.skeleton.angular.security.service.SecurityService
import io.cloudflight.skeleton.angular.api.HelloWorldApi
import io.cloudflight.skeleton.angular.api.dto.OutputGreeting
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController(
    private val securityService: SecurityService
) : HelloWorldApi {

    override fun getHello(): OutputGreeting {
        return OutputGreeting("Hello ;)", "Pleasure to meet you!")
    }

    override fun getHelloAdmin(name: String): OutputGreeting {
        securityService.assertAdminAccess()
        return OutputGreeting("Hello $name ;)", "Pleasure to meet you $name!")
    }
}

package io.cloudflight.skeleton.angular.api

import io.cloudflight.skeleton.angular.api.dto.OutputGreeting
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Api("HelloWorld")
@Tag(name="HelloWorld")
interface HelloWorldApi {

    @ApiOperation("Returns a greeting from the server")
    @GetMapping("$CONTEXT_PATH/hello")
    fun getHello(): OutputGreeting

    @ApiOperation("Returns a greeting from the admin")
    @GetMapping("$CONTEXT_PATH/hello-admin")
    fun getHelloAdmin(@RequestParam() name: String): OutputGreeting

    companion object {
        private const val CONTEXT_PATH = "/api"
    }

}

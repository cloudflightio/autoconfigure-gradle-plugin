package io.cloudflight.skeleton.angular.api

import io.cloudflight.skeleton.angular.api.dto.CurrentUserDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping

@Api("User")
interface UserApi {

    @ApiOperation("Returns the current user")
    @GetMapping("$CONTEXT_PATH/user")
    fun getCurrentUser(): CurrentUserDto

    companion object {
        private const val CONTEXT_PATH = "/api"
    }
}

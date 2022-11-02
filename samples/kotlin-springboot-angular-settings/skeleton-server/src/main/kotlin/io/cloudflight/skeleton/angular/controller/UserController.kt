package io.cloudflight.skeleton.angular.controller

import io.cloudflight.skeleton.angular.api.UserApi
import io.cloudflight.skeleton.angular.api.dto.CurrentUserDto
import io.cloudflight.skeleton.angular.security.service.SecurityService
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val securityService: SecurityService
) : UserApi {

    override fun getCurrentUser(): CurrentUserDto {
        return securityService.currentUser.let {
            CurrentUserDto(userName = it.userName, name = "${it.firstName} ${it.lastName}")
        }
    }
}

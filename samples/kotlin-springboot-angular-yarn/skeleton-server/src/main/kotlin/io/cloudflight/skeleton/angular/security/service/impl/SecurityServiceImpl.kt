package io.cloudflight.skeleton.angular.security.service.impl

import io.cloudflight.skeleton.angular.security.model.CurrentUser
import io.cloudflight.skeleton.angular.security.service.SecurityService
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class SecurityServiceImpl : SecurityService {
        override val currentUser: CurrentUser
        get() {
            val token = SecurityContextHolder.getContext().authentication
            return if (token is CurrentUser) {
                token
            } else if (token is UsernamePasswordAuthenticationToken) {
                token.principal as CurrentUser
            } else {
                throw AccessDeniedException("User not logged in.")
            }
        }
    val currentUserAvailable: Boolean
        get() = SecurityContextHolder.getContext().authentication != null

    override fun assertAdminAccess() {
        if (!currentUser.isAdmin) throw AccessDeniedException("User does not have admin access")
    }

    override fun getUserName(): String {
        return if (currentUserAvailable) {currentUser.userName} else { "User not logged in" }
    }
}

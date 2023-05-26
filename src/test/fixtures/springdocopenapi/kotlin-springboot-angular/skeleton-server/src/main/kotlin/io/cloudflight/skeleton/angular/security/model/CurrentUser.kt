package io.cloudflight.skeleton.angular.security.model

import io.cloudflight.skeleton.angular.security.ROLE_ADMIN
import org.springframework.security.core.GrantedAuthority

interface CurrentUser {

    val isAdmin: Boolean
        get() = hasRole(ROLE_ADMIN)

    val userName: String

    val firstName: String
    val lastName: String

    fun getAuthorities(): Collection<GrantedAuthority>

    fun hasRole(role: String): Boolean {
        return getAuthorities().stream().anyMatch { r -> r.authority.equals(role, ignoreCase = true) }
    }
}

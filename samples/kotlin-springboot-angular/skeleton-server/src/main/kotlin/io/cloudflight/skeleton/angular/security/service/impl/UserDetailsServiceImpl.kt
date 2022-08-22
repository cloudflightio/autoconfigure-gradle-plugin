package io.cloudflight.skeleton.angular.security.service.impl

import io.cloudflight.skeleton.angular.security.ROLE_ADMIN
import io.cloudflight.skeleton.angular.security.model.CurrentUserImpl
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
internal class UserDetailsServiceImpl(private val passwordEncoder: PasswordEncoder) : UserDetailsService {

    // TODO replace that with calls to the repository
    private val localUsers = listOf(
        createDummyUser(userName = "admin", admin = true, firstName = "Jim", lastName = "Admin"),
        createDummyUser(userName = "user", admin = false, firstName = "John", lastName = "Doe"),
    ).associateBy { it.userName }

    private fun createDummyUser(userName: String, admin: Boolean, firstName: String, lastName: String): User {
        return User(
            userName = userName,
            password = passwordEncoder.encode(userName),
            admin = admin,
            firstName = firstName,
            lastName = lastName
        )
    }

    private class User(
        val userName: String,
        val password: String,
        val admin: Boolean,
        val firstName: String,
        val lastName: String
    )

    override fun loadUserByUsername(username: String): UserDetails {
        val user = localUsers.get(username) ?: throw UsernameNotFoundException("$username not found")
        return CurrentUserImpl(
            userName = user.userName,
            password = user.password,
            firstName = user.firstName,
            lastName = user.lastName,
            authorities = if (user.admin) listOf(SimpleGrantedAuthority(ROLE_ADMIN)) else emptyList()
        )
    }
}

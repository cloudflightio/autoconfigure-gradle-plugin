package io.cloudflight.skeleton.angular.security.model

import org.springframework.security.core.GrantedAuthority

internal class CurrentUserImpl(
    override val userName: String,
    password: String,
    authorities: Collection<GrantedAuthority> = emptyList(),
    override val firstName: String,
    override val lastName: String,
) : org.springframework.security.core.userdetails.User(userName, password, authorities), CurrentUser

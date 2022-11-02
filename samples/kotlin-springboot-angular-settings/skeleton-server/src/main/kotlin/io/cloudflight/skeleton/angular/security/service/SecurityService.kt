package io.cloudflight.skeleton.angular.security.service

import io.cloudflight.skeleton.angular.security.model.CurrentUser

interface SecurityService {
    val currentUser: CurrentUser

    fun assertAdminAccess()

    fun getUserName(): String
}

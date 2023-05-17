package com.study.profile.domain.repository

import com.study.components.model.UiUserPresenceStatus
import com.study.profile.domain.model.UserDetailed

internal interface UserRepository {
    suspend fun getUserById(id: Int): UserDetailed?
    suspend fun getCurrentUser(): UserDetailed
    suspend fun getUserPresence(userId: Int): UiUserPresenceStatus
}

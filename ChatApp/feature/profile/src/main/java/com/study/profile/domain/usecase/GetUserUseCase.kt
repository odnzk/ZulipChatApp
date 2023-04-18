package com.study.profile.domain.usecase

import com.study.profile.domain.exceptions.UserNotFoundException
import com.study.profile.domain.model.UserDetailed
import com.study.profile.domain.repository.UserRepository
import com.study.ui.NavConstants
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
internal class GetUserUseCase @Inject constructor(
    private val repository: UserRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(userId: Int): UserDetailed = withContext(dispatcher) {
        if (userId == NavConstants.CURRENT_USER_ID_KEY) {
            repository.getCurrentUser()
        } else {
            repository.getUserById(userId) ?: throw UserNotFoundException()
        }
    }
}


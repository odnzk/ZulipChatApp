package com.study.users.domain.usecase

import com.study.users.domain.model.User
import com.study.users.domain.repository.UsersRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SearchUsersUseCase(
    private val repository: UsersRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(query: String): List<User> = withContext(dispatcher) {
        repository.getUsers().filter { it.name.startsWith(query) }
    }
}

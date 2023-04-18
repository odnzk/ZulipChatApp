package com.study.users.presentation.elm

import com.study.common.extensions.toFlow
import com.study.users.domain.usecase.GetUsersPresenceCase
import com.study.users.domain.usecase.GetUsersUseCase
import com.study.users.domain.usecase.SearchUsersUseCase
import com.study.users.presentation.util.mapper.toUiUsers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import vivid.money.elmslie.core.switcher.Switcher
import vivid.money.elmslie.coroutines.Actor
import vivid.money.elmslie.coroutines.switch
import javax.inject.Inject

internal class UsersActor @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val getUsersPresenceUseCase: GetUsersPresenceCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val dispatcher: CoroutineDispatcher
) : Actor<UsersCommand, UsersEvent.Internal> {

    private val switcher = Switcher()

    override fun execute(command: UsersCommand): Flow<UsersEvent.Internal> =
        when (command) {
            is UsersCommand.LoadUsers -> switcher.switch {
                toFlow(dispatcher) {
                    getUsersUseCase().toUiUsers(getUsersPresenceUseCase())
                }.mapEvents(
                    UsersEvent.Internal::LoadingUsersSuccess,
                    UsersEvent.Internal::LoadingUsersError
                )
            }
            is UsersCommand.SearchUsers -> switcher.switch {
                toFlow(dispatcher) {
                    searchUsersUseCase(command.query).toUiUsers(getUsersPresenceUseCase())
                }.mapEvents(
                    UsersEvent.Internal::LoadingUsersSuccess,
                    UsersEvent.Internal::LoadingUsersError
                )
            }
        }
}

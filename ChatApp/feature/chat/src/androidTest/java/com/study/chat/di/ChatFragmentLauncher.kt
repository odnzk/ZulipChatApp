package com.study.chat.di

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import com.study.auth.api.UserAuthRepository
import com.study.chat.chat.presentation.ChatFragment
import com.study.chat.shared.di.ChatDep
import com.study.chat.shared.di.ChatDepStore
import com.study.chat.util.TEST_CHANNEL
import com.study.chat.util.TEST_TOPIC
import com.study.database.dao.ChannelTopicDao
import com.study.database.dao.MessageDao
import com.study.database.dao.ReactionDao
import com.study.network.ZulipApi
import com.study.ui.NavConstants
import com.study.ui.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private val searchQueryFlow: Flow<String> = flowOf()

internal fun launchChatFragment(
    zulipApi: ZulipApi,
    reactionDao: ReactionDao,
    messageDao: MessageDao,
    topicDao: ChannelTopicDao,
    userAuthRepository: UserAuthRepository,
    context: Context,
    dispatcher: CoroutineDispatcher,
    isUiNeeded: Boolean = true,
): FragmentScenario<ChatFragment> {
    ChatDepStore.dep = chatFragmentDep(
        zulipApi, reactionDao, messageDao, topicDao, userAuthRepository, context, dispatcher
    )
    val args = bundleOf(
        NavConstants.CHANNEL_ID_KEY to TEST_CHANNEL, NavConstants.TOPIC_TITLE_KEY to TEST_TOPIC
    )
    val fragmentThemeResId = R.style.Theme_ChatApp
    return if (isUiNeeded) {
        launchFragmentInContainer(fragmentArgs = args, themeResId = fragmentThemeResId)
    } else {
        launchFragment(fragmentArgs = args, themeResId = fragmentThemeResId)
    }
}


fun chatFragmentDep(
    zulipApi: ZulipApi,
    reactionDao: ReactionDao,
    messageDao: MessageDao,
    topicDao: ChannelTopicDao,
    userAuthRepository: UserAuthRepository,
    context: Context,
    dispatcher: CoroutineDispatcher
) = object : ChatDep {
    override val dispatcher: CoroutineDispatcher = dispatcher
    override val searchFlow: Flow<String> = searchQueryFlow
    override val zulipApi: ZulipApi = zulipApi
    override val messageDao: MessageDao = messageDao
    override val reactionDao: ReactionDao = reactionDao
    override val topicDao: ChannelTopicDao = topicDao
    override val userAuthRepository: UserAuthRepository = userAuthRepository
    override val applicationContext: Context = context
}

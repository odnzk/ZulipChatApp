package com.study.chat.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.study.chat.data.mapper.toIncomeMessage
import com.study.chat.data.pagination.MessagesPagingSource
import com.study.chat.domain.model.IncomeMessage
import com.study.chat.domain.model.OutcomeMessage
import com.study.chat.domain.repository.MessageRepository
import com.study.network.impl.ZulipApi
import kotlinx.coroutines.flow.Flow

internal class RemoteMessageRepository(private val api: ZulipApi) : MessageRepository {
    override fun getMessages(
        channelTitle: String,
        topicName: String,
        searchQuery: String
    ): Flow<PagingData<IncomeMessage>> {
        return createLatestMessagesPager(channelTitle, topicName, searchQuery).flow
    }

    override suspend fun sendMessage(message: OutcomeMessage): Int {
        val id = api.sendMessage(
            type = message.type,
            to = message.channelTitle,
            content = message.content,
            topic = message.topicTitle
        ).id
        return requireNotNull(id)
    }

    override suspend fun addReaction(messageId: Int, emojiName: String) {
        api.addReactionToMessage(messageId, emojiName)
    }

    override suspend fun removeReaction(messageId: Int, emojiName: String) {
        api.removeReactionFromMessage(messageId, emojiName)
    }

    override suspend fun fetchMessage(messageId: Int): IncomeMessage {
        return requireNotNull(api.fetchSingleMessage(messageId).message).toIncomeMessage()
    }

    private fun createLatestMessagesPager(
        channelTitle: String,
        topicName: String,
        searchQuery: String
    ): Pager<Int, IncomeMessage> {
        return Pager(
            PagingConfig(
                PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = MAX_PAGER_SIZE,
                prefetchDistance = 2
            )
        )
        { MessagesPagingSource(channelTitle, topicName, searchQuery) }
    }

    companion object {
        private const val PAGE_SIZE = 10
        private const val MAX_PAGER_SIZE = 40
    }
}

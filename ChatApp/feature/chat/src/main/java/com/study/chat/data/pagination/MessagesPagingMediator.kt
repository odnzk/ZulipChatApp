package com.study.chat.data.pagination

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.study.chat.data.mapper.getAllReactionEntities
import com.study.chat.data.mapper.toMessageEntities
import com.study.database.dataSource.MessageLocalDataSource
import com.study.database.entity.tuple.MessageWithReactionsTuple
import com.study.network.dataSource.MessageRemoteDataSource
import com.study.network.model.request.message.MessageNarrow
import com.study.network.model.request.message.MessageNarrowList
import com.study.network.model.request.message.MessageNarrowOperator
import com.study.network.model.request.message.MessagesAnchor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@OptIn(ExperimentalPagingApi::class)
class MessagesPagingMediator @AssistedInject constructor(
    private val localDs: MessageLocalDataSource,
    private val remoteDS: MessageRemoteDataSource,
    @Assisted("query") query: String,
    @Assisted("channelTitle") private val channelTitle: String,
    @Assisted("topicName") private val topicTitle: String?,
) : RemoteMediator<Int, MessageWithReactionsTuple>() {
    private val narrows = MessageNarrowList()

    init {
        narrows.run {
            add(MessageNarrow(MessageNarrowOperator.STREAM, channelTitle))
            if (topicTitle != null) {
                add(MessageNarrow(MessageNarrowOperator.TOPIC, topicTitle))
            }
            if (query.isNotEmpty()) {
                add(MessageNarrow(MessageNarrowOperator.SEARCH, query))
            }
        }
    }

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MessageWithReactionsTuple>
    ): MediatorResult {
        val lastItemId: Int = when (loadType) {
            LoadType.REFRESH -> state.lastItemOrNull()?.message?.id ?: DEFAULT_LAST_MESSAGE_ID
            LoadType.PREPEND -> return MediatorResult.Success(true)
            LoadType.APPEND -> state.lastItemOrNull()?.message?.id
                ?: return MediatorResult.Success(true)
        }
        return try {
            val pageSize = state.config.pageSize

            val response = if (lastItemId == DEFAULT_LAST_MESSAGE_ID) {
                remoteDS.getMessages(
                    anchor = MessagesAnchor.NEWEST,
                    numBefore = pageSize,
                    numAfter = 0,
                    narrow = narrows
                )
            } else {
                remoteDS.getMessages(
                    anchorMessageId = lastItemId,
                    numBefore = pageSize,
                    numAfter = 0,
                    narrow = narrows
                )
            }
            val messages = response.toMessageEntities(channelTitle)
            localDs.addMessagesWithReactions(messages, response.getAllReactionEntities())

            MediatorResult.Success(messages.size < pageSize)
        } catch (ex: Exception) {
            MediatorResult.Error(ex)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("channelTitle") channelTitle: String,
            @Assisted("topicName") topicName: String?,
            @Assisted("query") query: String
        ): MessagesPagingMediator
    }

    companion object {
        private const val DEFAULT_LAST_MESSAGE_ID = -1
    }
}

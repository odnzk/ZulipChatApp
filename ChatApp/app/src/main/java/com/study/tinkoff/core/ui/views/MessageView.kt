package com.study.tinkoff.core.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.study.tinkoff.R
import com.study.tinkoff.core.domain.model.Reaction
import com.study.tinkoff.core.ui.extensions.dp
import com.study.tinkoff.core.ui.mappers.toMessageEmojiViews
import java.lang.Integer.max

class MessageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val ivAvatar: ImageView
    private val tvSender: TextView
    private val tvContent: TextView
    private val flexboxEmoji: FlexBoxLayout
    private val textBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val chatMessageBackground = context.getColor(R.color.item_background_color)
    private val meMessageBackground = context.getColor(R.color.accent_color)
    private val textBackground = RectF()
    private val textBackgroundCornerRadius = 18f.dp(context).toFloat()
    private var messageType: MessageType = MessageType.CHAT_MESSAGE

    var onAddReactionClickListener = OnClickListener { }
        set(value) {
            field = value
            flexboxEmoji.onPlusClickListener = field
        }

    fun setChatMessage(
        messageId: Int,
        avatarUrl: String,
        senderName: String,
        messageContent: String,
        reactions: List<Reaction>,
        onReactionClick: ((messageId: Int, emojiName: String) -> Unit)? = null
    ) {
        tvSender.text = senderName
        tvContent.text = messageContent
        ivAvatar.isVisible = true
        tvSender.isVisible = true
        addReactionsToFlexBox(reactions, messageId, onReactionClick)
        messageType = MessageType.CHAT_MESSAGE
    }

    fun setUserMessage(
        messageId: Int,
        messageContent: String,
        reactions: List<Reaction>,
        onReactionClick: ((messageId: Int, emojiName: String) -> Unit)? = null
    ) {
        ivAvatar.isVisible = false
        tvSender.isVisible = false
        tvContent.text = messageContent
        addReactionsToFlexBox(reactions, messageId, onReactionClick)
        messageType = MessageType.ME_MESSAGE
    }

    init {
        inflate(context, R.layout.view_message, this)
        ivAvatar = findViewById(R.id.view_message_iv_avatar)
        tvSender = findViewById(R.id.view_message_tv_user)
        tvContent = findViewById(R.id.view_message_tv_content)
        flexboxEmoji = findViewById(R.id.view_message_fl_emoji)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalsWidthAndHeight = when (messageType) {
            MessageType.ME_MESSAGE -> {
                measureMeMessage(widthMeasureSpec, heightMeasureSpec)
            }
            MessageType.CHAT_MESSAGE -> measureChatMessage(widthMeasureSpec, heightMeasureSpec)
        }
        textBackgroundPaint.color = when (messageType) {
            MessageType.ME_MESSAGE -> {
                val offsetX =
                    (totalsWidthAndHeight.first - paddingRight - tvContent.measuredWidth - tvContent.marginRight - tvContent.marginLeft).toFloat()
                textBackground.set(
                    offsetX,
                    paddingTop.toFloat(),
                    totalsWidthAndHeight.first.toFloat() - paddingRight,
                    (paddingTop + tvContent.marginTop + tvContent.measuredHeight + tvContent.marginBottom).toFloat()
                )
                meMessageBackground
            }
            MessageType.CHAT_MESSAGE -> {
                val textBackgroundStart =
                    (paddingLeft + ivAvatar.marginLeft + ivAvatar.measuredWidth + ivAvatar.marginRight).toFloat()
                textBackground.set(
                    textBackgroundStart,
                    paddingTop.toFloat(),
                    textBackgroundStart + max(
                        tvSender.marginLeft + tvSender.measuredWidth + tvSender.marginRight,
                        tvContent.marginLeft + tvContent.measuredWidth + tvContent.marginRight
                    ),
                    (tvSender.marginTop + tvSender.measuredHeight + tvSender.marginBottom + tvContent.marginTop + tvContent.measuredHeight + tvContent.marginBottom).toFloat()
                )
                chatMessageBackground
            }
        }
        setMeasuredDimension(totalsWidthAndHeight.first, totalsWidthAndHeight.second)
    }

    private fun measureMeMessage(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        measureChildWithMargins(tvContent, widthMeasureSpec, 0, heightMeasureSpec, 0)
        measureChildWithMargins(
            flexboxEmoji,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            tvContent.marginTop + tvContent.measuredHeight + tvContent.marginBottom
        )
        val tvContentHeight =
            tvContent.measuredHeight + tvContent.marginTop + tvContent.marginBottom
        val flexboxHeight =
            flexboxEmoji.measuredHeight + flexboxEmoji.marginTop + flexboxEmoji.marginBottom
        val totalWidth = paddingLeft + paddingRight + max(
            tvContent.measuredWidth + tvContent.marginLeft + tvContent.marginRight,
            flexboxEmoji.measuredWidth
        )
        val totalHeight = paddingTop + paddingBottom + tvContentHeight + flexboxHeight
        return resolveSize(totalWidth, widthMeasureSpec) to totalHeight
    }

    private fun measureChatMessage(widthMeasureSpec: Int, heightMeasureSpec: Int): Pair<Int, Int> {
        var heightUsed = 0
        var avatarWidth = 0
        measureChildWithMargins(
            ivAvatar, widthMeasureSpec, 0, heightMeasureSpec, 0
        )
        avatarWidth += ivAvatar.measuredWidth + ivAvatar.marginLeft + ivAvatar.marginRight
        measureChildWithMargins(tvSender, widthMeasureSpec, avatarWidth, heightMeasureSpec, 0)
        heightUsed += tvSender.measuredHeight + tvSender.marginTop + tvSender.marginBottom
        measureChildWithMargins(
            tvContent, widthMeasureSpec, avatarWidth, heightMeasureSpec, heightUsed
        )
        heightUsed += tvContent.marginTop + tvContent.measuredHeight + tvContent.marginBottom
        measureChildWithMargins(
            flexboxEmoji, widthMeasureSpec, avatarWidth, heightMeasureSpec, heightUsed
        )
        val maxTextWidth = max(
            tvSender.measuredWidth + tvSender.marginRight + tvSender.marginLeft,
            tvContent.measuredWidth + tvContent.marginLeft + tvContent.marginRight
        )
        val tvUserHeight = tvSender.measuredHeight + tvSender.marginTop + tvSender.marginBottom
        val tvContentHeight =
            tvContent.measuredHeight + tvContent.marginTop + tvContent.marginBottom
        val flexboxHeight =
            flexboxEmoji.measuredHeight + flexboxEmoji.marginTop + flexboxEmoji.marginBottom

        val totalWidth =
            paddingLeft + paddingRight + ivAvatar.measuredWidth + ivAvatar.marginLeft + ivAvatar.marginRight + max(
                maxTextWidth, flexboxEmoji.measuredWidth
            )
        val totalHeight = paddingTop + paddingBottom + max(
            ivAvatar.marginTop + ivAvatar.marginBottom + ivAvatar.measuredHeight,
            tvUserHeight + tvContentHeight + flexboxHeight
        )
        return resolveSize(totalWidth, widthMeasureSpec) to totalHeight
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        when (messageType) {
            MessageType.ME_MESSAGE -> layoutMeMessage()
            MessageType.CHAT_MESSAGE -> layoutChatMessage()
        }
    }

    private fun layoutChatMessage() {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        ivAvatar.layout(
            offsetX + ivAvatar.marginLeft,
            offsetY + ivAvatar.marginTop,
            offsetX + ivAvatar.measuredWidth,
            offsetY + ivAvatar.measuredHeight + ivAvatar.marginTop
        )
        offsetX += ivAvatar.measuredWidth + ivAvatar.marginRight + ivAvatar.marginLeft
        tvSender.layout(
            offsetX + tvSender.marginLeft,
            offsetY + tvSender.marginTop,
            offsetX + tvSender.measuredWidth + tvSender.marginLeft,
            offsetY + tvSender.marginTop + tvSender.measuredHeight
        )
        offsetY += tvSender.measuredHeight + tvSender.marginTop + tvSender.marginBottom
        tvContent.layout(
            offsetX + tvContent.marginLeft,
            offsetY + tvContent.marginTop,
            offsetX + tvContent.measuredWidth + tvContent.marginLeft,
            offsetY + tvContent.marginTop + tvContent.measuredHeight
        )
        offsetY += tvContent.marginTop + tvContent.measuredHeight + tvContent.marginBottom
        flexboxEmoji.layout(
            offsetX + flexboxEmoji.marginLeft,
            offsetY + flexboxEmoji.marginTop,
            offsetX + flexboxEmoji.marginLeft + flexboxEmoji.measuredWidth,
            offsetY + flexboxEmoji.marginTop + flexboxEmoji.measuredHeight
        )
    }

    private fun layoutMeMessage() {
        val offsetX = width - paddingRight
        var offsetY = paddingTop
        tvContent.layout(
            offsetX - tvContent.measuredWidth - tvContent.marginRight,
            offsetY + tvContent.marginTop,
            width - paddingRight,
            offsetY + tvContent.marginTop + tvContent.measuredHeight
        )
        offsetY += tvContent.marginTop + tvContent.measuredHeight + tvContent.marginBottom
        flexboxEmoji.layout(
            offsetX - flexboxEmoji.measuredWidth - flexboxEmoji.marginRight,
            offsetY + flexboxEmoji.marginTop,
            width - paddingRight,
            offsetY + flexboxEmoji.marginTop + flexboxEmoji.measuredHeight
        )
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(
            textBackground,
            textBackgroundCornerRadius,
            textBackgroundCornerRadius,
            textBackgroundPaint
        )
        super.dispatchDraw(canvas)
    }


    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    private enum class MessageType {
        ME_MESSAGE, CHAT_MESSAGE
    }

    private fun addReactionsToFlexBox(
        reactions: List<Reaction>,
        messageId: Int,
        onReactionClick: ((messageId: Int, emojiName: String) -> Unit)? = null
    ) {
        flexboxEmoji.removeAllViews()
        reactions.toMessageEmojiViews(context, messageId, onReactionClick).forEach {
            flexboxEmoji.addView(it)
        }
    }
}

package com.study.chat.presentation.emoji

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.navArgs
import com.study.chat.databinding.FragmentBottomSheetListBinding
import com.study.chat.di.ChatComponentViewModel
import com.study.chat.presentation.emoji.delegate.EmojiDelegate
import com.study.chat.presentation.emoji.elm.EmojiListEffect
import com.study.chat.presentation.emoji.elm.EmojiListEvent
import com.study.chat.presentation.emoji.elm.EmojiListState
import com.study.chat.presentation.util.toErrorMessage
import com.study.common.extension.fastLazy
import com.study.components.BaseBottomSheetFragment
import com.study.components.extension.delegatesToList
import com.study.components.extension.dp
import com.study.components.extension.showErrorSnackbar
import com.study.components.recycler.delegates.GeneralAdapterDelegate
import com.study.components.recycler.manager.VarSpanGridLayoutManager
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.core.store.Store
import javax.inject.Inject

internal class EmojiListFragment :
    BaseBottomSheetFragment<EmojiListEvent, EmojiListEffect, EmojiListState>() {
    private var _binding: FragmentBottomSheetListBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EmojiListFragmentArgs>()
    private var mainEmojiAdapter: GeneralAdapterDelegate? = null

    @Inject
    lateinit var store: Store<EmojiListEvent, EmojiListEffect, EmojiListState>

    override val initEvent: EmojiListEvent = EmojiListEvent.Ui.Init
    override val storeHolder: StoreHolder<EmojiListEvent, EmojiListEffect, EmojiListState> by fastLazy {
        LifecycleAwareStoreHolder(lifecycle) { store }
    }

    override fun onAttach(context: Context) {
        ViewModelProvider(this).get<ChatComponentViewModel>().chatComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainEmojiAdapter = null
        _binding = null
    }


    override fun render(state: EmojiListState) {
        when {
            state.error != null ->
                showErrorSnackbar(binding.root, state.error, Throwable::toErrorMessage)
            state.emojis.isNotEmpty() -> mainEmojiAdapter?.submitList(state.emojis)
        }
    }

    private fun initUI() {
        mainEmojiAdapter = GeneralAdapterDelegate(
            delegatesToList(
                EmojiDelegate(onEmojiClick = { emoji ->
                    setFragmentResult(
                        args.resultKey,
                        bundleOf(args.resultKey to emoji)
                    )
                    dismiss()
                }
                )
            )
        )
        with(binding) {
            fragmentEmojiListRvEmoji.run {
                adapter = mainEmojiAdapter
                layoutManager = VarSpanGridLayoutManager(
                    context,
                    80f.dp(context),
                    MIN_GRID_COLUMNS
                )
            }
        }
    }

    companion object {
        private const val MIN_GRID_COLUMNS = 7
    }
}

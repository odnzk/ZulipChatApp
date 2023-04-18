package com.study.channels.presentation.util.pager

import androidx.fragment.app.Fragment
import com.study.channels.presentation.ChannelsFragment
import com.study.channels.presentation.util.model.ChannelFilter
import com.study.components.FragmentPagerFactory

internal class ChannelFragmentFactory : FragmentPagerFactory {
    override fun getFragmentCount(): Int = 2

    override fun getFragmentCreator(position: Int): () -> Fragment = when (position) {
        SUBSCRIBED_CHANNELS_POSITION -> {
            { ChannelsFragment.newInstance(ChannelFilter.SUBSCRIBED_ONLY) }
        }
        ALL_CHANNELS_POSITION -> {
            { ChannelsFragment.newInstance(ChannelFilter.ALL) }
        }
        else -> {
            error("Cannot create fragment for this position")
        }
    }


    companion object {
        private const val SUBSCRIBED_CHANNELS_POSITION = 0
        private const val ALL_CHANNELS_POSITION = 1
    }
}

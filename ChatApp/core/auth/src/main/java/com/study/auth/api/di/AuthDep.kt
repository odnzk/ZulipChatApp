package com.study.auth.api.di

import android.content.Context
import com.study.network.ZulipApi
import kotlinx.coroutines.CoroutineDispatcher

interface AuthDep {
    val dispatcher: CoroutineDispatcher
    val api: ZulipApi
    val context: Context
}

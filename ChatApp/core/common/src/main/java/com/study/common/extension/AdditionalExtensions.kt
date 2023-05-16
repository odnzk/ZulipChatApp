package com.study.common.extension

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

inline fun <T> fastLazy(crossinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) { initializer() }

inline fun <T> toFlow(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    crossinline action: suspend () -> T
): Flow<T> = flow { emit(action()) }.flowOn(coroutineContext)

inline fun <R> runCatchingNonCancellation(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline fun onFailureRestorePrevStateAndThrowError(
    action: () -> Unit,
    restorePreviousState: () -> Unit
) = runCatchingNonCancellation { action() }.onFailure { error ->
    restorePreviousState()
    throw error
}

fun maxOfThee(a: Int, b: Int, c: Int) = when {
    a >= b && a >= c -> a
    b >= c -> b
    else -> c
}

inline fun <reified T> List<*>.firstInstance(): T = first { it is T } as T

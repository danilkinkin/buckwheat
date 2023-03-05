package com.danilkinkin.buckwheat.history

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.danilkinkin.buckwheat.data.entities.Spent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate

enum class RowEntityType { DayDivider, Spent, DayTotal }

data class RowEntity(
    val type: RowEntityType,
    val key: String,
    val contentHash: String? = null,
    val day: LocalDate,
    val spent: Spent?,
    var dayTotal: BigDecimal?,
)

@Suppress("UpdateTransitionLabel", "TransitionPropertiesLabel")
@SuppressLint("ComposableNaming", "UnusedTransitionTargetStateParameter")
/**
 * @param state Use [updateAnimatedItemsState].
 */
inline fun LazyListScope.animatedItemsIndexed(
    state: List<AnimatedItem<RowEntity>>,
    enterTransition: EnterTransition = expandVertically(),
    exitTransition: ExitTransition = shrinkVertically(),
    noinline key: ((item: RowEntity) -> Any)? = null,
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: RowEntity) -> Unit
) {
    items(
        state.size,
        if (key != null) { keyIndex: Int -> key(state[keyIndex].item) } else null
    ) { index ->

        val item = state[index]
        val visibility = item.visibility

        key(key?.invoke(item.item)) {
            AnimatedVisibility(
                visibleState = item.visibility,
                enter = enterTransition,
                exit = exitTransition
            ) {
                itemContent(index, item.item)
            }
        }
    }
}

@Composable
fun updateAnimatedItemsState(
    newList: List<RowEntity>
): State<List<AnimatedItem<RowEntity>>> {

    val state = remember { mutableStateOf(emptyList<AnimatedItem<RowEntity>>()) }
    val firstInject = remember { mutableStateOf(true) }

    LaunchedEffect(newList) {
        if (state.value == newList) {
            return@LaunchedEffect
        }
        val oldList = state.value.toList()

        val diffCb = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldList[oldItemPosition].item.key == newList[newItemPosition].key

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                (oldList[oldItemPosition].item.contentHash
                    ?: oldList[oldItemPosition].item.key) == (newList[newItemPosition].contentHash
                    ?: newList[newItemPosition].key)

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): RowEntity =
                newList[newItemPosition]
        }
        val diffResult = calculateDiff(false, diffCb)
        val compositeList = oldList.toMutableList()

        diffResult.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                for (i in 0 until count) {
                    val newItem = AnimatedItem(
                        visibility = MutableTransitionState(firstInject.value),
                        newList[position + i]
                    )
                    newItem.visibility.targetState = true
                    compositeList.add(position + i, newItem)
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                for (i in 0 until count) {
                    compositeList[position + i].visibility.targetState = false
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                // not detecting moves.
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                for (i in 0 until count) {
                    compositeList[position + i].item.dayTotal = (payload as RowEntity).dayTotal
                }
            }
        })

        if (state.value != compositeList) {
            state.value = compositeList
        }
        firstInject.value = false
        val initialAnimation = androidx.compose.animation.core.Animatable(1.0f)
        initialAnimation.animateTo(0f)
        state.value = state.value.filter { it.visibility.targetState }
    }

    return state
}

data class AnimatedItem<T>(
    val visibility: MutableTransitionState<Boolean>,
    val item: T,
) {

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimatedItem<*>

        if (item != other.item) return false

        return true
    }
}


suspend fun calculateDiff(
    detectMoves: Boolean = true,
    diffCb: DiffUtil.Callback
): DiffUtil.DiffResult {
    return withContext(Dispatchers.Unconfined) {
        DiffUtil.calculateDiff(diffCb, detectMoves)
    }
}
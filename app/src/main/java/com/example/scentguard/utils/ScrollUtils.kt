package com.example.scentguard.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

/**
 * Returns true if the list is scrolling up (or at the top), false if scrolling down.
 */
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                (previousIndex > firstVisibleItemIndex).also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            } else {
                (previousScrollOffset >= firstVisibleItemScrollOffset).also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }
    }.value
}

/**
 * Returns true if the container is scrolling up (or at the top), false if scrolling down.
 */
@Composable
fun ScrollState.isScrollingUp(): Boolean {
    var previousOffset by remember(this) { mutableIntStateOf(value) }
    return remember(this) {
        derivedStateOf {
            (previousOffset >= value).also {
                previousOffset = value
            }
        }
    }.value
}

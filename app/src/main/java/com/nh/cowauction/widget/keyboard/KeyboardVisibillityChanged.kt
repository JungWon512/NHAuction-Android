package com.nh.cowauction.widget.keyboard

data class KeyboardVisibilityChanged(
        val visible: Boolean,
        val contentHeight: Int,
        val contentHeightBeforeResize: Int
)
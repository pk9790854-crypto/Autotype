package com.example.autotyper

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TyperAccessibilityService : AccessibilityService() {

    companion object {
        var instance: TyperAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun typeText(text: String): Boolean {
        val focusedNode = findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false

        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }

        val success = focusedNode.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            arguments
        )

        focusedNode.recycle()
        return success
    }
}

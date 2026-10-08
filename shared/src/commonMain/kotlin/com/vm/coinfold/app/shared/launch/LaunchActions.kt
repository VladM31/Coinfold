package com.vm.coinfold.app.shared.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Something the user asked for from outside the app: an icon shortcut or the home screen widget. */
enum class LaunchAction(val key: String) {
    /** Open the expense entry sheet right away. */
    ADD_EXPENSE("add_expense"),

    /** Open the top-up sheet of an account right away. */
    ADD_INCOME("add_income"),
    ;

    companion object {
        fun fromKey(key: String?): LaunchAction? = entries.firstOrNull { it.key == key }
    }
}

/**
 * A mailbox between the platform entry point (which reads the shortcut or widget intent) and the screens.
 * The action stays here until the screen that handles it has consumed it, so it also works when the app
 * is still starting or sits behind the PIN screen.
 */
object LaunchActions {
    private val _pending = MutableStateFlow<LaunchAction?>(null)
    val pending: StateFlow<LaunchAction?> = _pending.asStateFlow()

    fun post(action: LaunchAction) {
        _pending.value = action
    }

    /** Clears [action] if it is still the pending one. */
    fun consume(action: LaunchAction) {
        if (_pending.value == action) _pending.value = null
    }
}

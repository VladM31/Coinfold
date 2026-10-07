package com.vm.coinfold.app.feature.security.domain.services

import com.vm.coinfold.app.feature.security.domain.models.LockStatus
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.utils.nowMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** How long the app may stay in the background before it asks for the PIN again. */
const val LOCK_GRACE_MILLIS = 30_000L

/**
 * Decides when the app is locked. It starts locked if a PIN exists, locks again when the app returns from the
 * background after more than [LOCK_GRACE_MILLIS] (so a quick look at another app or a share sheet does not
 * lock you out), and unlocks after the PIN or biometrics were accepted. Without a PIN it is never locked.
 */
class LockController(
    repository: SecurityRepository,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val now: () -> Long = ::nowMillis,
) {
    private val _status = MutableStateFlow(LockStatus.LOADING)
    val status: StateFlow<LockStatus> = _status.asStateFlow()

    private val _isProtected = MutableStateFlow(false)

    /** True while a PIN is set; used to hide the app in recent apps and from screenshots. */
    val isProtected: StateFlow<Boolean> = _isProtected.asStateFlow()

    private var backgroundedAt: Long? = null

    init {
        scope.launch {
            repository.isPinSet.collect { pinSet ->
                _isProtected.value = pinSet
                when {
                    !pinSet -> _status.value = LockStatus.UNLOCKED
                    // first read after start: a PIN exists, so start locked; later changes (PIN just set) keep it open
                    _status.value == LockStatus.LOADING -> _status.value = LockStatus.LOCKED
                }
            }
        }
    }

    fun unlock() {
        if (_status.value == LockStatus.LOCKED) _status.value = LockStatus.UNLOCKED
    }

    fun onBackground() {
        if (_status.value == LockStatus.UNLOCKED && _isProtected.value) backgroundedAt = now()
    }

    fun onForeground() {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        if (_isProtected.value && now() - since >= LOCK_GRACE_MILLIS) _status.value = LockStatus.LOCKED
    }
}

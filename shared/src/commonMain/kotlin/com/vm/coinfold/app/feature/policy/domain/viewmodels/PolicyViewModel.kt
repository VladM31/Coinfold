package com.vm.coinfold.app.feature.policy.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vm.coinfold.app.feature.appconfig.domain.models.PolicyLink
import com.vm.coinfold.app.feature.appconfig.domain.repositories.AppConfigRepository
import com.vm.coinfold.app.feature.policy.db.storages.PolicyStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PolicyStatus {
    /** The stored answer is still being read. */
    CHECKING,

    /** The current policy is accepted (or there is nothing to accept), the app can be used. */
    ALLOWED,

    /** The user has to accept [PolicyState.policy] first. */
    REQUIRED,
}

data class PolicyState(
    val status: PolicyStatus = PolicyStatus.CHECKING,
    /** The policy to accept; null while it is not loaded yet (first launch without internet). */
    val policy: PolicyLink? = null,
    val isLoading: Boolean = false,
)

/**
 * Start-up gate: the app opens only after the user accepted the current version of the privacy policy.
 * A user who accepted once is not blocked when the settings cannot be downloaded (offline).
 */
class PolicyViewModel(
    private val storage: PolicyStorage,
    private val appConfig: AppConfigRepository,
) : ViewModel() {

    private val language = MutableStateFlow("en")
    private val isLoading = MutableStateFlow(false)

    private val accepted: StateFlow<String?> = storage.acceptedVersion
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<PolicyState> = combine(accepted, appConfig.config, language, isLoading) { accepted, config, lang, loading ->
        when {
            accepted == null -> PolicyState()
            else -> {
                val policy = config?.policyFor(lang)
                when {
                    // a settings file without policies has nothing to accept
                    config != null && policy == null -> PolicyState(PolicyStatus.ALLOWED)
                    accepted == PolicyStorage.NONE -> PolicyState(PolicyStatus.REQUIRED, policy, loading)
                    // accepted before and the new version cannot be checked now: do not lock the user out
                    policy == null -> PolicyState(PolicyStatus.ALLOWED)
                    policy.version == accepted -> PolicyState(PolicyStatus.ALLOWED)
                    else -> PolicyState(PolicyStatus.REQUIRED, policy, loading)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PolicyState())

    fun setLanguage(code: String) {
        language.value = code
    }

    /** Downloads the settings; call on start and when the app comes to the front. */
    fun refresh() {
        if (isLoading.value) return
        viewModelScope.launch {
            isLoading.value = true
            try {
                appConfig.refresh()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun accept() {
        val policy = state.value.policy ?: return
        viewModelScope.launch { storage.accept(policy.version) }
    }
}

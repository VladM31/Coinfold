package com.vm.coinfold.app.feature.overview.domain.viewmodels

import com.vm.coinfold.app.feature.overview.domain.models.OverviewSummary
import com.vm.coinfold.app.shared.domain.models.Period

data class OverviewState(
    val isLoading: Boolean = true,
    val period: Period? = null,
    val summary: OverviewSummary? = null,
)

sealed interface OverviewIntent {
    data object PreviousPeriod : OverviewIntent
    data object NextPeriod : OverviewIntent
}

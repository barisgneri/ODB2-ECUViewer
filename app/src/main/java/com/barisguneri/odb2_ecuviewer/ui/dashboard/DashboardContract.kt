package com.barisguneri.odb2_ecuviewer.ui.dashboard

object DashboardContract {
    data class UiState(
        val isLoading: Boolean = false,
        val list: List<String> = emptyList(),
    )

    sealed interface UiAction

    sealed interface UiEffect
}
package com.barisguneri.odb2_ecuviewer.ui.dashboard

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class DashboardScreenPreviewProvider : PreviewParameterProvider<DashboardContract.UiState> {
    override val values: Sequence<DashboardContract.UiState>
        get() = sequenceOf(
            DashboardContract.UiState(
                isLoading = true,
                list = emptyList(),
            ),
            DashboardContract.UiState(
                isLoading = false,
                list = emptyList(),
            ),
            DashboardContract.UiState(
                isLoading = false,
                list = listOf("Item 1", "Item 2", "Item 3")
            ),
        )
}
package com.barisguneri.odb2_ecuviewer.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import com.barisguneri.odb2_ecuviewer.common.collectWithLifecycle
import com.barisguneri.odb2_ecuviewer.ui.dashboard.DashboardContract.UiAction
import com.barisguneri.odb2_ecuviewer.ui.dashboard.DashboardContract.UiEffect
import com.barisguneri.odb2_ecuviewer.ui.dashboard.DashboardContract.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun DashboardScreen(
    uiState: UiState,
    uiEffect: Flow<UiEffect>,
    onAction: (UiAction) -> Unit,
) {
    uiEffect.collectWithLifecycle {}

    DashboardContent(
        modifier = Modifier.fillMaxSize(),
        uiState = uiState,
        onAction = onAction,
    )
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    uiState: UiState,
    onAction: (UiAction) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Dashboard Content",
            fontSize = 24.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview(
    @PreviewParameter(DashboardScreenPreviewProvider::class) uiState: UiState,
) {
    DashboardScreen(
        uiState = uiState,
        uiEffect = emptyFlow(),
        onAction = {},
    )
}
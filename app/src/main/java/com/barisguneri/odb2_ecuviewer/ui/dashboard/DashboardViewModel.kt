package com.barisguneri.odb2_ecuviewer.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barisguneri.odb2_ecuviewer.delegation.MVI
import com.barisguneri.odb2_ecuviewer.delegation.mvi
import com.barisguneri.odb2_ecuviewer.ui.dashboard.DashboardContract.UiAction
import com.barisguneri.odb2_ecuviewer.ui.dashboard.DashboardContract.UiEffect
import com.barisguneri.odb2_ecuviewer.ui.dashboard.DashboardContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel(),
    MVI<UiState, UiAction, UiEffect> by mvi(UiState()) {

    override fun onAction(uiAction: UiAction) {
        viewModelScope.launch {
        }
    }
}
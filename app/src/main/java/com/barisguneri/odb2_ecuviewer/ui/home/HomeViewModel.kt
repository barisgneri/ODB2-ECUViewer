package com.barisguneri.odb2_ecuviewer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barisguneri.odb2_ecuviewer.delegation.MVI
import com.barisguneri.odb2_ecuviewer.delegation.mvi
import com.barisguneri.odb2_ecuviewer.ui.home.HomeContract.UiAction
import com.barisguneri.odb2_ecuviewer.ui.home.HomeContract.UiEffect
import com.barisguneri.odb2_ecuviewer.ui.home.HomeContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel(),
    MVI<UiState, UiAction, UiEffect> by mvi(UiState()) {

    override fun onAction(uiAction: UiAction) {
        viewModelScope.launch {
        }
    }
}
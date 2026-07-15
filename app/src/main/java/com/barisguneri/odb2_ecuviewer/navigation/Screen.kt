package com.barisguneri.odb2_ecuviewer.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Dashboard : Screen
}
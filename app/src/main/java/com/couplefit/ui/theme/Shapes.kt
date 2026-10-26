package com.couplefit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),     // iOS Badges / Small elements
    medium = RoundedCornerShape(14.dp),   // iOS standard Button/TextField radius
    large = RoundedCornerShape(20.dp),    // iOS standard Grouped Card radius
    extraLarge = RoundedCornerShape(32.dp) // Modals / Sheets
)


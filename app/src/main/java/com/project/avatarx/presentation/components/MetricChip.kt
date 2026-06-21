package com.project.avatarx.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.project.avatarx.ui.theme.CloudGray
import com.project.avatarx.ui.theme.SilverHighlight
import com.project.avatarx.ui.theme.SlateGlass
import com.project.avatarx.ui.theme.SlateGlassBorder

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(SlateGlass)
            .border(1.dp, SlateGlassBorder, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = SilverHighlight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CloudGray
        )
    }
}

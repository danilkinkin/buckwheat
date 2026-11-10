package com.danilkinkin.buckwheat.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ServiceCatalog
import com.danilkinkin.buckwheat.data.ServiceGroup
import com.danilkinkin.buckwheat.data.SubscriptionService

/**
 * The branded tile used by subscription lists. A service without a vector logo gets a
 * brand-coloured monogram, so adding catalog entries never needs artwork.
 */
@Composable
fun ServiceBadge(
    service: SubscriptionService?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val containerColor = service
        ?.let { Color(it.brandColor) }
        ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = if (service != null) {
        if (containerColor.luminance() > 0.5f) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * 0.32f),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (service != null && service.logo != null) {
                Icon(
                    painter = painterResource(service.logo),
                    contentDescription = service.name,
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.56f),
                )
            } else if (service?.monogram != null) {
                Text(
                    text = service.monogram,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    maxLines = 1,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_label),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.45f),
                )
            }
        }
    }
}

/**
 * A compact service pill that opens a grouped picker. Bundles are ordinary list entries; there is
 * no include/child relationship to manage.
 */
@Composable
fun ServicePicker(
    selectedId: String?,
    onSelect: (SubscriptionService?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val selected = ServiceCatalog.find(selectedId)

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable { open = true },
        shape = CircleShape,
        color = if (selected != null) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (selected != null) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ServiceBadge(service = selected, size = 28.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = selected?.name ?: stringResource(R.string.choose_service),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    if (open) {
        Dialog(onDismissRequest = { open = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(min = 300.dp, max = 420.dp),
            ) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.service),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )

                    LazyColumn(Modifier.heightIn(max = 480.dp)) {
                        item("custom") {
                            ServicePickerRow(
                                service = null,
                                label = stringResource(R.string.custom_service),
                                selected = selectedId == null,
                                onClick = {
                                    onSelect(null)
                                    open = false
                                },
                            )
                        }

                        ServiceGroup.entries.forEach { group ->
                            item("group-${group.name}") {
                                Text(
                                    text = group.label(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 14.dp,
                                        bottom = 4.dp,
                                    ),
                                )
                            }

                            items(
                                items = ServiceCatalog.services.filter { it.group == group },
                                key = { it.id },
                            ) { service ->
                                ServicePickerRow(
                                    service = service,
                                    label = service.name,
                                    selected = service.id == selectedId,
                                    onClick = {
                                        onSelect(service)
                                        open = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicePickerRow(
    service: SubscriptionService?,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ServiceBadge(service = service, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_apply),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ServiceGroup.label(): String = when (this) {
    ServiceGroup.BUNDLES -> stringResource(R.string.service_group_bundles)
    ServiceGroup.STREAMING -> stringResource(R.string.service_group_streaming)
    ServiceGroup.MUSIC -> stringResource(R.string.service_group_music)
    ServiceGroup.CLOUD_AI -> stringResource(R.string.service_group_cloud_ai)
    ServiceGroup.OTHER -> stringResource(R.string.service_group_other)
}

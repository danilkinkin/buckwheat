package com.luna.dollargrain.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.ButtonRow
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.widget.extend.ExtendWidgetReceiver
import com.luna.dollargrain.widget.minimal.MinimalWidgetReceiver

const val SETTINGS_TRY_WIDGET_SHEET = "settings.tryWidget"

@Composable
fun TryWidget(appViewModel: AppViewModel = hiltViewModel(), onTried: () -> Unit) {

    Box(contentAlignment = Alignment.TopStart) {
        ButtonRow(
            icon = painterResource(R.drawable.ic_widgets),
            text = stringResource(R.string.home_widgets_label),
            onClick = {
                appViewModel.openSheet(PathState(SETTINGS_TRY_WIDGET_SHEET))
                onTried()
            },
        )
    }

}

@Composable
fun TryWidgetDialog() {
    val context = LocalContext.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_widgets_label),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WidgetRow(
                        preview = painterResource(R.drawable.minimal_app_widget_preview),
                        description = stringResource(R.string.app_widget_minimal_name),
                        onClick = {
                            appWidgetManager.requestPinAppWidget(
                                ComponentName(context, MinimalWidgetReceiver::class.java),
                                null,
                                null
                            )
                        },
                    )

                    WidgetRow(
                        preview = painterResource(R.drawable.extend_app_widget_preview),
                        description = stringResource(R.string.app_widget_extend_name),
                        onClick = {
                            appWidgetManager.requestPinAppWidget(
                                ComponentName(context, ExtendWidgetReceiver::class.java),
                                null,
                                null
                            )
                        },
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier
                                .offset((-20).dp, 0.dp)
                                .rotate(-10F)
                                .shadow(
                                    6.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color.Black.copy(alpha = 0.2f),
                                    ambientColor = Color.Black.copy(alpha = 0.2f)
                                ),
                            painter = painterResource(R.drawable.extend_app_widget_preview),
                            contentDescription = null
                        )
                        Image(
                            modifier = Modifier
                                .offset(70.dp, (-20).dp)
                                .rotate(10F)
                                .shadow(
                                    14.dp,
                                    shape = RoundedCornerShape(50),
                                    spotColor = Color.Black.copy(alpha = 0.5f),
                                    ambientColor = Color.Black.copy(alpha = 0.5f)
                                ),
                            painter = painterResource(R.drawable.minimal_app_widget_preview),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                }
            }

            Text(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.try_home_widgets_desc),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
            )
        }
    }
}

@Composable
fun WidgetRow(preview: Painter, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = preview,
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center,
            text = description,
            style = MaterialTheme.typography.bodyMedium
                .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
        )
    }
}
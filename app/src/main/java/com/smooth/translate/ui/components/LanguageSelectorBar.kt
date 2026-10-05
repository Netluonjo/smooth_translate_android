package com.smooth.translate.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smooth.translate.data.model.LanguageItem
import com.smooth.translate.ui.theme.DarkText
import com.smooth.translate.ui.theme.YellowDark
import com.smooth.translate.ui.theme.YellowPrimary

@Composable
fun LanguageSelectorBar(
    sourceLang: LanguageItem,
    targetLang: LanguageItem,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF93C5FD)
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 300),
        label = "swapRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(29.dp))
            .background(backgroundColor, RoundedCornerShape(29.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Source Language Button
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onSourceClick() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = sourceLang.flagEmoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = sourceLang.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = DarkText,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center Swap Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(2.dp, CircleShape)
                .background(YellowPrimary, CircleShape)
                .clickable {
                    rotation += 180f
                    onSwapClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = DarkText,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(animatedRotation)
            )
        }

        // Target Language Button
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onTargetClick() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = targetLang.flagEmoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = targetLang.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = DarkText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

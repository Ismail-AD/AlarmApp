package com.appdev.alarmapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomButton(
    onClick: () -> Unit,
    text: String,
    textColor: Color = Color.White,
    width: Float = 0.8f,
    backgroundColor: Color = Color(0xffBC243B),
    height: Dp = 50.dp,
    fontSize: TextUnit = 18.sp,
    isBorderPreview: Boolean = false,
    isEnabled: Boolean = true
) {
    Card(
        onClick = { onClick() },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp)),
        enabled = isEnabled,
        border = if (isBorderPreview) BorderStroke(2.dp, Color(0xff7B70FF)) else BorderStroke(
            0.dp,
            Color.Transparent
        ),
        colors = CardDefaults.cardColors(
            disabledContainerColor = Color.Gray,
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text, fontSize = fontSize,
                letterSpacing = 0.sp,
                color = textColor, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomImageButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String, height: Dp = 40.dp,
    width: Float = 0.8f,
    backgroundColor: Color = Color(0xffBC243B),
    icon: ImageVector = Icons.Filled.ArrowForwardIos,
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(20.dp),
    iconColor: Color = Color.White, textColor: Color = Color.White
) {
    Card(
        modifier = modifier
            .fillMaxWidth(width)
            .height(height)
            .border(width = 0.dp, color = Color.Transparent)
            .clip(roundedCornerShape)
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color = backgroundColor),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text, fontSize = 14.sp,
                letterSpacing = 0.sp,
                color = textColor, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
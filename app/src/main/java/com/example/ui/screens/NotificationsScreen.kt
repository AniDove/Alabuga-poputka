package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
  viewModel: AppViewModel,
  onOpenRide: (String) -> Unit
) {
  val currentUser = viewModel.getCurrentUser()
  val allNotifications by viewModel.notifications.collectAsState()

  val myNotifications = allNotifications.filter { it.userId == currentUser.id }
  val unreadCount = myNotifications.count { !it.isRead }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Уведомления",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "Статусы поездок, бронирований и баланса",
            fontSize = 13.sp,
            color = TextSecondary
          )
        }

        if (unreadCount > 0) {
          TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
            Text("Прочитать все", color = BlueSecondary, fontSize = 12.sp)
          }
        }
      }
    }

    if (myNotifications.isEmpty()) {
      item {
        Surface(
          color = SurfaceDark,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Новых уведомлений нет", color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Здесь будут появляться изменения по вашим бронированиям и поездкам.", color = TextSecondary, fontSize = 12.sp)
          }
        }
      }
    } else {
      items(myNotifications, key = { it.id }) { notif ->
        NotificationItemCard(
          notif = notif,
          onOpenRide = {
            if (notif.targetRideId != null) {
              onOpenRide(notif.targetRideId)
            }
          }
        )
      }
    }
  }
}

@Composable
private fun NotificationItemCard(
  notif: AppNotification,
  onOpenRide: () -> Unit
) {
  val dateStr = remember(notif.timestamp) {
    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(notif.timestamp))
  }

  val (icon, iconColor, iconBg) = when (notif.type) {
    NotificationType.REQUEST -> Triple(Icons.Default.PersonAdd, WarningAmber, WarningBg)
    NotificationType.CONFIRMATION -> Triple(Icons.Default.CheckCircle, SuccessGreen, SuccessBg)
    NotificationType.REJECTION -> Triple(Icons.Default.Cancel, ErrorRed, ErrorBg)
    NotificationType.CANCELLATION -> Triple(Icons.Default.EventBusy, ErrorRed, ErrorBg)
    NotificationType.RIDE_UPDATE -> Triple(Icons.Default.DirectionsCar, BlueSecondary, Color(0xFF1E3A8A))
    NotificationType.CHAT -> Triple(Icons.Default.Chat, BlueSecondary, Color(0xFF1E3A8A))
    NotificationType.BALANCE -> Triple(Icons.Default.AccountBalanceWallet, InfoCyan, Color(0xFF0E3A4A))
  }

  Surface(
    color = if (notif.isRead) SurfaceDark else Color(0xFF1E283E),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (notif.isRead) SurfaceBorderDark else BluePrimary.copy(alpha = 0.6f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(notif.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(dateStr, color = TextTertiary, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(notif.message, color = TextSecondary, fontSize = 12.sp)

        if (notif.targetRideId != null) {
          Spacer(modifier = Modifier.height(6.dp))
          TextButton(
            onClick = onOpenRide,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(26.dp)
          ) {
            Text("Посмотреть поездку →", fontSize = 11.sp, color = BlueSecondary)
          }
        }
      }
    }
  }
}

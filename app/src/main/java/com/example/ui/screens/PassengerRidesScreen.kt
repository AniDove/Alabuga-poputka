package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PassengerRidesScreen(
  viewModel: AppViewModel,
  onNavigateToChat: (String) -> Unit
) {
  val currentUser = viewModel.getCurrentUser()
  val allBookings by viewModel.bookings.collectAsState()
  val allRides by viewModel.rides.collectAsState()

  var selectedTab by remember { mutableStateOf("Все") }
  var disputeTargetBooking by remember { mutableStateOf<Booking?>(null) }
  var complaintTargetBooking by remember { mutableStateOf<Booking?>(null) }

  val myBookings = allBookings.filter { it.passengerId == currentUser.id }

  val filteredBookings = myBookings.filter { b ->
    when (selectedTab) {
      "Ожидают" -> b.status == BookingStatus.PENDING
      "Подтверждены" -> b.status == BookingStatus.CONFIRMED
      "Завершены" -> b.status == BookingStatus.COMPLETED
      "Отменены" -> b.status == BookingStatus.CANCELLED || b.status == BookingStatus.REJECTED
      else -> true
    }
  }

  val tabs = listOf("Все", "Ожидают", "Подтверждены", "Завершены", "Отменены")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Column {
        Text(
          text = "Мои бронирования",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "История ваших заявок и подтвержденных поездок",
          fontSize = 13.sp,
          color = TextSecondary
        )
      }
    }

    // Filter Tabs
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(tabs) { tab ->
          val isSelected = selectedTab == tab
          Surface(
            color = if (isSelected) BluePrimary else SurfaceDark,
            shape = RoundedCornerShape(16.dp),
            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.clickable { selectedTab = tab }
          ) {
            Text(
              text = tab,
              color = if (isSelected) Color.White else TextSecondary,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    if (filteredBookings.isEmpty()) {
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
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = TextTertiary,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Поездок в этом разделе нет",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Используйте вкладку «Поиск», чтобы найти нужный маршрут и забронировать место.",
              color = TextSecondary,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    } else {
      items(filteredBookings, key = { it.id }) { booking ->
        val ride = allRides.find { it.id == booking.rideId }
        PassengerBookingCard(
          booking = booking,
          ride = ride,
          onCancelBooking = { viewModel.cancelBooking(booking.id) },
          onConfirmCompletion = {
            if (ride != null) {
              viewModel.markRideCompleted(ride.id)
            }
          },
          onDispute = { disputeTargetBooking = booking },
          onOpenChat = {
            if (ride != null) {
              viewModel.openChatForRide(ride.id)
              onNavigateToChat(ride.id)
            }
          },
          onReport = { complaintTargetBooking = booking }
        )
      }
    }
  }

  // Dispute Dialog
  disputeTargetBooking?.let { b ->
    var disputeReason by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { disputeTargetBooking = null },
      containerColor = SurfaceDark,
      title = { Text("Оспорить поездку", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "Если возникли разногласия с водителем, поездка перейдёт в статус «На проверке», а списание комиссии будет заблокировано до решения администратора.",
            color = TextSecondary,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = disputeReason,
            onValueChange = { disputeReason = it },
            placeholder = { Text("Причина спора...", color = TextTertiary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E),
              focusedBorderColor = WarningAmber
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (disputeReason.isNotBlank()) {
              viewModel.disputeRide(b.rideId, disputeReason)
              disputeTargetBooking = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
          enabled = disputeReason.isNotBlank()
        ) {
          Text("Перевести на проверку", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { disputeTargetBooking = null }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }

  // Complaint Dialog
  complaintTargetBooking?.let { b ->
    ComplaintDialog(
      targetType = "USER",
      targetId = b.rideId,
      targetTitle = "Поездка #${b.id.takeLast(4)}",
      onDismiss = { complaintTargetBooking = null },
      onSubmit = { reason, desc ->
        viewModel.submitComplaint("RIDE", b.rideId, "Бронь #${b.id.takeLast(4)}", reason, desc)
      }
    )
  }
}

@Composable
private fun PassengerBookingCard(
  booking: Booking,
  ride: Ride?,
  onCancelBooking: () -> Unit,
  onConfirmCompletion: () -> Unit,
  onDispute: () -> Unit,
  onOpenChat: () -> Unit,
  onReport: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Status & Price
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        BookingStatusBadge(booking.status)
        Text(
          text = "${booking.totalPrice} ₽ (${booking.seatsCount} мест)",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = SuccessGreen
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Route
      if (ride != null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
          Spacer(modifier = Modifier.width(6.dp))
          Text(ride.fromLocation, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BluePrimary))
          Spacer(modifier = Modifier.width(6.dp))
          Text(ride.toLocation, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Отправление: ${ride.date}, ${ride.time}",
            color = TextSecondary,
            fontSize = 12.sp
          )
          Text(
            text = "Водитель: ${ride.driverName}",
            color = BlueSecondary,
            fontSize = 12.sp
          )
        }
      }

      // Notes if any
      if (booking.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Ваш комментарий: ${booking.notes}",
          color = TextTertiary,
          fontSize = 11.sp
        )
      }

      // Commission info
      Spacer(modifier = Modifier.height(6.dp))
      Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(6.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Оплата: ${booking.paymentMethod.title}",
            color = TextSecondary,
            fontSize = 10.sp
          )
          Text(
            text = "Сервисный сбор 10%: ${booking.commissionAmount} ₽",
            color = TextTertiary,
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons according to state
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        when (booking.status) {
          BookingStatus.PENDING -> {
            OutlinedButton(
              onClick = onCancelBooking,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
              border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Отменить заявку", fontSize = 11.sp)
            }

            Button(
              onClick = onOpenChat,
              colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Чат", fontSize = 11.sp)
            }
          }

          BookingStatus.CONFIRMED -> {
            OutlinedButton(
              onClick = onCancelBooking,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(0.9f)
            ) {
              Text("Отменить бронь", fontSize = 10.sp)
            }

            IconButton(onClick = onOpenChat, modifier = Modifier.size(36.dp)) {
              Icon(Icons.Default.Chat, contentDescription = "Чат", tint = BlueSecondary)
            }

            Button(
              onClick = onConfirmCompletion,
              colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1.3f)
            ) {
              Text("Завершить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          BookingStatus.COMPLETED -> {
            Text(
              text = "Поездка успешно завершена",
              color = SuccessGreen,
              fontSize = 11.sp,
              modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onReport) {
              Text("Пожаловаться", color = TextTertiary, fontSize = 11.sp)
            }
          }

          BookingStatus.CANCELLED, BookingStatus.REJECTED -> {
            Text(
              text = if (booking.status == BookingStatus.REJECTED) "Водитель отклонил запрос" else "Заявка отменена (места освобождены)",
              color = TextTertiary,
              fontSize = 11.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}

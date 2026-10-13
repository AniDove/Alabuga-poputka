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
import com.example.model.*
import com.example.ui.components.BookingStatusBadge
import com.example.ui.theme.*

@Composable
fun DriverRequestsScreen(
  viewModel: AppViewModel,
  onOpenChat: (String) -> Unit
) {
  val currentUser = viewModel.getCurrentUser()
  val allBookings by viewModel.bookings.collectAsState()
  val allRides by viewModel.rides.collectAsState()

  // Find all bookings for rides belonging to this driver
  val driverRideIds = allRides.filter { it.driverId == currentUser.id }.map { it.id }.toSet()
  val driverBookings = allBookings.filter { it.rideId in driverRideIds }

  val pendingBookings = driverBookings.filter { it.status == BookingStatus.PENDING }
  val processedBookings = driverBookings.filter { it.status != BookingStatus.PENDING }

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
          text = "Входящие заявки",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Запросы пассажиров на бронирование мест в ваших рейсах",
          fontSize = 13.sp,
          color = TextSecondary
        )
      }
    }

    // Pending Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ожидают решения (${pendingBookings.size})",
          color = WarningAmber,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }
    }

    if (pendingBookings.isEmpty()) {
      item {
        Surface(
          color = SurfaceDark,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, tint = SuccessGreen)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Новых ожидающих заявок нет", color = TextSecondary, fontSize = 13.sp)
          }
        }
      }
    } else {
      items(pendingBookings, key = { it.id }) { booking ->
        val ride = allRides.find { it.id == booking.rideId }
        RequestCard(
          booking = booking,
          ride = ride,
          onAccept = { viewModel.acceptBooking(booking.id) },
          onReject = { viewModel.rejectBooking(booking.id) },
          onChat = { if (ride != null) onOpenChat(ride.id) }
        )
      }
    }

    // Processed Section
    if (processedBookings.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "История заявок (${processedBookings.size})",
          color = TextSecondary,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }

      items(processedBookings, key = { it.id }) { booking ->
        val ride = allRides.find { it.id == booking.rideId }
        RequestHistoryCard(
          booking = booking,
          ride = ride,
          onChat = { if (ride != null) onOpenChat(ride.id) }
        )
      }
    }
  }
}

@Composable
private fun RequestCard(
  booking: Booking,
  ride: Ride?,
  onAccept: () -> Unit,
  onReject: () -> Unit,
  onChat: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(BluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(booking.passengerName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(booking.passengerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(booking.passengerPhone, color = BlueSecondary, fontSize = 11.sp)
          }
        }

        IconButton(onClick = onChat, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Chat, contentDescription = "Чат", tint = BlueSecondary)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Ride Info
      if (ride != null) {
        Surface(
          color = Color(0xFF131B2E),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Маршрут: ${ride.fromLocation} → ${ride.toLocation}",
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            )
            Text(
              text = "Время рейса: ${ride.date}, ${ride.time}",
              color = TextSecondary,
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Запрошено мест: ${booking.seatsCount} шт.", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("Свободно в авто: ${ride.availableSeats}", color = SuccessGreen, fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Financials
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Оплата: ${booking.paymentMethod.title}", color = TextSecondary, fontSize = 11.sp)
        Text("Сумма: ${booking.totalPrice} ₽ (комиссия 10%: ${booking.commissionAmount} ₽)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
      }

      if (booking.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text("Пожелание пассажира: «${booking.notes}»", color = TextTertiary, fontSize = 11.sp)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action buttons: Accept & Reject
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onReject,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
          border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Отклонить", fontSize = 12.sp)
        }

        Button(
          onClick = onAccept,
          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1.3f),
          enabled = ride != null && ride.availableSeats >= booking.seatsCount
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Подтвердить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RequestHistoryCard(
  booking: Booking,
  ride: Ride?,
  onChat: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(booking.passengerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.width(8.dp))
          BookingStatusBadge(booking.status)
        }
        if (ride != null) {
          Text(
            text = "${ride.fromLocation} → ${ride.toLocation} (${booking.seatsCount} мест, ${booking.totalPrice} ₽)",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      IconButton(onClick = onChat) {
        Icon(Icons.Default.Chat, contentDescription = "Чат", tint = BlueSecondary, modifier = Modifier.size(18.dp))
      }
    }
  }
}

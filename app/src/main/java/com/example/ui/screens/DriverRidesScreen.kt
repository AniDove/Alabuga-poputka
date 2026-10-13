package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DriverRidesScreen(
  viewModel: AppViewModel,
  onCreateRideClick: () -> Unit,
  onNavigateToRequests: () -> Unit,
  onOpenChat: (String) -> Unit
) {
  val currentUser = viewModel.getCurrentUser()
  val allRides by viewModel.rides.collectAsState()
  val allBookings by viewModel.bookings.collectAsState()

  val myRides = allRides.filter { it.driverId == currentUser.id }
  val pendingRequestsCount = allBookings.count { b ->
    myRides.any { it.id == b.rideId } && b.status == BookingStatus.PENDING
  }

  var rideToCancel by remember { mutableStateOf<Ride?>(null) }
  var rideToComplete by remember { mutableStateOf<Ride?>(null) }
  var rideToDispute by remember { mutableStateOf<Ride?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header with incoming requests badge banner
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Мои рейсы водителя",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Управление опубликованными поездками и пассажирами",
              fontSize = 13.sp,
              color = TextSecondary
            )
          }

          IconButton(
            onClick = onCreateRideClick,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(BluePrimary)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Создать поездку", tint = Color.White)
          }
        }

        // Pending requests alert banner
        if (pendingRequestsCount > 0) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = Color(0xFF2E2211),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onNavigateToRequests)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarningAmber)
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Входящие заявки от пассажиров: $pendingRequestsCount",
                  color = WarningAmber,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "Нажмите, чтобы подтвердить или отклонить запросы",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = WarningAmber)
            }
          }
        }
      }
    }

    if (myRides.isEmpty()) {
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
            Icon(Icons.Default.AddRoad, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "У вас нет опубликованных поездок",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Создайте свой первый маршрут между Елабугой и ОЭЗ, чтобы найти попутчиков.",
              color = TextSecondary,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onCreateRideClick,
              colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Создать поездку")
            }
          }
        }
      }
    } else {
      items(myRides, key = { it.id }) { ride ->
        val rideBookings = allBookings.filter { it.rideId == ride.id }
        DriverRideCard(
          ride = ride,
          bookings = rideBookings,
          onComplete = { rideToComplete = ride },
          onCancel = { rideToCancel = ride },
          onDispute = { rideToDispute = ride },
          onOpenChat = onOpenChat
        )
      }
    }
  }

  // Cancel Confirmation Dialog
  rideToCancel?.let { ride ->
    AlertDialog(
      onDismissRequest = { rideToCancel = null },
      containerColor = SurfaceDark,
      title = { Text("Отменить поездку?", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          text = "Маршрут «${ride.fromLocation} → ${ride.toLocation}» будет отменён. Все заявки пассажиров аннулируются. Комиссия сервиса не взимается.",
          color = TextSecondary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.cancelRide(ride.id)
            rideToCancel = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Да, отменить поездку")
        }
      },
      dismissButton = {
        TextButton(onClick = { rideToCancel = null }) {
          Text("Назад", color = TextSecondary)
        }
      }
    )
  }

  // Complete Ride Dialog
  rideToComplete?.let { ride ->
    val confirmedBookings = allBookings.filter { it.rideId == ride.id && it.status == BookingStatus.CONFIRMED }
    val commissionTotal = confirmedBookings.sumOf { it.commissionAmount }
    val revenueTotal = confirmedBookings.sumOf { it.totalPrice }

    AlertDialog(
      onDismissRequest = { rideToComplete = null },
      containerColor = SurfaceDark,
      title = { Text("Завершение поездки", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Отметить поездку «${ride.fromLocation} → ${ride.toLocation}» как завершенную?",
            color = TextPrimary,
            fontSize = 13.sp
          )
          Surface(
            color = Color(0xFF131B2E),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("Подтвержденных пассажиров: ${confirmedBookings.size}", color = TextSecondary, fontSize = 12.sp)
              Text("Общая стоимость мест: $revenueTotal ₽", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Списание комиссии 10%: $commissionTotal ₽", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("(Будет списано с вашего баланса один раз)", color = TextTertiary, fontSize = 10.sp)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.markRideCompleted(ride.id)
            rideToComplete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
          Text("Подтвердить завершение", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { rideToComplete = null }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }

  // Dispute Dialog
  rideToDispute?.let { ride ->
    var disputeReason by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { rideToDispute = null },
      containerColor = SurfaceDark,
      title = { Text("Оспорить завершение поездки", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "Поездка перейдет в статус «На проверке», а комиссия не будет списана до рассмотрения администратором.",
            color = TextSecondary,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = disputeReason,
            onValueChange = { disputeReason = it },
            placeholder = { Text("Причина (пассажир не явился, срыв поездки)...", color = TextTertiary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E)
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (disputeReason.isNotBlank()) {
              viewModel.disputeRide(ride.id, disputeReason)
              rideToDispute = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
          enabled = disputeReason.isNotBlank()
        ) {
          Text("На проверку", color = Color.Black)
        }
      },
      dismissButton = {
        TextButton(onClick = { rideToDispute = null }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun DriverRideCard(
  ride: Ride,
  bookings: List<Booking>,
  onComplete: () -> Unit,
  onCancel: () -> Unit,
  onDispute: () -> Unit,
  onOpenChat: (String) -> Unit
) {
  val confirmedBookings = bookings.filter { it.status == BookingStatus.CONFIRMED }
  val pendingBookings = bookings.filter { it.status == BookingStatus.PENDING }

  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Time, Status, Price
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${ride.date}, ${ride.time}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          RideStatusBadge(ride.status)
        }

        Text(
          text = "${ride.pricePerSeat} ₽/место",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = SuccessGreen
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Route
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

      Spacer(modifier = Modifier.height(10.dp))

      // Seats & Revenue Info
      Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Свободные места:", color = TextSecondary, fontSize = 11.sp)
            Text(
              text = "${ride.availableSeats} из ${ride.totalSeats}",
              color = if (ride.availableSeats > 0) SuccessGreen else TextTertiary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("Пассажиры в рейсе:", color = TextSecondary, fontSize = 11.sp)
            Text(
              text = "${confirmedBookings.sumOf { it.seatsCount }} чел." +
                  (if (pendingBookings.isNotEmpty()) " (+${pendingBookings.size} ждут)" else ""),
              color = if (pendingBookings.isNotEmpty()) WarningAmber else TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }

      // Confirmed passengers list
      if (confirmedBookings.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Пассажиры рейса:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        confirmedBookings.forEach { b ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(18.dp)
                  .clip(CircleShape)
                  .background(BluePrimary),
                contentAlignment = Alignment.Center
              ) {
                Text(b.passengerName.take(1), fontSize = 10.sp, color = Color.White)
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${b.passengerName} (${b.seatsCount} мест)",
                color = TextPrimary,
                fontSize = 12.sp
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("${b.totalPrice} ₽ (${b.paymentMethod.title})", color = TextSecondary, fontSize = 11.sp)
              Spacer(modifier = Modifier.width(6.dp))
              IconButton(onClick = { onOpenChat(ride.id) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Chat, contentDescription = "Чат", tint = BlueSecondary, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (ride.status == RideStatus.ACTIVE) {
          OutlinedButton(
            onClick = onCancel,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Отменить рейс", fontSize = 11.sp)
          }

          Button(
            onClick = onComplete,
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1.3f)
          ) {
            Text("Завершить поездку", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else if (ride.status == RideStatus.COMPLETED) {
          Text(
            text = "Поездка завершена • Комиссия 10% рассчитана",
            color = SuccessGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
          )
          TextButton(onClick = onDispute) {
            Text("Оспорить", color = TextTertiary, fontSize = 11.sp)
          }
        } else if (ride.status == RideStatus.UNDER_REVIEW) {
          Surface(
            color = WarningBg,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Поездка на проверке администратором. Списание комиссии приостановлено.",
              color = WarningAmber,
              fontSize = 11.sp,
              modifier = Modifier.padding(8.dp)
            )
          }
        } else if (ride.status == RideStatus.CANCELLED) {
          Text(
            text = "Рейс отменен • Комиссия сервиса не начислялась",
            color = TextTertiary,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}

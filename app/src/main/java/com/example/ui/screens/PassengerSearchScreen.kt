package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.*
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
fun PassengerSearchScreen(
  viewModel: AppViewModel,
  onNavigateToChats: (String) -> Unit
) {
  val rides by viewModel.rides.collectAsState()
  val locations by viewModel.locations.collectAsState()
  val currentUser = viewModel.getCurrentUser()

  var fromPoint by remember { mutableStateOf("") }
  var toPoint by remember { mutableStateOf("") }
  var selectedDate by remember { mutableStateOf("Все") }
  var selectedPassengers by remember { mutableStateOf(1) }
  var sortBy by remember { mutableStateOf("time") } // "time", "price", "seats"

  var selectedRideForDetails by remember { mutableStateOf<Ride?>(null) }
  var selectedRideForBooking by remember { mutableStateOf<Ride?>(null) }
  var complaintTarget by remember { mutableStateOf<Ride?>(null) }

  // Popular routes for quick 1-tap select
  val popularRoutes = listOf(
    "Южный парк" to "Завод Аурус",
    "ул. Королёва" to "КПП-1 ОЭЗ Алабуга",
    "Общежитие Синергия 1" to "Завод Джошкуноз",
    "Завод Аурус" to "Южный парк",
    "КПП-1 ОЭЗ Алабуга" to "4-й микрорайон"
  )

  // Filtered rides
  val filteredRides = rides.filter { r ->
    val matchesFrom = fromPoint.isBlank() || r.fromLocation.contains(fromPoint, ignoreCase = true)
    val matchesTo = toPoint.isBlank() || r.toLocation.contains(toPoint, ignoreCase = true)
    val matchesDate = selectedDate == "Все" || r.date.contains(selectedDate, ignoreCase = true)
    val matchesSeats = r.availableSeats >= selectedPassengers
    val isAvailable = r.status == RideStatus.ACTIVE
    matchesFrom && matchesTo && matchesDate && matchesSeats && isAvailable
  }.sortedWith(
    when (sortBy) {
      "price" -> compareBy { it.pricePerSeat }
      "seats" -> compareByDescending { it.availableSeats }
      else -> compareBy { it.time }
    }
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "Поездки Елабуга ⇄ ОЭЗ",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Быстрый поиск попутчиков на работу и в город",
          fontSize = 13.sp,
          color = TextSecondary
        )
      }
    }

    // Quick Popular Routes Chips
    item {
      Column {
        Text(
          text = "Популярные маршруты:",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = BlueSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(popularRoutes) { (from, to) ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = SurfaceDark,
              border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
              modifier = Modifier.clickable {
                fromPoint = from
                toPoint = to
              }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = BlueSecondary,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "$from → $to",
                  fontSize = 11.sp,
                  color = TextPrimary,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }

    // Search Box Card
    item {
      Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // From & To fields with swap button
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              // From Field
              LocationDropdownField(
                label = "Откуда",
                value = fromPoint,
                locations = locations,
                onSelected = { fromPoint = it },
                onClear = { fromPoint = "" }
              )

              Spacer(modifier = Modifier.height(8.dp))

              // To Field
              LocationDropdownField(
                label = "Куда",
                value = toPoint,
                locations = locations,
                onSelected = { toPoint = it },
                onClear = { toPoint = "" }
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Swap Button
            IconButton(
              onClick = {
                val tmp = fromPoint
                fromPoint = toPoint
                toPoint = tmp
              },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF283548))
            ) {
              Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Поменять местами",
                tint = BlueSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Date and Passengers selection
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Date Filter
            Box(modifier = Modifier.weight(1f)) {
              var dateExpanded by remember { mutableStateOf(false) }
              OutlinedButton(
                onClick = { dateExpanded = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color(0xFF131B2E),
                  contentColor = TextPrimary
                )
              ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = BlueSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(selectedDate, fontSize = 12.sp, maxLines = 1)
              }
              DropdownMenu(
                expanded = dateExpanded,
                onDismissRequest = { dateExpanded = false },
                modifier = Modifier.background(SurfaceDark)
              ) {
                listOf("Все", "Сегодня", "Завтра").forEach { d ->
                  DropdownMenuItem(
                    text = { Text(d, color = TextPrimary) },
                    onClick = {
                      selectedDate = d
                      dateExpanded = false
                    }
                  )
                }
              }
            }

            // Passenger count
            Box(modifier = Modifier.weight(1f)) {
              var countExpanded by remember { mutableStateOf(false) }
              OutlinedButton(
                onClick = { countExpanded = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color(0xFF131B2E),
                  contentColor = TextPrimary
                )
              ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = BlueSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("$selectedPassengers пасс.", fontSize = 12.sp, maxLines = 1)
              }
              DropdownMenu(
                expanded = countExpanded,
                onDismissRequest = { countExpanded = false },
                modifier = Modifier.background(SurfaceDark)
              ) {
                listOf(1, 2, 3, 4).forEach { count ->
                  DropdownMenuItem(
                    text = { Text("$count пассажир(а)", color = TextPrimary) },
                    onClick = {
                      selectedPassengers = count
                      countExpanded = false
                    }
                  )
                }
              }
            }
          }

          // Reset filters button if applied
          if (fromPoint.isNotBlank() || toPoint.isNotBlank() || selectedDate != "Все" || selectedPassengers > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
              onClick = {
                fromPoint = ""
                toPoint = ""
                selectedDate = "Все"
                selectedPassengers = 1
              },
              modifier = Modifier.align(Alignment.End)
            ) {
              Text("Сбросить фильтры", fontSize = 11.sp, color = BlueSecondary)
            }
          }
        }
      }
    }

    // Sort and Results Count
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Найдено поездок: ${filteredRides.size}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextSecondary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Сортировка: ", fontSize = 11.sp, color = TextTertiary)
          var sortMenuOpen by remember { mutableStateOf(false) }
          Box {
            Text(
              text = when (sortBy) {
                "price" -> "Дешевле"
                "seats" -> "Больше мест"
                else -> "По времени"
              },
              fontSize = 12.sp,
              color = BlueSecondary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier
                .clickable { sortMenuOpen = true }
                .padding(4.dp)
            )
            DropdownMenu(
              expanded = sortMenuOpen,
              onDismissRequest = { sortMenuOpen = false },
              modifier = Modifier.background(SurfaceDark)
            ) {
              DropdownMenuItem(
                text = { Text("По времени", color = TextPrimary) },
                onClick = { sortBy = "time"; sortMenuOpen = false }
              )
              DropdownMenuItem(
                text = { Text("Сначала дешевле", color = TextPrimary) },
                onClick = { sortBy = "price"; sortMenuOpen = false }
              )
              DropdownMenuItem(
                text = { Text("Больше мест", color = TextPrimary) },
                onClick = { sortBy = "seats"; sortMenuOpen = false }
              )
            }
          }
        }
      }
    }

    // Ride Cards List
    if (filteredRides.isEmpty()) {
      item {
        Surface(
          color = SurfaceDark,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
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
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Поездок не найдено",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Попробуйте изменить точки отправления/назначения или сбросить фильтры.",
              color = TextSecondary,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    } else {
      items(filteredRides, key = { it.id }) { ride ->
        RideCard(
          ride = ride,
          onDetailsClick = { selectedRideForDetails = ride },
          onBookClick = { selectedRideForBooking = ride }
        )
      }
    }
  }

  // Ride Details Sheet / Dialog
  selectedRideForDetails?.let { ride ->
    RideDetailsDialog(
      ride = ride,
      onDismiss = { selectedRideForDetails = null },
      onBook = {
        selectedRideForBooking = ride
        selectedRideForDetails = null
      },
      onChat = {
        viewModel.openChatForRide(ride.id)
        selectedRideForDetails = null
        onNavigateToChats(ride.id)
      },
      onReport = {
        complaintTarget = ride
      }
    )
  }

  // Booking Flow Dialog
  selectedRideForBooking?.let { ride ->
    BookingDialog(
      ride = ride,
      currentUser = currentUser,
      onDismiss = { selectedRideForBooking = null },
      onConfirmBooking = { seats, method, notes ->
        viewModel.bookRide(
          rideId = ride.id,
          seatsCount = seats,
          paymentMethod = method,
          notes = notes
        )
        selectedRideForBooking = null
      }
    )
  }

  // Complaint Dialog
  complaintTarget?.let { ride ->
    ComplaintDialog(
      targetType = "RIDE",
      targetId = ride.id,
      targetTitle = "${ride.fromLocation} → ${ride.toLocation} (${ride.driverName})",
      onDismiss = { complaintTarget = null },
      onSubmit = { reason, desc ->
        viewModel.submitComplaint("RIDE", ride.id, "${ride.fromLocation} → ${ride.toLocation}", reason, desc)
      }
    )
  }
}

@Composable
private fun LocationDropdownField(
  label: String,
  value: String,
  locations: List<LocationPoint>,
  onSelected: (String) -> Unit,
  onClear: () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onSelected,
      label = { Text(label, fontSize = 11.sp, color = TextSecondary) },
      placeholder = { Text("Выберите точку...", fontSize = 12.sp, color = TextTertiary) },
      trailingIcon = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (value.isNotBlank()) {
            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Очистить", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
          }
          IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Список", tint = BlueSecondary)
          }
        }
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = Color(0xFF131B2E),
        unfocusedContainerColor = Color(0xFF131B2E),
        focusedBorderColor = BluePrimary,
        unfocusedBorderColor = SurfaceBorderDark
      ),
      singleLine = true,
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.fillMaxWidth()
    )

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .background(SurfaceDark)
        .fillMaxWidth(0.85f)
    ) {
      Text(
        text = "Справочник остановок и заводов:",
        fontSize = 11.sp,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
      )
      locations.forEach { loc ->
        DropdownMenuItem(
          text = {
            Column {
              Text(loc.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
              Text(loc.category, color = BlueSecondary, fontSize = 10.sp)
            }
          },
          onClick = {
            onSelected(loc.name)
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
fun RideCard(
  ride: Ride,
  onDetailsClick: () -> Unit,
  onBookClick: () -> Unit
) {
  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Top info: Time & Price & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = ride.time,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            color = Color(0xFF283548),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = ride.date,
              fontSize = 11.sp,
              color = BlueSecondary,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${ride.pricePerSeat} ₽",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen
          )
          Text(
            text = " /место",
            fontSize = 11.sp,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Route schematic summary
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(SuccessGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = ride.fromLocation,
          color = TextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = null,
          tint = TextSecondary,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(BluePrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = ride.toLocation,
          color = TextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Driver info row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(BlueDark),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = ride.driverName.take(1),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = ride.driverName,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
              Text(" ${ride.driverRating}", fontSize = 11.sp, color = TextSecondary)
              Text(" • ${ride.driverCar}", fontSize = 11.sp, color = TextTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }
        }

        // Available seats pill
        Surface(
          color = if (ride.availableSeats > 0) Color(0xFF132D24) else Color(0xFF3B1E1E),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (ride.availableSeats > 0) SuccessGreen.copy(alpha = 0.5f) else ErrorRed.copy(alpha = 0.5f)
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EventSeat,
              contentDescription = null,
              tint = if (ride.availableSeats > 0) SuccessGreen else ErrorRed,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Мест: ${ride.availableSeats}/${ride.totalSeats}",
              fontSize = 11.sp,
              color = if (ride.availableSeats > 0) SuccessGreen else ErrorRed,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onDetailsClick,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
          border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
        ) {
          Text("Подробнее", fontSize = 12.sp)
        }

        Button(
          onClick = onBookClick,
          modifier = Modifier.weight(1.3f),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
          enabled = ride.availableSeats > 0
        ) {
          Text("Забронировать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun RideDetailsDialog(
  ride: Ride,
  onDismiss: () -> Unit,
  onBook: () -> Unit,
  onChat: () -> Unit,
  onReport: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceDark,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Детали поездки", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        RideStatusBadge(ride.status)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Schematic route visualization
        SchematicRouteVisualizer(
          from = ride.fromLocation,
          to = ride.toLocation,
          time = "${ride.date}, ${ride.time}"
        )

        // Driver details
        Surface(
          color = Color(0xFF131B2E),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BluePrimary),
              contentAlignment = Alignment.Center
            ) {
              Text(ride.driverName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(ride.driverName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Авто: ${ride.driverCar}", color = TextSecondary, fontSize = 11.sp)
              Text("Тел.: ${ride.driverPhone}", color = BlueSecondary, fontSize = 11.sp)
            }
            IconButton(onClick = onChat) {
              Icon(Icons.Default.Chat, contentDescription = "Чат с водителем", tint = BlueSecondary)
            }
          }
        }

        // Details breakdown
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Свободно мест", color = TextSecondary, fontSize = 11.sp)
            Text("${ride.availableSeats} из ${ride.totalSeats}", color = TextPrimary, fontWeight = FontWeight.Bold)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text("Цена за место", color = TextSecondary, fontSize = 11.sp)
            Text("${ride.pricePerSeat} ₽", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }

        if (ride.comment.isNotBlank()) {
          Column {
            Text("Комментарий водителя:", color = TextSecondary, fontSize = 11.sp)
            Text(ride.comment, color = TextPrimary, fontSize = 12.sp)
          }
        }

        // Service commission notice
        Surface(
          color = Color(0xFF1E293B),
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
          Text(
            text = "Сервисный сбор 10% (${(ride.pricePerSeat * 0.1).toInt()} ₽/место) включён в расчёт и списывается только после успешного завершения поездки.",
            color = TextTertiary,
            fontSize = 10.sp,
            modifier = Modifier.padding(8.dp)
          )
        }

        // Report button
        TextButton(
          onClick = onReport,
          modifier = Modifier.align(Alignment.End)
        ) {
          Icon(Icons.Default.ReportProblem, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Пожаловаться", color = TextTertiary, fontSize = 11.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onBook,
        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
        enabled = ride.availableSeats > 0 && ride.status == RideStatus.ACTIVE
      ) {
        Text("Перейти к бронированию")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Закрыть", color = TextSecondary)
      }
    }
  )
}

@Composable
fun BookingDialog(
  ride: Ride,
  currentUser: User,
  onDismiss: () -> Unit,
  onConfirmBooking: (seats: Int, method: PaymentMethod, notes: String) -> Unit
) {
  var seatsCount by remember { mutableStateOf(1) }
  var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
  var notes by remember { mutableStateOf("") }

  val totalPrice = seatsCount * ride.pricePerSeat
  val commission = (totalPrice * 0.1).toInt()

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SurfaceDark,
    title = {
      Text(
        text = "Бронирование поездки",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "${ride.fromLocation} → ${ride.toLocation}",
          color = BlueSecondary,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Text(
          text = "Отправление: ${ride.date}, ${ride.time}",
          color = TextSecondary,
          fontSize = 12.sp
        )

        Divider(color = SurfaceBorderDark)

        // Seats selection
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Количество мест:", color = TextPrimary, fontSize = 13.sp)
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { if (seatsCount > 1) seatsCount-- },
              enabled = seatsCount > 1,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF283548))
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Меньше", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Text(
              text = "$seatsCount",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              modifier = Modifier.padding(horizontal = 14.dp)
            )
            IconButton(
              onClick = { if (seatsCount < ride.availableSeats) seatsCount++ },
              enabled = seatsCount < ride.availableSeats,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF283548))
            ) {
              Icon(Icons.Default.Add, contentDescription = "Больше", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
          }
        }

        // Financial Calculation Breakdown
        Surface(
          color = Color(0xFF131B2E),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BlueDark)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Стоимость ($seatsCount × ${ride.pricePerSeat} ₽):", color = TextSecondary, fontSize = 12.sp)
              Text("$totalPrice ₽", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Комиссия сервиса (10%):", color = TextTertiary, fontSize = 11.sp)
              Text("$commission ₽", color = TextTertiary, fontSize = 11.sp)
            }
            Divider(color = SurfaceBorderDark, modifier = Modifier.padding(vertical = 6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("К оплате водителю:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("$totalPrice ₽", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }
        }

        // Payment method toggle
        Text("Способ оплаты:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = paymentMethod == PaymentMethod.CASH,
            onClick = { paymentMethod = PaymentMethod.CASH },
            label = { Text("Наличные", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(14.dp)) },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = paymentMethod == PaymentMethod.ONLINE_DEMO,
            onClick = { paymentMethod = PaymentMethod.ONLINE_DEMO },
            label = { Text("Онлайн (демо)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp)) },
            modifier = Modifier.weight(1f)
          )
        }

        // Notes for driver
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          placeholder = { Text("Пожелания или место посадки...", fontSize = 11.sp, color = TextTertiary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = Color(0xFF131B2E),
            unfocusedContainerColor = Color(0xFF131B2E),
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = SurfaceBorderDark
          ),
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirmBooking(seatsCount, paymentMethod, notes) },
        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
      ) {
        Text("Отправить заявку", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена", color = TextSecondary)
      }
    }
  )
}

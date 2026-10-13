package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppViewModel
import com.example.model.LocationPoint
import com.example.ui.components.SchematicRouteVisualizer
import com.example.ui.theme.*

@Composable
fun CreateRideScreen(
  viewModel: AppViewModel,
  onRideCreated: () -> Unit
) {
  val locations by viewModel.locations.collectAsState()
  val currentUser = viewModel.getCurrentUser()

  var fromPoint by remember { mutableStateOf("Южный парк") }
  var toPoint by remember { mutableStateOf("КПП-1 ОЭЗ Алабуга") }
  var selectedDate by remember { mutableStateOf("Сегодня") }
  var time by remember { mutableStateOf("07:30") }
  var seatsCount by remember { mutableStateOf("3") }
  var pricePerSeat by remember { mutableStateOf("250") }
  var comment by remember { mutableStateOf("Еду через пр-т Нефтяников. В салоне чисто, без курения.") }

  var fromError by remember { mutableStateOf(false) }
  var toError by remember { mutableStateOf(false) }
  var timeError by remember { mutableStateOf(false) }
  var seatsError by remember { mutableStateOf(false) }
  var priceError by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .padding(bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    Column {
      Text(
        text = "Создание поездки",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = "Опубликуйте маршрут и возьмите попутчиков по пути",
        fontSize = 13.sp,
        color = TextSecondary
      )
    }

    // Driver vehicle banner
    Surface(
      color = SurfaceDark,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BlueSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Ваш автомобиль:", color = TextSecondary, fontSize = 11.sp)
          Text(
            text = if (currentUser.carModel.isNotBlank()) "${currentUser.carModel} (${currentUser.carNumber})" else "Автомобиль не указан в профиле",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }

    // Route Schematic Preview
    SchematicRouteVisualizer(
      from = if (fromPoint.isNotBlank()) fromPoint else "Откуда",
      to = if (toPoint.isNotBlank()) toPoint else "Куда",
      time = "$selectedDate, $time"
    )

    // Form Container
    Surface(
      color = SurfaceDark,
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Point From
        Column {
          LocationPickerField(
            label = "Точка отправления (Откуда)*",
            value = fromPoint,
            locations = locations,
            isError = fromError,
            onSelect = {
              fromPoint = it
              fromError = false
            }
          )
          if (fromError) {
            Text("Укажите точку отправления", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
          }
        }

        // Point To
        Column {
          LocationPickerField(
            label = "Точка назначения (Куда)*",
            value = toPoint,
            locations = locations,
            isError = toError,
            onSelect = {
              toPoint = it
              toError = false
            }
          )
          if (toError) {
            Text("Укажите точку назначения", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
          }
        }

        // Date selection chips
        Column {
          Text("Дата отправления*", color = TextSecondary, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Сегодня", "Завтра", "Послезавтра").forEach { d ->
              FilterChip(
                selected = selectedDate == d,
                onClick = { selectedDate = d },
                label = { Text(d, fontSize = 12.sp) }
              )
            }
          }
        }

        // Time field
        Column {
          OutlinedTextField(
            value = time,
            onValueChange = {
              time = it
              timeError = false
            },
            label = { Text("Время отправления (например, 07:30)*", fontSize = 11.sp) },
            isError = timeError,
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = BlueSecondary) },
            colors = formFieldColors(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
          )
          if (timeError) {
            Text("Укажите время отправления", color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
          }
        }

        // Seats and Price Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Seats count
          Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
              value = seatsCount,
              onValueChange = {
                seatsCount = it.filter { ch -> ch.isDigit() }
                seatsError = false
              },
              label = { Text("Свободных мест*", fontSize = 11.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              isError = seatsError,
              leadingIcon = { Icon(Icons.Default.EventSeat, contentDescription = null, tint = BlueSecondary) },
              colors = formFieldColors(),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )
            if (seatsError) {
              Text("От 1 до 8 мест", color = ErrorRed, fontSize = 11.sp)
            }
          }

          // Price per seat
          Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
              value = pricePerSeat,
              onValueChange = {
                pricePerSeat = it.filter { ch -> ch.isDigit() }
                priceError = false
              },
              label = { Text("Цена за место (₽)*", fontSize = 11.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              isError = priceError,
              leadingIcon = { Icon(Icons.Default.CurrencyRuble, contentDescription = null, tint = SuccessGreen) },
              colors = formFieldColors(),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )
            if (priceError) {
              Text("Укажите цену > 0", color = ErrorRed, fontSize = 11.sp)
            }
          }
        }

        // Commission Breakdown Visual Preview
        val seatsInt = seatsCount.toIntOrNull() ?: 0
        val priceInt = pricePerSeat.toIntOrNull() ?: 0
        val potentialTotal = seatsInt * priceInt
        val potentialCommission = (potentialTotal * 0.1).toInt()
        val driverNet = potentialTotal - potentialCommission

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
              Text("Потенциальный доход ($seatsInt мест):", color = TextSecondary, fontSize = 12.sp)
              Text("$potentialTotal ₽", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Комиссия сервиса 10% (только за факт поездки):", color = TextTertiary, fontSize = 11.sp)
              Text("−$potentialCommission ₽", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Divider(color = SurfaceBorderDark, modifier = Modifier.padding(vertical = 5.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Ваш чистый заработок:", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
              Text("$driverNet ₽", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }
        }

        // Comment / condition
        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          label = { Text("Комментарий и условия (необязательно)", fontSize = 11.sp) },
          placeholder = { Text("Например: багажник свободен, еду до проходной завода Аурус...", color = TextTertiary) },
          colors = formFieldColors(),
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Submit Button
        Button(
          onClick = {
            var hasErr = false
            if (fromPoint.isBlank()) { fromError = true; hasErr = true }
            if (toPoint.isBlank()) { toError = true; hasErr = true }
            if (time.isBlank()) { timeError = true; hasErr = true }
            val s = seatsCount.toIntOrNull()
            if (s == null || s <= 0 || s > 8) { seatsError = true; hasErr = true }
            val p = pricePerSeat.toIntOrNull()
            if (p == null || p <= 0) { priceError = true; hasErr = true }

            if (!hasErr) {
              val ok = viewModel.createRide(
                from = fromPoint,
                to = toPoint,
                date = selectedDate,
                time = time,
                seats = seatsCount.toInt(),
                price = pricePerSeat.toInt(),
                comment = comment
              )
              if (ok) {
                onRideCreated()
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Опубликовать поездку", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun LocationPickerField(
  label: String,
  value: String,
  locations: List<LocationPoint>,
  isError: Boolean,
  onSelect: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onSelect,
      label = { Text(label, fontSize = 11.sp) },
      isError = isError,
      trailingIcon = {
        IconButton(onClick = { expanded = !expanded }) {
          Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать точку", tint = BlueSecondary)
        }
      },
      colors = formFieldColors(),
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(8.dp)
    )

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .background(SurfaceDark)
        .fillMaxWidth(0.85f)
    ) {
      Text(
        text = "Выберите точку из справочника:",
        color = TextSecondary,
        fontSize = 11.sp,
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
            onSelect(loc.name)
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedTextColor = TextPrimary,
  unfocusedTextColor = TextPrimary,
  focusedContainerColor = Color(0xFF131B2E),
  unfocusedContainerColor = Color(0xFF131B2E),
  focusedBorderColor = BluePrimary,
  unfocusedBorderColor = SurfaceBorderDark,
  errorBorderColor = ErrorRed
)

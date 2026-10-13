package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
  viewModel: AppViewModel
) {
  val currentUser = viewModel.getCurrentUser()
  val allUsers by viewModel.users.collectAsState()

  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }
  var notifPushEnabled by remember { mutableStateOf(true) }
  var notifSoundEnabled by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Профиль пользователя",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Profile Card
    item {
      Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(BluePrimary)
              .border(2.dp, BlueSecondary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentUser.name.take(1),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 26.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = currentUser.name,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )

          Surface(
            color = Color(0xFF1E3A8A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = currentUser.role.title,
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stats row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("${currentUser.tripsCount}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text("Поездок", color = TextSecondary, fontSize = 11.sp)
            }
            Divider(modifier = Modifier.height(30.dp).width(1.dp), color = SurfaceBorderDark)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                Text(" ${currentUser.rating}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }
              Text("Рейтинг", color = TextSecondary, fontSize = 11.sp)
            }
            if (currentUser.role == UserRole.DRIVER) {
              Divider(modifier = Modifier.height(30.dp).width(1.dp), color = SurfaceBorderDark)
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${currentUser.balance} ₽", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Баланс", color = TextSecondary, fontSize = 11.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showEditProfileDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283548)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = BlueSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Редактировать данные", fontSize = 12.sp, color = TextPrimary)
          }
        }
      }
    }

    // Contact info & Car info
    item {
      Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Данные аккаунта:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = BlueSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Телефон (демонстрационный)", color = TextTertiary, fontSize = 10.sp)
              Text(currentUser.phone, color = TextPrimary, fontSize = 13.sp)
            }
          }

          if (currentUser.role == UserRole.DRIVER) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BlueSecondary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Автомобиль и номер", color = TextTertiary, fontSize = 10.sp)
                Text(
                  text = if (currentUser.carModel.isNotBlank()) "${currentUser.carModel} (${currentUser.carNumber})" else "Не указан",
                  color = TextPrimary,
                  fontSize = 13.sp
                )
              }
            }
          }
        }
      }
    }

    // Notifications Settings
    item {
      Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Настройки уведомлений:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Push-уведомления о статусе брони", color = TextPrimary, fontSize = 13.sp)
            Switch(
              checked = notifPushEnabled,
              onCheckedChange = { notifPushEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BluePrimary)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Звук входящих сообщений в чате", color = TextPrimary, fontSize = 13.sp)
            Switch(
              checked = notifSoundEnabled,
              onCheckedChange = { notifSoundEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BluePrimary)
            )
          }
        }
      }
    }

    // Demo Data Management & Reset
    item {
      Surface(
        color = Color(0xFF1E2430),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Управление демонстрационными данными:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Прототип сохраняет все изменения (поездки, бронирования, чаты, баланс) на устройстве. При необходимости вы можете вернуть базу к исходному состоянию.",
            color = TextSecondary,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedButton(
            onClick = { showResetConfirmDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Сбросить демо-данные к исходным")
          }
        }
      }
    }
  }

  // Edit Profile Dialog
  if (showEditProfileDialog) {
    var editName by remember { mutableStateOf(currentUser.name) }
    var editPhone by remember { mutableStateOf(currentUser.phone) }
    var editCarModel by remember { mutableStateOf(currentUser.carModel) }
    var editCarNumber by remember { mutableStateOf(currentUser.carNumber) }

    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      containerColor = SurfaceDark,
      title = { Text("Редактирование профиля", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Имя и фамилия") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E),
              focusedBorderColor = BluePrimary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = editPhone,
            onValueChange = { editPhone = it },
            label = { Text("Номер телефона") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E),
              focusedBorderColor = BluePrimary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          if (currentUser.role == UserRole.DRIVER) {
            OutlinedTextField(
              value = editCarModel,
              onValueChange = { editCarModel = it },
              label = { Text("Марка и цвет авто") },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedBorderColor = BluePrimary
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = editCarNumber,
              onValueChange = { editCarNumber = it },
              label = { Text("Госномер (например: А123ВС 116)") },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedBorderColor = BluePrimary
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateProfile(editName, editPhone, editCarModel, editCarNumber)
            showEditProfileDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
          Text("Сохранить")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }

  // Reset Demo Confirm Dialog
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      containerColor = SurfaceDark,
      title = { Text("Сбросить все демо-данные?", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          text = "Все созданные тестовые поездки, заявки, баланс и новые сообщения будут возвращены к начальным фабричным значениям демонстрации.",
          color = TextSecondary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetAllDemoData()
            showResetConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Сбросить")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }
}

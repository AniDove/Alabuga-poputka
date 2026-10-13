package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinanceScreen(
  viewModel: AppViewModel
) {
  val currentUser = viewModel.getCurrentUser()
  val allTransactions by viewModel.transactions.collectAsState()
  val allRides by viewModel.rides.collectAsState()
  val allBookings by viewModel.bookings.collectAsState()

  val myTransactions = allTransactions.filter { it.driverId == currentUser.id }
  val totalCommissionPaid = myTransactions
    .filter { it.type == TransactionType.COMMISSION_FEE }
    .sumOf { kotlin.math.abs(it.amount) }

  var showTopUpDialog by remember { mutableStateOf(false) }
  var showRulesDialog by remember { mutableStateOf(false) }

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
          text = "Финансы и баланс",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Учёт комиссии сервиса и баланс водителя (демо-режим)",
          fontSize = 13.sp,
          color = TextSecondary
        )
      }
    }

    // Demo Mode Notice Banner
    item {
      Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Info, contentDescription = null, tint = InfoCyan, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Тестовый кошелек. Реальные денежные переводы и платежные системы отключены.",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    // Balance Card
    item {
      Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Текущий баланс водителя:", color = TextSecondary, fontSize = 12.sp)
            IconButton(onClick = { showRulesDialog = true }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.HelpOutline, contentDescription = "Правила комиссии", tint = BlueSecondary)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "${currentUser.balance} ₽",
            color = if (currentUser.balance < 100) WarningAmber else SuccessGreen,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
          )

          if (currentUser.balance < 100) {
            Text(
              text = "Внимание: баланс ниже 100 ₽. Рекомендуется пополнить для беспрепятственного списания комиссии за рейсы.",
              color = WarningAmber,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Key metrics row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              color = Color(0xFF131B2E),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Всего списано комиссии", color = TextTertiary, fontSize = 10.sp)
                Text("$totalCommissionPaid ₽", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
            }

            Surface(
              color = Color(0xFF131B2E),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Тариф комиссии", color = TextTertiary, fontSize = 10.sp)
                Text("10% за место", color = BlueSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Top up action button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showTopUpDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Пополнить баланс (демо)")
            }

            OutlinedButton(
              onClick = { showRulesDialog = true },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
              border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
            ) {
              Text("Правила")
            }
          }
        }
      }
    }

    // Commission Model Visual Explainer
    item {
      Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlueDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Примеры расчета комиссии сервиса (10%):",
            color = BlueSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text("• Место 250 ₽ → 25 ₽ комиссии сервису (водителю остаётся 225 ₽)", color = TextSecondary, fontSize = 11.sp)
          Text("• Место 350 ₽ → 35 ₽ комиссии сервису (водителю остаётся 315 ₽)", color = TextSecondary, fontSize = 11.sp)
          Text("• Место 500 ₽ → 50 ₽ комиссии сервису (водителю остаётся 450 ₽)", color = TextSecondary, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Комиссия рассчитывается единожды только после фактического завершения рейса.", color = TextTertiary, fontSize = 10.sp)
        }
      }
    }

    // Transactions History
    item {
      Text(
        text = "История финансовых операций",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
      )
    }

    if (myTransactions.isEmpty()) {
      item {
        Surface(
          color = SurfaceDark,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Операций пока нет. Пополните баланс или завершите поездку.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(myTransactions, key = { it.id }) { tx ->
        TransactionCard(tx)
      }
    }
  }

  // Demo Top-up Dialog
  if (showTopUpDialog) {
    var topUpAmountText by remember { mutableStateOf("500") }
    AlertDialog(
      onDismissRequest = { showTopUpDialog = false },
      containerColor = SurfaceDark,
      title = { Text("Демо-пополнение баланса", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Введите сумму для виртуального зачисления на баланс. Реальные деньги не списываются.",
            color = TextSecondary,
            fontSize = 12.sp
          )
          OutlinedTextField(
            value = topUpAmountText,
            onValueChange = { topUpAmountText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Сумма в рублях (₽)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("200", "500", "1000", "2000").forEach { preset ->
              FilterChip(
                selected = topUpAmountText == preset,
                onClick = { topUpAmountText = preset },
                label = { Text("$preset ₽", fontSize = 11.sp) }
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = topUpAmountText.toIntOrNull() ?: 0
            if (amount > 0) {
              viewModel.topUpDriverBalance(amount)
              showTopUpDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
          Text("Пополнить")
        }
      },
      dismissButton = {
        TextButton(onClick = { showTopUpDialog = false }) {
          Text("Отмена", color = TextSecondary)
        }
      }
    )
  }

  // Rules Dialog
  if (showRulesDialog) {
    AlertDialog(
      onDismissRequest = { showRulesDialog = false },
      containerColor = SurfaceDark,
      title = { Text("Правила комиссии сервиса", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("1. Ставка комиссии составляет ровно 10% от стоимости каждого подтверждённого пассажирского места.", color = TextPrimary, fontSize = 12.sp)
          Text("2. Списание происходит один раз в момент завершения поездки.", color = TextSecondary, fontSize = 12.sp)
          Text("3. Если поездка отменена водителем или пассажиром, комиссия не списывается.", color = TextSecondary, fontSize = 12.sp)
          Text("4. В случае открытия спора статус переводится в «На проверке», и комиссия блокируется до решения арбитража.", color = TextSecondary, fontSize = 12.sp)
          Text("5. При наличной оплате пассажир передаёт сумму за проезд водителю, а комиссия списывается с текущего счета водителя.", color = TextSecondary, fontSize = 12.sp)
        }
      },
      confirmButton = {
        Button(
          onClick = { showRulesDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
          Text("Понятно")
        }
      }
    )
  }
}

@Composable
private fun TransactionCard(tx: WalletTransaction) {
  val dateStr = remember(tx.timestamp) {
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
  }
  val isPositive = tx.amount > 0

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
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isPositive) SuccessBg else WarningBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = null,
            tint = if (isPositive) SuccessGreen else WarningAmber,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(tx.type.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text(tx.description, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
          Text(dateStr, color = TextTertiary, fontSize = 10.sp)
        }
      }

      Text(
        text = (if (isPositive) "+" else "") + "${tx.amount} ₽",
        color = if (isPositive) SuccessGreen else WarningAmber,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
    }
  }
}

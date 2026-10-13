package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.UserRole
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatsListScreen(
  viewModel: AppViewModel,
  onOpenThread: (String) -> Unit
) {
  val currentUser = viewModel.getCurrentUser()
  val allChats by viewModel.chats.collectAsState()

  // Filter chats relevant to current user
  val myChats = allChats.filter {
    it.driverId == currentUser.id || it.passengerId == currentUser.id || currentUser.role == UserRole.ADMIN
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Column {
        Text(
          text = "Чаты и сообщения",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Общение между водителями и пассажирами по маршрутам",
          fontSize = 13.sp,
          color = TextSecondary
        )
      }
    }

    if (myChats.isEmpty()) {
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
            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("У вас пока нет активных диалогов", color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Диалоги создаются при бронировании поездки или запросе у водителя.", color = TextSecondary, fontSize = 12.sp)
          }
        }
      }
    } else {
      items(myChats, key = { it.id }) { thread ->
        ChatThreadItem(
          thread = thread,
          currentUserId = currentUser.id,
          onClick = { onOpenThread(thread.id) }
        )
      }
    }
  }
}

@Composable
private fun ChatThreadItem(
  thread: ChatThread,
  currentUserId: String,
  onClick: () -> Unit
) {
  val otherName = if (thread.driverId == currentUserId) thread.passengerName else thread.driverName
  val lastMsg = thread.messages.lastOrNull()
  val timeStr = if (lastMsg != null) {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastMsg.timestamp))
  } else ""

  Surface(
    color = SurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(BlueDark),
        contentAlignment = Alignment.Center
      ) {
        Text(otherName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(otherName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(timeStr, color = TextTertiary, fontSize = 11.sp)
        }

        Text(
          text = thread.routeTitle,
          color = BlueSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = lastMsg?.text ?: "Сообщений нет",
          color = TextSecondary,
          fontSize = 12.sp,
          maxLines = 1
        )
      }

      if (thread.unreadCount > 0) {
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(BluePrimary),
          contentAlignment = Alignment.Center
        ) {
          Text("${thread.unreadCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun ChatConversationScreen(
  viewModel: AppViewModel,
  chatId: String,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  val chats by viewModel.chats.collectAsState()
  val thread = chats.find { it.id == chatId }
  val currentUser = viewModel.getCurrentUser()

  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Scroll to bottom when messages update
  LaunchedEffect(thread?.messages?.size) {
    if ((thread?.messages?.size ?: 0) > 0) {
      listState.animateScrollToItem((thread?.messages?.size ?: 1) - 1)
    }
  }

  if (thread == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Диалог не найден", color = TextSecondary)
    }
    return
  }

  val otherName = if (thread.driverId == currentUser.id) thread.passengerName else thread.driverName

  Scaffold(
    containerColor = BackgroundDark,
    topBar = {
      Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
          }

          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(BluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(otherName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(otherName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(thread.routeTitle, color = BlueSecondary, fontSize = 11.sp, maxLines = 1)
          }
        }
      }
    },
    bottomBar = {
      Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
        modifier = Modifier.navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Напишите сообщение...", color = TextTertiary, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = Color(0xFF131B2E),
              unfocusedContainerColor = Color(0xFF131B2E),
              focusedBorderColor = BluePrimary,
              unfocusedBorderColor = SurfaceBorderDark
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.weight(1f),
            maxLines = 3
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (inputText.isNotBlank()) {
                viewModel.sendMessage(chatId, inputText)
                inputText = ""
              }
            },
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(if (inputText.isNotBlank()) BluePrimary else Color(0xFF283548))
          ) {
            Icon(Icons.Default.Send, contentDescription = "Отправить", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(thread.messages, key = { it.id }) { msg ->
        val isMine = msg.senderId == currentUser.id || msg.isMine
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
          Surface(
            color = if (isMine) BluePrimary else SurfaceDark,
            shape = RoundedCornerShape(
              topStart = 12.dp,
              topEnd = 12.dp,
              bottomStart = if (isMine) 12.dp else 2.dp,
              bottomEnd = if (isMine) 2.dp else 12.dp
            ),
            border = if (!isMine) androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark) else null,
            modifier = Modifier.widthIn(max = 280.dp)
          ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
              if (!isMine) {
                Text(msg.senderName, color = BlueSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
              Text(
                text = msg.text,
                color = Color.White,
                fontSize = 13.sp
              )
              Text(
                text = timeStr,
                color = if (isMine) Color(0xFFBFDBFE) else TextTertiary,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.End)
              )
            }
          }
        }
      }
    }
  }
}

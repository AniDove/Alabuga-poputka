package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

  private val storage = AppStorage(application)

  private val _users = MutableStateFlow<List<User>>(emptyList())
  val users: StateFlow<List<User>> = _users.asStateFlow()

  private val _currentUserId = MutableStateFlow("user_pass_1")
  val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

  private val _activeRole = MutableStateFlow(UserRole.PASSENGER)
  val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

  private val _rides = MutableStateFlow<List<Ride>>(emptyList())
  val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

  private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

  private val _chats = MutableStateFlow<List<ChatThread>>(emptyList())
  val chats: StateFlow<List<ChatThread>> = _chats.asStateFlow()

  private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
  val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

  private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())
  val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

  private val _locations = MutableStateFlow<List<LocationPoint>>(emptyList())
  val locations: StateFlow<List<LocationPoint>> = _locations.asStateFlow()

  // Navigation State
  private val _currentScreen = MutableStateFlow("search")
  val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

  private val _selectedChatId = MutableStateFlow<String?>(null)
  val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  init {
    loadSavedData()
  }

  private fun loadSavedData() {
    val state = storage.loadInitialState()
    _users.value = state.users
    _currentUserId.value = state.currentUserId
    _rides.value = state.rides
    _bookings.value = state.bookings
    _chats.value = state.chats
    _notifications.value = state.notifications
    _transactions.value = state.transactions
    _complaints.value = state.complaints
    _locations.value = state.locations

    val curUser = state.users.find { it.id == state.currentUserId }
    if (curUser != null) {
      _activeRole.value = curUser.role
      when (curUser.role) {
        UserRole.PASSENGER -> _currentScreen.value = "search"
        UserRole.DRIVER -> _currentScreen.value = "driver_rides"
        UserRole.ADMIN -> _currentScreen.value = "admin"
      }
    }
  }

  private fun persist() {
    storage.saveState(
      AppStorage.State(
        users = _users.value,
        currentUserId = _currentUserId.value,
        rides = _rides.value,
        bookings = _bookings.value,
        chats = _chats.value,
        notifications = _notifications.value,
        transactions = _transactions.value,
        complaints = _complaints.value,
        locations = _locations.value
      )
    )
  }

  fun getCurrentUser(): User {
    return _users.value.find { it.id == _currentUserId.value }
      ?: User(
        id = _currentUserId.value,
        name = "Пользователь",
        phone = "+7 (999) 000-00-00",
        role = _activeRole.value,
        rating = 5.0,
        tripsCount = 0
      )
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  fun navigateTo(screen: String) {
    _currentScreen.value = screen
  }

  fun switchRole(role: UserRole) {
    _activeRole.value = role
    when (role) {
      UserRole.PASSENGER -> {
        _currentUserId.value = "user_pass_1"
        _currentScreen.value = "search"
        showToast("Переключено на роль: Пассажир")
      }
      UserRole.DRIVER -> {
        _currentUserId.value = "user_driver_1"
        _currentScreen.value = "driver_rides"
        showToast("Переключено на роль: Водитель")
      }
      UserRole.ADMIN -> {
        _currentUserId.value = "user_admin"
        _currentScreen.value = "admin"
        showToast("Переключено на роль: Администратор")
      }
    }
    persist()
  }

  fun switchUser(userId: String) {
    val u = _users.value.find { it.id == userId } ?: return
    _currentUserId.value = u.id
    _activeRole.value = u.role
    when (u.role) {
      UserRole.PASSENGER -> _currentScreen.value = "search"
      UserRole.DRIVER -> _currentScreen.value = "driver_rides"
      UserRole.ADMIN -> _currentScreen.value = "admin"
    }
    showToast("Активный профиль: ${u.name} (${u.role.title})")
    persist()
  }

  // --- PASSENGER ACTIONS ---

  fun bookRide(
    rideId: String,
    seatsCount: Int,
    paymentMethod: PaymentMethod,
    notes: String
  ): Boolean {
    val user = getCurrentUser()
    val ride = _rides.value.find { it.id == rideId }

    if (ride == null) {
      showToast("Ошибка: поездка не найдена")
      return false
    }

    if (ride.driverId == user.id) {
      showToast("Нельзя забронировать собственную поездку")
      return false
    }

    if (ride.status != RideStatus.ACTIVE) {
      showToast("Поездка уже недоступна для бронирования")
      return false
    }

    if (seatsCount > ride.availableSeats) {
      showToast("Недостаточно свободных мест (доступно: ${ride.availableSeats})")
      return false
    }

    val existing = _bookings.value.find {
      it.rideId == rideId && it.passengerId == user.id &&
          (it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED)
    }
    if (existing != null) {
      showToast("У вас уже есть активная заявка на эту поездку")
      return false
    }

    val totalPrice = seatsCount * ride.pricePerSeat
    val commission = (totalPrice * 0.10).toInt() // 10%

    val newBooking = Booking(
      id = "book_" + UUID.randomUUID().toString().take(8),
      rideId = rideId,
      passengerId = user.id,
      passengerName = user.name,
      passengerPhone = user.phone,
      seatsCount = seatsCount,
      pricePerSeat = ride.pricePerSeat,
      totalPrice = totalPrice,
      commissionAmount = commission,
      paymentMethod = paymentMethod,
      status = BookingStatus.PENDING,
      notes = notes,
      createdAt = System.currentTimeMillis()
    )

    _bookings.value = listOf(newBooking) + _bookings.value

    // Notify driver
    val notif = AppNotification(
      id = "notif_" + UUID.randomUUID().toString().take(8),
      userId = ride.driverId,
      title = "Входящая заявка",
      message = "Пассажир ${user.name} запросил $seatsCount мест (${ride.fromLocation} → ${ride.toLocation}).",
      timestamp = System.currentTimeMillis(),
      isRead = false,
      type = NotificationType.REQUEST,
      targetRideId = ride.id
    )
    _notifications.value = listOf(notif) + _notifications.value

    // Ensure chat thread exists
    ensureChatThread(ride, user)

    persist()
    showToast("Заявка успешно отправлена водителю!")
    return true
  }

  fun cancelBooking(bookingId: String) {
    val b = _bookings.value.find { it.id == bookingId } ?: return
    val ride = _rides.value.find { it.id == b.rideId }

    // If was confirmed, return seats
    if (b.status == BookingStatus.CONFIRMED && ride != null) {
      _rides.value = _rides.value.map {
        if (it.id == ride.id) {
          it.copy(availableSeats = (it.availableSeats + b.seatsCount).coerceAtMost(it.totalSeats))
        } else it
      }
    }

    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CANCELLED) else it
    }

    // Notify driver
    if (ride != null) {
      val notif = AppNotification(
        id = "notif_" + UUID.randomUUID().toString().take(8),
        userId = ride.driverId,
        title = "Отмена бронирования",
        message = "Пассажир ${b.passengerName} отменил заявку на ${b.seatsCount} мест (${ride.fromLocation} → ${ride.toLocation}).",
        timestamp = System.currentTimeMillis(),
        isRead = false,
        type = NotificationType.CANCELLATION,
        targetRideId = ride.id
      )
      _notifications.value = listOf(notif) + _notifications.value
    }

    persist()
    showToast("Бронирование отменено")
  }

  // --- DRIVER ACTIONS ---

  fun acceptBooking(bookingId: String) {
    val b = _bookings.value.find { it.id == bookingId } ?: return
    val ride = _rides.value.find { it.id == b.rideId } ?: return

    if (b.seatsCount > ride.availableSeats) {
      showToast("Недостаточно свободных мест для подтверждения заявки!")
      return
    }

    // Recalculate seats
    _rides.value = _rides.value.map {
      if (it.id == ride.id) {
        it.copy(availableSeats = (it.availableSeats - b.seatsCount).coerceAtLeast(0))
      } else it
    }

    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.CONFIRMED) else it
    }

    // Notify passenger
    val notif = AppNotification(
      id = "notif_" + UUID.randomUUID().toString().take(8),
      userId = b.passengerId,
      title = "Заявка подтверждена",
      message = "Водитель ${ride.driverName} подтвердил ваше бронирование «${ride.fromLocation} → ${ride.toLocation}»!",
      timestamp = System.currentTimeMillis(),
      isRead = false,
      type = NotificationType.CONFIRMATION,
      targetRideId = ride.id
    )
    _notifications.value = listOf(notif) + _notifications.value

    persist()
    showToast("Заявка подтверждена! Места обновлены.")
  }

  fun rejectBooking(bookingId: String) {
    val b = _bookings.value.find { it.id == bookingId } ?: return
    val ride = _rides.value.find { it.id == b.rideId }

    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(status = BookingStatus.REJECTED) else it
    }

    // Notify passenger
    val notif = AppNotification(
      id = "notif_" + UUID.randomUUID().toString().take(8),
      userId = b.passengerId,
      title = "Заявка отклонена",
      message = "Водитель отклонил ваш запрос на поездку ${ride?.fromLocation ?: ""} → ${ride?.toLocation ?: ""}.",
      timestamp = System.currentTimeMillis(),
      isRead = false,
      type = NotificationType.REJECTION,
      targetRideId = ride?.id
    )
    _notifications.value = listOf(notif) + _notifications.value

    persist()
    showToast("Заявка отклонена")
  }

  fun createRide(
    from: String,
    to: String,
    date: String,
    time: String,
    seats: Int,
    price: Int,
    comment: String
  ): Boolean {
    val driver = getCurrentUser()
    if (from.isBlank() || to.isBlank() || time.isBlank() || seats <= 0 || price <= 0) {
      showToast("Заполните все обязательные поля корректно")
      return false
    }

    val newRide = Ride(
      id = "ride_" + UUID.randomUUID().toString().take(8),
      driverId = driver.id,
      driverName = driver.name,
      driverCar = if (driver.carModel.isNotBlank()) "${driver.carModel} (${driver.carNumber})" else "Автомобиль водителя",
      driverRating = driver.rating,
      driverPhone = driver.phone,
      fromLocation = from.trim(),
      toLocation = to.trim(),
      date = date.trim(),
      time = time.trim(),
      totalSeats = seats,
      availableSeats = seats,
      pricePerSeat = price,
      comment = comment.trim(),
      status = RideStatus.ACTIVE,
      isCommissionCharged = false,
      createdAt = System.currentTimeMillis()
    )

    _rides.value = listOf(newRide) + _rides.value
    persist()
    showToast("Поездка успешно создана и опубликована!")
    return true
  }

  fun cancelRide(rideId: String) {
    val ride = _rides.value.find { it.id == rideId } ?: return

    _rides.value = _rides.value.map {
      if (it.id == rideId) it.copy(status = RideStatus.CANCELLED) else it
    }

    // Cancel related active bookings
    val relatedBookings = _bookings.value.filter {
      it.rideId == rideId && (it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED)
    }

    _bookings.value = _bookings.value.map {
      if (it.rideId == rideId && (it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED)) {
        it.copy(status = BookingStatus.CANCELLED)
      } else it
    }

    // Notify affected passengers
    relatedBookings.forEach { b ->
      val notif = AppNotification(
        id = "notif_" + UUID.randomUUID().toString().take(8),
        userId = b.passengerId,
        title = "Поездка отменена водителем",
        message = "Водитель отменил поездку «${ride.fromLocation} → ${ride.toLocation}». Бронирование аннулировано.",
        timestamp = System.currentTimeMillis(),
        isRead = false,
        type = NotificationType.CANCELLATION,
        targetRideId = rideId
      )
      _notifications.value = listOf(notif) + _notifications.value
    }

    persist()
    showToast("Поездка отменена. Комиссия не начисляется.")
  }

  // --- COMPLETION & COMMISSION SCENARIO ---

  fun markRideCompleted(rideId: String) {
    val ride = _rides.value.find { it.id == rideId } ?: return

    if (ride.status == RideStatus.COMPLETED) {
      showToast("Поездка уже завершена")
      return
    }

    // Update ride status
    _rides.value = _rides.value.map {
      if (it.id == rideId) it.copy(status = RideStatus.COMPLETED) else it
    }

    // Mark confirmed bookings as completed
    val confirmedBookings = _bookings.value.filter {
      it.rideId == rideId && it.status == BookingStatus.CONFIRMED
    }

    _bookings.value = _bookings.value.map {
      if (it.rideId == rideId && it.status == BookingStatus.CONFIRMED) {
        it.copy(status = BookingStatus.COMPLETED)
      } else it
    }

    // Calculate commission once
    if (!ride.isCommissionCharged && confirmedBookings.isNotEmpty()) {
      var totalCommission = 0
      confirmedBookings.forEach { b ->
        totalCommission += b.commissionAmount
      }

      if (totalCommission > 0) {
        // Deduct from driver's balance
        _users.value = _users.value.map { u ->
          if (u.id == ride.driverId) {
            u.copy(
              balance = u.balance - totalCommission,
              tripsCount = u.tripsCount + 1
            )
          } else if (confirmedBookings.any { it.passengerId == u.id }) {
            u.copy(tripsCount = u.tripsCount + 1)
          } else u
        }

        // Add transaction
        val tx = WalletTransaction(
          id = "tx_" + UUID.randomUUID().toString().take(8),
          driverId = ride.driverId,
          type = TransactionType.COMMISSION_FEE,
          amount = -totalCommission,
          description = "Комиссия 10% за поездку ${ride.fromLocation} → ${ride.toLocation} (${confirmedBookings.sumOf { it.seatsCount }} мест)",
          timestamp = System.currentTimeMillis(),
          rideId = ride.id
        )
        _transactions.value = listOf(tx) + _transactions.value

        // Notify driver
        val driverNotif = AppNotification(
          id = "notif_" + UUID.randomUUID().toString().take(8),
          userId = ride.driverId,
          title = "Поездка завершена",
          message = "Поездка завершена. Списана комиссия сервиса 10%: $totalCommission ₽.",
          timestamp = System.currentTimeMillis(),
          isRead = false,
          type = NotificationType.BALANCE,
          targetRideId = ride.id
        )
        _notifications.value = listOf(driverNotif) + _notifications.value
      }

      // Mark ride commission as charged
      _rides.value = _rides.value.map {
        if (it.id == rideId) it.copy(isCommissionCharged = true) else it
      }
    }

    // Notify passengers
    confirmedBookings.forEach { b ->
      val pNotif = AppNotification(
        id = "notif_" + UUID.randomUUID().toString().take(8),
        userId = b.passengerId,
        title = "Поездка завершена",
        message = "Поездка «${ride.fromLocation} → ${ride.toLocation}» успешно завершена. Спасибо за поездку!",
        timestamp = System.currentTimeMillis(),
        isRead = false,
        type = NotificationType.RIDE_UPDATE,
        targetRideId = ride.id
      )
      _notifications.value = listOf(pNotif) + _notifications.value
    }

    persist()
    showToast("Поездка завершена! Баланс и комиссия обновлены.")
  }

  fun disputeRide(rideId: String, reason: String) {
    val ride = _rides.value.find { it.id == rideId } ?: return
    val user = getCurrentUser()

    _rides.value = _rides.value.map {
      if (it.id == rideId) it.copy(status = RideStatus.UNDER_REVIEW) else it
    }

    // Submit complaint
    submitComplaint(
      targetType = "RIDE",
      targetId = rideId,
      targetTitle = "Поездка ${ride.fromLocation} → ${ride.toLocation}",
      reason = "Спор по завершению: $reason",
      description = "Пользователь ${user.name} открыл спор по завершению поездки. Статус переведен в «На проверке», комиссия заморожена."
    )

    showToast("Спор открыт! Поездка переведена в статус «На проверке».")
    persist()
  }

  // --- BALANCE & WALLET ---

  fun topUpDriverBalance(amount: Int) {
    if (amount <= 0) {
      showToast("Введите корректную сумму пополнения")
      return
    }

    val curUser = getCurrentUser()
    _users.value = _users.value.map {
      if (it.id == curUser.id) it.copy(balance = it.balance + amount) else it
    }

    val tx = WalletTransaction(
      id = "tx_" + UUID.randomUUID().toString().take(8),
      driverId = curUser.id,
      type = TransactionType.DEMO_TOPUP,
      amount = amount,
      description = "Тестовое пополнение демо-баланса",
      timestamp = System.currentTimeMillis()
    )
    _transactions.value = listOf(tx) + _transactions.value

    val notif = AppNotification(
      id = "notif_" + UUID.randomUUID().toString().take(8),
      userId = curUser.id,
      title = "Демо-баланс пополнен",
      message = "Баланс успешно пополнен на $amount ₽ (тестовая операция).",
      timestamp = System.currentTimeMillis(),
      isRead = false,
      type = NotificationType.BALANCE
    )
    _notifications.value = listOf(notif) + _notifications.value

    persist()
    showToast("Баланс пополнен на $amount ₽ (демо-режим)")
  }

  // --- CHAT ACTIONS ---

  private fun ensureChatThread(ride: Ride, passenger: User): ChatThread {
    val existing = _chats.value.find { it.rideId == ride.id && it.passengerId == passenger.id }
    if (existing != null) return existing

    val newChat = ChatThread(
      id = "chat_" + UUID.randomUUID().toString().take(8),
      rideId = ride.id,
      driverId = ride.driverId,
      passengerId = passenger.id,
      passengerName = passenger.name,
      driverName = ride.driverName,
      routeTitle = "${ride.fromLocation} → ${ride.toLocation} (${ride.time})",
      unreadCount = 0,
      messages = emptyList()
    )
    _chats.value = listOf(newChat) + _chats.value
    return newChat
  }

  fun openChatForRide(rideId: String) {
    val user = getCurrentUser()
    val ride = _rides.value.find { it.id == rideId } ?: return

    val chat = if (user.role == UserRole.PASSENGER) {
      ensureChatThread(ride, user)
    } else {
      _chats.value.find { it.rideId == rideId } ?: ensureChatThread(ride, user)
    }
    _selectedChatId.value = chat.id
    _currentScreen.value = "chat_thread"
  }

  fun selectChat(chatId: String) {
    _selectedChatId.value = chatId
    // mark thread as read
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(unreadCount = 0) else it
    }
    _currentScreen.value = "chat_thread"
    persist()
  }

  fun closeChatThread() {
    _selectedChatId.value = null
    _currentScreen.value = "chats"
  }

  fun sendMessage(chatId: String, text: String) {
    if (text.isBlank()) return
    val user = getCurrentUser()

    val newMsg = ChatMessage(
      id = "msg_" + UUID.randomUUID().toString().take(8),
      senderId = user.id,
      senderName = user.name,
      text = text.trim(),
      timestamp = System.currentTimeMillis(),
      isMine = true
    )

    _chats.value = _chats.value.map { c ->
      if (c.id == chatId) {
        c.copy(
          messages = c.messages + newMsg,
          unreadCount = 0
        )
      } else c
    }

    persist()
  }

  // --- NOTIFICATIONS ---

  fun markAllNotificationsRead() {
    val curId = _currentUserId.value
    _notifications.value = _notifications.value.map {
      if (it.userId == curId) it.copy(isRead = true) else it
    }
    persist()
    showToast("Все уведомления прочитаны")
  }

  // --- COMPLAINTS & SAFETY ---

  fun submitComplaint(
    targetType: String,
    targetId: String,
    targetTitle: String,
    reason: String,
    description: String
  ) {
    val user = getCurrentUser()
    val cmp = Complaint(
      id = "comp_" + UUID.randomUUID().toString().take(8),
      authorId = user.id,
      authorName = user.name,
      targetType = targetType,
      targetId = targetId,
      targetTitle = targetTitle,
      reason = reason,
      description = description,
      status = ComplaintStatus.PENDING,
      createdAt = System.currentTimeMillis()
    )
    _complaints.value = listOf(cmp) + _complaints.value
    persist()
    showToast("Жалоба принята и направлена администратору")
  }

  // --- ADMIN ACTIONS ---

  fun adminResolveComplaint(complaintId: String, status: ComplaintStatus) {
    _complaints.value = _complaints.value.map {
      if (it.id == complaintId) it.copy(status = status) else it
    }
    persist()
    showToast("Статус жалобы обновлен: ${status.title}")
  }

  fun adminToggleBlockUser(userId: String) {
    _users.value = _users.value.map {
      if (it.id == userId) it.copy(isBlocked = !it.isBlocked) else it
    }
    persist()
    showToast("Статус пользователя изменен")
  }

  fun adminCancelRide(rideId: String) {
    cancelRide(rideId)
  }

  fun adminAddLocation(name: String, category: String, desc: String) {
    if (name.isBlank()) return
    val loc = LocationPoint(
      id = "loc_" + UUID.randomUUID().toString().take(8),
      name = name.trim(),
      category = category.trim(),
      description = desc.trim()
    )
    _locations.value = _locations.value + loc
    persist()
    showToast("Точка «$name» добавлена в справочник!")
  }

  fun resetAllDemoData() {
    val fresh = storage.resetToDefaults()
    _users.value = fresh.users
    _currentUserId.value = fresh.currentUserId
    _rides.value = fresh.rides
    _bookings.value = fresh.bookings
    _chats.value = fresh.chats
    _notifications.value = fresh.notifications
    _transactions.value = fresh.transactions
    _complaints.value = fresh.complaints
    _locations.value = fresh.locations

    _activeRole.value = UserRole.PASSENGER
    _currentScreen.value = "search"
    _selectedChatId.value = null
    showToast("Демо-данные сброшены к начальным значениям!")
  }

  fun updateProfile(name: String, phone: String, carModel: String, carNumber: String) {
    val curId = _currentUserId.value
    _users.value = _users.value.map {
      if (it.id == curId) {
        it.copy(
          name = name.trim(),
          phone = phone.trim(),
          carModel = carModel.trim(),
          carNumber = carNumber.trim()
        )
      } else it
    }
    persist()
    showToast("Профиль обновлен")
  }
}

package com.example.model

enum class UserRole(val title: String) {
  PASSENGER("Пассажир"),
  DRIVER("Водитель"),
  ADMIN("Администратор")
}

enum class RideStatus(val title: String) {
  ACTIVE("Активна"),
  IN_PROGRESS("В пути"),
  COMPLETED("Завершена"),
  UNDER_REVIEW("На проверке"),
  CANCELLED("Отменена")
}

enum class BookingStatus(val title: String) {
  PENDING("Ожидает"),
  CONFIRMED("Подтверждено"),
  REJECTED("Отклонено"),
  CANCELLED("Отменено"),
  COMPLETED("Завершено")
}

enum class PaymentMethod(val title: String) {
  CASH("Наличными водителю"),
  ONLINE_DEMO("Онлайн (демо)")
}

enum class NotificationType {
  REQUEST,
  CONFIRMATION,
  REJECTION,
  CANCELLATION,
  RIDE_UPDATE,
  CHAT,
  BALANCE
}

enum class TransactionType(val title: String) {
  COMMISSION_FEE("Комиссия сервиса 10%"),
  DEMO_TOPUP("Демо-пополнение"),
  ONLINE_PAYMENT("Онлайн оплата поездки")
}

enum class ComplaintStatus(val title: String) {
  PENDING("На рассмотрении"),
  RESOLVED("Удовлетворена"),
  REJECTED("Отклонена")
}

data class User(
  val id: String,
  val name: String,
  val phone: String,
  val role: UserRole,
  val rating: Double,
  val tripsCount: Int,
  val carModel: String = "",
  val carNumber: String = "",
  val isBlocked: Boolean = false,
  val balance: Int = 0 // Driver's demo balance in rubles
)

data class Ride(
  val id: String,
  val driverId: String,
  val driverName: String,
  val driverCar: String,
  val driverRating: Double,
  val driverPhone: String,
  val fromLocation: String,
  val toLocation: String,
  val date: String,
  val time: String,
  val totalSeats: Int,
  val availableSeats: Int,
  val pricePerSeat: Int,
  val comment: String,
  val status: RideStatus = RideStatus.ACTIVE,
  val isCommissionCharged: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class Booking(
  val id: String,
  val rideId: String,
  val passengerId: String,
  val passengerName: String,
  val passengerPhone: String,
  val seatsCount: Int,
  val pricePerSeat: Int,
  val totalPrice: Int,
  val commissionAmount: Int, // 10%
  val paymentMethod: PaymentMethod,
  val status: BookingStatus = BookingStatus.PENDING,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
  val id: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val timestamp: Long,
  val isMine: Boolean = false
)

data class ChatThread(
  val id: String,
  val rideId: String,
  val driverId: String,
  val passengerId: String,
  val passengerName: String,
  val driverName: String,
  val routeTitle: String,
  val unreadCount: Int = 0,
  val messages: List<ChatMessage> = emptyList()
)

data class AppNotification(
  val id: String,
  val userId: String,
  val title: String,
  val message: String,
  val timestamp: Long,
  val isRead: Boolean = false,
  val type: NotificationType,
  val targetRideId: String? = null
)

data class WalletTransaction(
  val id: String,
  val driverId: String,
  val type: TransactionType,
  val amount: Int, // Positive or negative
  val description: String,
  val timestamp: Long,
  val rideId: String? = null
)

data class Complaint(
  val id: String,
  val authorId: String,
  val authorName: String,
  val targetType: String, // "RIDE" or "USER"
  val targetId: String,
  val targetTitle: String,
  val reason: String,
  val description: String,
  val status: ComplaintStatus = ComplaintStatus.PENDING,
  val createdAt: Long = System.currentTimeMillis()
)

data class LocationPoint(
  val id: String,
  val name: String,
  val category: String, // "Елабуга" or "ОЭЗ Алабуга"
  val description: String = ""
)

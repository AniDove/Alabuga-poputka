package com.example.data

import com.example.model.*

object DemoData {

  fun getDefaultLocations(): List<LocationPoint> = listOf(
    LocationPoint("loc_1", "Южный парк", "Елабуга", "Остановка возле главного входа в парк"),
    LocationPoint("loc_2", "ул. Королёва", "Елабуга", "Возле супермаркета «Магнит»"),
    LocationPoint("loc_3", "пр-т Нефтяников", "Елабуга", "Центральная остановка, ост. Мира"),
    LocationPoint("loc_4", "4-й микрорайон", "Елабуга", "У дома быта"),
    LocationPoint("loc_5", "ТЦ «Эссен»", "Елабуга", "Парковка со стороны автомойки"),
    LocationPoint("loc_6", "КПП-1 ОЭЗ Алабуга", "ОЭЗ Алабуга", "Главный пропускной пункт"),
    LocationPoint("loc_7", "Общежитие Синергия 1", "ОЭЗ Алабуга", "Корпус А, центральный вход"),
    LocationPoint("loc_8", "Общежитие Синергия 2", "ОЭЗ Алабуга", "Площадка перед корпусом 3"),
    LocationPoint("loc_9", "Завод Аурус", "ОЭЗ Алабуга", "Проходная автозавода Aurus"),
    LocationPoint("loc_10", "Завод Джошкуноз", "ОЭЗ Алабуга", "Северный въезд"),
    LocationPoint("loc_11", "Завод Кастамону", "ОЭЗ Алабуга", "Остановка для персонала"),
    LocationPoint("loc_12", "Завод Соллерс", "ОЭЗ Алабуга", "Центральное КПП")
  )

  fun getDefaultUsers(): List<User> = listOf(
    User(
      id = "user_pass_1",
      name = "Айдар Закиров",
      phone = "+7 (919) 678-90-12",
      role = UserRole.PASSENGER,
      rating = 5.0,
      tripsCount = 34
    ),
    User(
      id = "user_pass_2",
      name = "Диана Каримова",
      phone = "+7 (927) 456-78-90",
      role = UserRole.PASSENGER,
      rating = 4.9,
      tripsCount = 21
    ),
    User(
      id = "user_driver_1",
      name = "Руслан Шакиров",
      phone = "+7 (917) 234-56-78",
      role = UserRole.DRIVER,
      rating = 4.9,
      tripsCount = 142,
      carModel = "Kia Rio (белый)",
      carNumber = "А123ВС 116",
      balance = 750
    ),
    User(
      id = "user_driver_2",
      name = "Артур Галимов",
      phone = "+7 (905) 345-67-89",
      role = UserRole.DRIVER,
      rating = 4.8,
      tripsCount = 89,
      carModel = "Lada Vesta (серебро)",
      carNumber = "С789ХУ 16",
      balance = 420
    ),
    User(
      id = "user_admin",
      name = "Администратор Смены",
      phone = "+7 (85557) 5-90-00",
      role = UserRole.ADMIN,
      rating = 5.0,
      tripsCount = 0
    )
  )

  fun getDefaultRides(): List<Ride> = listOf(
    Ride(
      id = "ride_1",
      driverId = "user_driver_1",
      driverName = "Руслан Шакиров",
      driverCar = "Kia Rio (белый, А123ВС 116)",
      driverRating = 4.9,
      driverPhone = "+7 (917) 234-56-78",
      fromLocation = "Южный парк",
      toLocation = "Завод Аурус",
      date = "Сегодня",
      time = "07:15",
      totalSeats = 4,
      availableSeats = 3, // 1 seat booked by Айдар
      pricePerSeat = 300,
      comment = "Еду по пр-т Нефтяников. В салоне чисто, без курения. Багажник свободный.",
      status = RideStatus.ACTIVE
    ),
    Ride(
      id = "ride_2",
      driverId = "user_driver_2",
      driverName = "Артур Галимов",
      driverCar = "Lada Vesta (серебро, С789ХУ 16)",
      driverRating = 4.8,
      driverPhone = "+7 (905) 345-67-89",
      fromLocation = "ул. Королёва",
      toLocation = "КПП-1 ОЭЗ Алабуга",
      date = "Сегодня",
      time = "07:30",
      totalSeats = 3,
      availableSeats = 3, // 1 request pending from Диана
      pricePerSeat = 250,
      comment = "Быстро до КПП-1. Могу забрать у ТЦ Эссен по пути.",
      status = RideStatus.ACTIVE
    ),
    Ride(
      id = "ride_3",
      driverId = "user_driver_1",
      driverName = "Руслан Шакиров",
      driverCar = "Kia Rio (белый, А123ВС 116)",
      driverRating = 4.9,
      driverPhone = "+7 (917) 234-56-78",
      fromLocation = "Общежитие Синергия 1",
      toLocation = "Завод Джошкуноз",
      date = "Сегодня",
      time = "08:00",
      totalSeats = 4,
      availableSeats = 4,
      pricePerSeat = 200,
      comment = "Для студентов и сотрудников ОЭЗ. Сбор у главного входа.",
      status = RideStatus.ACTIVE
    ),
    Ride(
      id = "ride_4",
      driverId = "user_driver_1",
      driverName = "Руслан Шакиров",
      driverCar = "Kia Rio (белый, А123ВС 116)",
      driverRating = 4.9,
      driverPhone = "+7 (917) 234-56-78",
      fromLocation = "Завод Аурус",
      toLocation = "Южный парк",
      date = "Сегодня",
      time = "17:15",
      totalSeats = 4,
      availableSeats = 4,
      pricePerSeat = 300,
      comment = "Обратный рейс после смены. Забираю с центральной проходной.",
      status = RideStatus.ACTIVE
    ),
    Ride(
      id = "ride_5",
      driverId = "user_driver_2",
      driverName = "Артур Галимов",
      driverCar = "Lada Vesta (серебро, С789ХУ 16)",
      driverRating = 4.8,
      driverPhone = "+7 (905) 345-67-89",
      fromLocation = "КПП-1 ОЭЗ Алабуга",
      toLocation = "4-й микрорайон",
      date = "Сегодня",
      time = "18:00",
      totalSeats = 3,
      availableSeats = 3,
      pricePerSeat = 250,
      comment = "Вечерний маршрут в город. Еду через 10-й микрорайон.",
      status = RideStatus.ACTIVE
    )
  )

  fun getDefaultBookings(): List<Booking> = listOf(
    Booking(
      id = "book_1",
      rideId = "ride_1",
      passengerId = "user_pass_1",
      passengerName = "Айдар Закиров",
      passengerPhone = "+7 (919) 678-90-12",
      seatsCount = 1,
      pricePerSeat = 300,
      totalPrice = 300,
      commissionAmount = 30, // 10%
      paymentMethod = PaymentMethod.CASH,
      status = BookingStatus.CONFIRMED,
      notes = "Буду ждать у аптеки"
    ),
    Booking(
      id = "book_2",
      rideId = "ride_2",
      passengerId = "user_pass_2",
      passengerName = "Диана Каримова",
      passengerPhone = "+7 (927) 456-78-90",
      seatsCount = 1,
      pricePerSeat = 250,
      totalPrice = 250,
      commissionAmount = 25, // 10%
      paymentMethod = PaymentMethod.ONLINE_DEMO,
      status = BookingStatus.PENDING,
      notes = "Без сдачи"
    )
  )

  fun getDefaultChats(): List<ChatThread> = listOf(
    ChatThread(
      id = "chat_1",
      rideId = "ride_1",
      driverId = "user_driver_1",
      passengerId = "user_pass_1",
      passengerName = "Айдар Закиров",
      driverName = "Руслан Шакиров",
      routeTitle = "Южный парк → Завод Аурус (07:15)",
      unreadCount = 0,
      messages = listOf(
        ChatMessage(
          id = "msg_1",
          senderId = "user_pass_1",
          senderName = "Айдар Закиров",
          text = "Доброе утро! Подскажите, у остановки возле аптеки удобно будет забрать?",
          timestamp = System.currentTimeMillis() - 3600000,
          isMine = true
        ),
        ChatMessage(
          id = "msg_2",
          senderId = "user_driver_1",
          senderName = "Руслан Шакиров",
          text = "Здравствуйте, Айдар! Да, отлично, в 07:15 буду там с белым Rio.",
          timestamp = System.currentTimeMillis() - 3000000,
          isMine = false
        )
      )
    ),
    ChatThread(
      id = "chat_2",
      rideId = "ride_2",
      driverId = "user_driver_2",
      passengerId = "user_pass_2",
      passengerName = "Диана Каримова",
      driverName = "Артур Галимов",
      routeTitle = "ул. Королёва → КПП-1 ОЭЗ Алабуга (07:30)",
      unreadCount = 1,
      messages = listOf(
        ChatMessage(
          id = "msg_3",
          senderId = "user_pass_2",
          senderName = "Диана Каримова",
          text = "Добрый день, заявку отправила. До КПП-1 успеем к 07:55?",
          timestamp = System.currentTimeMillis() - 1200000,
          isMine = false
        )
      )
    )
  )

  fun getDefaultNotifications(): List<AppNotification> = listOf(
    AppNotification(
      id = "notif_1",
      userId = "user_pass_1",
      title = "Заявка подтверждена",
      message = "Водитель Руслан подтвердил ваше бронирование «Южный парк → Завод Аурус» на 07:15.",
      timestamp = System.currentTimeMillis() - 4000000,
      isRead = true,
      type = NotificationType.CONFIRMATION,
      targetRideId = "ride_1"
    ),
    AppNotification(
      id = "notif_2",
      userId = "user_driver_1",
      title = "Новая бронь",
      message = "Пассажир Айдар Закиров забронировал 1 место на поездку Южный парк → Завод Аурус.",
      timestamp = System.currentTimeMillis() - 4100000,
      isRead = false,
      type = NotificationType.REQUEST,
      targetRideId = "ride_1"
    ),
    AppNotification(
      id = "notif_3",
      userId = "user_driver_2",
      title = "Входящая заявка",
      message = "Пассажир Диана Каримова отправила запрос на 1 место на рейс «ул. Королёва → КПП-1».",
      timestamp = System.currentTimeMillis() - 1200000,
      isRead = false,
      type = NotificationType.REQUEST,
      targetRideId = "ride_2"
    ),
    AppNotification(
      id = "notif_4",
      userId = "user_driver_1",
      title = "Комиссия сервиса",
      message = "Комиссия сервиса составляет 10% от стоимости каждого подтверждённого пассажирского места.",
      timestamp = System.currentTimeMillis() - 86400000,
      isRead = true,
      type = NotificationType.BALANCE
    )
  )

  fun getDefaultTransactions(): List<WalletTransaction> = listOf(
    WalletTransaction(
      id = "tx_1",
      driverId = "user_driver_1",
      type = TransactionType.DEMO_TOPUP,
      amount = 1000,
      description = "Демо-пополнение баланса через панель",
      timestamp = System.currentTimeMillis() - 86400000 * 2
    ),
    WalletTransaction(
      id = "tx_2",
      driverId = "user_driver_1",
      type = TransactionType.COMMISSION_FEE,
      amount = -250,
      description = "Комиссия 10% за завершенные поездки смены ОЭЗ",
      timestamp = System.currentTimeMillis() - 86400000
    )
  )

  fun getDefaultComplaints(): List<Complaint> = listOf(
    Complaint(
      id = "comp_1",
      authorId = "user_pass_2",
      authorName = "Диана Каримова",
      targetType = "USER",
      targetId = "user_driver_2",
      targetTitle = "Водитель Артур Галимов",
      reason = "Опоздание на рейс",
      description = "Вчера водитель задержался на 15 минут у ТЦ Эссен, чуть не опоздала на смену в ОЭЗ.",
      status = ComplaintStatus.PENDING
    )
  )
}

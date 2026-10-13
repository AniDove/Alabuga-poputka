package com.example.data

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AppStorage(private val context: Context) {

  private val fileName = "poputka_demo_data_v1.json"

  data class State(
    val users: List<User>,
    val currentUserId: String,
    val rides: List<Ride>,
    val bookings: List<Booking>,
    val chats: List<ChatThread>,
    val notifications: List<AppNotification>,
    val transactions: List<WalletTransaction>,
    val complaints: List<Complaint>,
    val locations: List<LocationPoint>
  )

  fun loadInitialState(): State {
    val file = File(context.filesDir, fileName)
    if (!file.exists()) {
      val fresh = getFreshState()
      saveState(fresh)
      return fresh
    }

    return try {
      val jsonString = file.readText()
      parseState(JSONObject(jsonString))
    } catch (e: Exception) {
      e.printStackTrace()
      val fresh = getFreshState()
      saveState(fresh)
      fresh
    }
  }

  fun saveState(state: State) {
    try {
      val json = serializeState(state)
      val file = File(context.filesDir, fileName)
      file.writeText(json.toString(2))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun resetToDefaults(): State {
    val fresh = getFreshState()
    saveState(fresh)
    return fresh
  }

  private fun getFreshState(): State {
    return State(
      users = DemoData.getDefaultUsers(),
      currentUserId = "user_pass_1", // Default starting as passenger Айдар Закиров
      rides = DemoData.getDefaultRides(),
      bookings = DemoData.getDefaultBookings(),
      chats = DemoData.getDefaultChats(),
      notifications = DemoData.getDefaultNotifications(),
      transactions = DemoData.getDefaultTransactions(),
      complaints = DemoData.getDefaultComplaints(),
      locations = DemoData.getDefaultLocations()
    )
  }

  private fun serializeState(state: State): JSONObject {
    val root = JSONObject()

    val usersArr = JSONArray()
    state.users.forEach { u ->
      val obj = JSONObject()
      obj.put("id", u.id)
      obj.put("name", u.name)
      obj.put("phone", u.phone)
      obj.put("role", u.role.name)
      obj.put("rating", u.rating)
      obj.put("tripsCount", u.tripsCount)
      obj.put("carModel", u.carModel)
      obj.put("carNumber", u.carNumber)
      obj.put("isBlocked", u.isBlocked)
      obj.put("balance", u.balance)
      usersArr.put(obj)
    }
    root.put("users", usersArr)
    root.put("currentUserId", state.currentUserId)

    val ridesArr = JSONArray()
    state.rides.forEach { r ->
      val obj = JSONObject()
      obj.put("id", r.id)
      obj.put("driverId", r.driverId)
      obj.put("driverName", r.driverName)
      obj.put("driverCar", r.driverCar)
      obj.put("driverRating", r.driverRating)
      obj.put("driverPhone", r.driverPhone)
      obj.put("fromLocation", r.fromLocation)
      obj.put("toLocation", r.toLocation)
      obj.put("date", r.date)
      obj.put("time", r.time)
      obj.put("totalSeats", r.totalSeats)
      obj.put("availableSeats", r.availableSeats)
      obj.put("pricePerSeat", r.pricePerSeat)
      obj.put("comment", r.comment)
      obj.put("status", r.status.name)
      obj.put("isCommissionCharged", r.isCommissionCharged)
      obj.put("createdAt", r.createdAt)
      ridesArr.put(obj)
    }
    root.put("rides", ridesArr)

    val bookingsArr = JSONArray()
    state.bookings.forEach { b ->
      val obj = JSONObject()
      obj.put("id", b.id)
      obj.put("rideId", b.rideId)
      obj.put("passengerId", b.passengerId)
      obj.put("passengerName", b.passengerName)
      obj.put("passengerPhone", b.passengerPhone)
      obj.put("seatsCount", b.seatsCount)
      obj.put("pricePerSeat", b.pricePerSeat)
      obj.put("totalPrice", b.totalPrice)
      obj.put("commissionAmount", b.commissionAmount)
      obj.put("paymentMethod", b.paymentMethod.name)
      obj.put("status", b.status.name)
      obj.put("notes", b.notes)
      obj.put("createdAt", b.createdAt)
      bookingsArr.put(obj)
    }
    root.put("bookings", bookingsArr)

    val chatsArr = JSONArray()
    state.chats.forEach { c ->
      val obj = JSONObject()
      obj.put("id", c.id)
      obj.put("rideId", c.rideId)
      obj.put("driverId", c.driverId)
      obj.put("passengerId", c.passengerId)
      obj.put("passengerName", c.passengerName)
      obj.put("driverName", c.driverName)
      obj.put("routeTitle", c.routeTitle)
      obj.put("unreadCount", c.unreadCount)

      val msgsArr = JSONArray()
      c.messages.forEach { m ->
        val mObj = JSONObject()
        mObj.put("id", m.id)
        mObj.put("senderId", m.senderId)
        mObj.put("senderName", m.senderName)
        mObj.put("text", m.text)
        mObj.put("timestamp", m.timestamp)
        mObj.put("isMine", m.isMine)
        msgsArr.put(mObj)
      }
      obj.put("messages", msgsArr)
      chatsArr.put(obj)
    }
    root.put("chats", chatsArr)

    val notifsArr = JSONArray()
    state.notifications.forEach { n ->
      val obj = JSONObject()
      obj.put("id", n.id)
      obj.put("userId", n.userId)
      obj.put("title", n.title)
      obj.put("message", n.message)
      obj.put("timestamp", n.timestamp)
      obj.put("isRead", n.isRead)
      obj.put("type", n.type.name)
      if (n.targetRideId != null) obj.put("targetRideId", n.targetRideId)
      notifsArr.put(obj)
    }
    root.put("notifications", notifsArr)

    val txArr = JSONArray()
    state.transactions.forEach { t ->
      val obj = JSONObject()
      obj.put("id", t.id)
      obj.put("driverId", t.driverId)
      obj.put("type", t.type.name)
      obj.put("amount", t.amount)
      obj.put("description", t.description)
      obj.put("timestamp", t.timestamp)
      if (t.rideId != null) obj.put("rideId", t.rideId)
      txArr.put(obj)
    }
    root.put("transactions", txArr)

    val compArr = JSONArray()
    state.complaints.forEach { cmp ->
      val obj = JSONObject()
      obj.put("id", cmp.id)
      obj.put("authorId", cmp.authorId)
      obj.put("authorName", cmp.authorName)
      obj.put("targetType", cmp.targetType)
      obj.put("targetId", cmp.targetId)
      obj.put("targetTitle", cmp.targetTitle)
      obj.put("reason", cmp.reason)
      obj.put("description", cmp.description)
      obj.put("status", cmp.status.name)
      obj.put("createdAt", cmp.createdAt)
      compArr.put(obj)
    }
    root.put("complaints", compArr)

    val locArr = JSONArray()
    state.locations.forEach { l ->
      val obj = JSONObject()
      obj.put("id", l.id)
      obj.put("name", l.name)
      obj.put("category", l.category)
      obj.put("description", l.description)
      locArr.put(obj)
    }
    root.put("locations", locArr)

    return root
  }

  private fun parseState(root: JSONObject): State {
    val users = mutableListOf<User>()
    val usersArr = root.optJSONArray("users") ?: JSONArray()
    for (i in 0 until usersArr.length()) {
      val obj = usersArr.getJSONObject(i)
      users.add(
        User(
          id = obj.getString("id"),
          name = obj.getString("name"),
          phone = obj.getString("phone"),
          role = try { UserRole.valueOf(obj.getString("role")) } catch (e: Exception) { UserRole.PASSENGER },
          rating = obj.optDouble("rating", 5.0),
          tripsCount = obj.optInt("tripsCount", 0),
          carModel = obj.optString("carModel", ""),
          carNumber = obj.optString("carNumber", ""),
          isBlocked = obj.optBoolean("isBlocked", false),
          balance = obj.optInt("balance", 0)
        )
      )
    }

    val currentUserId = root.optString("currentUserId", "user_pass_1")

    val rides = mutableListOf<Ride>()
    val ridesArr = root.optJSONArray("rides") ?: JSONArray()
    for (i in 0 until ridesArr.length()) {
      val obj = ridesArr.getJSONObject(i)
      rides.add(
        Ride(
          id = obj.getString("id"),
          driverId = obj.getString("driverId"),
          driverName = obj.getString("driverName"),
          driverCar = obj.getString("driverCar"),
          driverRating = obj.optDouble("driverRating", 5.0),
          driverPhone = obj.getString("driverPhone"),
          fromLocation = obj.getString("fromLocation"),
          toLocation = obj.getString("toLocation"),
          date = obj.getString("date"),
          time = obj.getString("time"),
          totalSeats = obj.getInt("totalSeats"),
          availableSeats = obj.getInt("availableSeats"),
          pricePerSeat = obj.getInt("pricePerSeat"),
          comment = obj.optString("comment", ""),
          status = try { RideStatus.valueOf(obj.getString("status")) } catch (e: Exception) { RideStatus.ACTIVE },
          isCommissionCharged = obj.optBoolean("isCommissionCharged", false),
          createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
      )
    }

    val bookings = mutableListOf<Booking>()
    val bookingsArr = root.optJSONArray("bookings") ?: JSONArray()
    for (i in 0 until bookingsArr.length()) {
      val obj = bookingsArr.getJSONObject(i)
      bookings.add(
        Booking(
          id = obj.getString("id"),
          rideId = obj.getString("rideId"),
          passengerId = obj.getString("passengerId"),
          passengerName = obj.getString("passengerName"),
          passengerPhone = obj.getString("passengerPhone"),
          seatsCount = obj.getInt("seatsCount"),
          pricePerSeat = obj.getInt("pricePerSeat"),
          totalPrice = obj.getInt("totalPrice"),
          commissionAmount = obj.optInt("commissionAmount", 0),
          paymentMethod = try { PaymentMethod.valueOf(obj.getString("paymentMethod")) } catch (e: Exception) { PaymentMethod.CASH },
          status = try { BookingStatus.valueOf(obj.getString("status")) } catch (e: Exception) { BookingStatus.PENDING },
          notes = obj.optString("notes", ""),
          createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
      )
    }

    val chats = mutableListOf<ChatThread>()
    val chatsArr = root.optJSONArray("chats") ?: JSONArray()
    for (i in 0 until chatsArr.length()) {
      val obj = chatsArr.getJSONObject(i)
      val msgs = mutableListOf<ChatMessage>()
      val msgsArr = obj.optJSONArray("messages") ?: JSONArray()
      for (j in 0 until msgsArr.length()) {
        val mObj = msgsArr.getJSONObject(j)
        msgs.add(
          ChatMessage(
            id = mObj.getString("id"),
            senderId = mObj.getString("senderId"),
            senderName = mObj.getString("senderName"),
            text = mObj.getString("text"),
            timestamp = mObj.getLong("timestamp"),
            isMine = mObj.optBoolean("isMine", false)
          )
        )
      }
      chats.add(
        ChatThread(
          id = obj.getString("id"),
          rideId = obj.getString("rideId"),
          driverId = obj.getString("driverId"),
          passengerId = obj.getString("passengerId"),
          passengerName = obj.getString("passengerName"),
          driverName = obj.getString("driverName"),
          routeTitle = obj.getString("routeTitle"),
          unreadCount = obj.optInt("unreadCount", 0),
          messages = msgs
        )
      )
    }

    val notifs = mutableListOf<AppNotification>()
    val notifsArr = root.optJSONArray("notifications") ?: JSONArray()
    for (i in 0 until notifsArr.length()) {
      val obj = notifsArr.getJSONObject(i)
      notifs.add(
        AppNotification(
          id = obj.getString("id"),
          userId = obj.getString("userId"),
          title = obj.getString("title"),
          message = obj.getString("message"),
          timestamp = obj.getLong("timestamp"),
          isRead = obj.optBoolean("isRead", false),
          type = try { NotificationType.valueOf(obj.getString("type")) } catch (e: Exception) { NotificationType.REQUEST },
          targetRideId = if (obj.has("targetRideId")) obj.getString("targetRideId") else null
        )
      )
    }

    val txs = mutableListOf<WalletTransaction>()
    val txArr = root.optJSONArray("transactions") ?: JSONArray()
    for (i in 0 until txArr.length()) {
      val obj = txArr.getJSONObject(i)
      txs.add(
        WalletTransaction(
          id = obj.getString("id"),
          driverId = obj.getString("driverId"),
          type = try { TransactionType.valueOf(obj.getString("type")) } catch (e: Exception) { TransactionType.DEMO_TOPUP },
          amount = obj.getInt("amount"),
          description = obj.getString("description"),
          timestamp = obj.getLong("timestamp"),
          rideId = if (obj.has("rideId")) obj.getString("rideId") else null
        )
      )
    }

    val comps = mutableListOf<Complaint>()
    val compArr = root.optJSONArray("complaints") ?: JSONArray()
    for (i in 0 until compArr.length()) {
      val obj = compArr.getJSONObject(i)
      comps.add(
        Complaint(
          id = obj.getString("id"),
          authorId = obj.getString("authorId"),
          authorName = obj.getString("authorName"),
          targetType = obj.getString("targetType"),
          targetId = obj.getString("targetId"),
          targetTitle = obj.getString("targetTitle"),
          reason = obj.getString("reason"),
          description = obj.getString("description"),
          status = try { ComplaintStatus.valueOf(obj.getString("status")) } catch (e: Exception) { ComplaintStatus.PENDING },
          createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
      )
    }

    val locs = mutableListOf<LocationPoint>()
    val locArr = root.optJSONArray("locations") ?: JSONArray()
    for (i in 0 until locArr.length()) {
      val obj = locArr.getJSONObject(i)
      locs.add(
        LocationPoint(
          id = obj.getString("id"),
          name = obj.getString("name"),
          category = obj.getString("category"),
          description = obj.optString("description", "")
        )
      )
    }

    return State(
      users = if (users.isEmpty()) DemoData.getDefaultUsers() else users,
      currentUserId = currentUserId,
      rides = if (rides.isEmpty()) DemoData.getDefaultRides() else rides,
      bookings = if (bookings.isEmpty()) DemoData.getDefaultBookings() else bookings,
      chats = if (chats.isEmpty()) DemoData.getDefaultChats() else chats,
      notifications = if (notifs.isEmpty()) DemoData.getDefaultNotifications() else notifs,
      transactions = if (txs.isEmpty()) DemoData.getDefaultTransactions() else txs,
      complaints = if (comps.isEmpty()) DemoData.getDefaultComplaints() else comps,
      locations = if (locs.isEmpty()) DemoData.getDefaultLocations() else locs
    )
  }
}

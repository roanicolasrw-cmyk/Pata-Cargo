package com.example.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey var id: String = "",
    var name: String = "",
    var dni: String = "",
    var rating: Float = 0.0f,
    var isVerified: Boolean = false,
    var isBiometricVerified: Boolean = false,
    var walletBalance: Double = 0.0,
    var registrationSelfie: String? = null,
    var mainRole: String = "ENVIADOR",
    var mercadoPagoEmail: String? = null,
    var isMercadoPagoConnected: Boolean = false
) : Serializable {
    // Constructor explícito sin argumentos para Firebase/Firestore
    constructor() : this("", "", "", 0.0f, false, false, 0.0, null, "ENVIADOR", null, false)
}

@Keep
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var carrierId: String = "",
    var origin: String = "",
    var destination: String = ""
) : Serializable {
    // Constructor explícito sin argumentos para Firebase/Firestore
    constructor() : this(0, "", "", "")
}

@Keep
@Entity(tableName = "shipments")
data class ShipmentEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var origin: String = "",
    var destination: String = "",
    var size: String = "",
    var isFragile: Boolean = false,
    var timeWindow: String = "",
    var status: String = "PENDIENTE",
    var price: Double = 0.0,
    var declaredValue: Double = 0.0,
    var insuranceEnabled: Boolean = false,
    var insuranceCost: Double = 0.0,
    var senderId: String = "",
    var carrierId: String? = null,
    var qrValueCollection: String = "",
    var qrValueDelivery: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var mpPreferenceId: String? = null,
    var mpCheckoutUrl: String? = null,
    var mpPaymentStatus: String = "PENDIENTE"
) : Serializable {
    // Constructor explícito sin argumentos para Firebase/Firestore
    constructor() : this(
        0, "", "", "", "", "", false, "", "PENDIENTE", 
        0.0, 0.0, false, 0.0, "", null, "", "", System.currentTimeMillis(),
        null, null, "PENDIENTE"
    )
}

@Keep
@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var shipmentId: Int = 0,
    var carrierId: String = "",
    var carrierName: String = "",
    var carrierRating: Float = 0.0f,
    var carrierDni: String = "",
    var amount: Double = 0.0,
    var comment: String = "",
    var status: String = "PENDIENTE"
) : Serializable {
    // Constructor explícito sin argumentos para Firebase/Firestore
    constructor() : this(0, 0, "", "", 0.0f, "", 0.0, "", "PENDIENTE")
}

@Keep
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var shipmentId: Int = 0,
    var senderId: String = "",
    var senderName: String = "",
    var message: String = "",
    var timestamp: Long = System.currentTimeMillis()
) : Serializable {
    // Constructor explícito sin argumentos para Firebase/Firestore
    constructor() : this(0, 0, "", "", "", System.currentTimeMillis())
}

@Keep
@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var shipmentId: Int = 0,
    var writerId: String = "",
    var targetId: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var timestamp: Long = System.currentTimeMillis()
) : Serializable {
    // Constructor explícito sin argumentos para Firebase/Firestore
    constructor() : this(0, 0, "", "", 0, "", System.currentTimeMillis())
}

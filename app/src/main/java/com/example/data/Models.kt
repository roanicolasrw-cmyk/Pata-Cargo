package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dni: String,
    val rating: Float,
    val isVerified: Boolean = false, // Approved by Admin (identity check)
    val isBiometricVerified: Boolean = false, // Biometric state
    val walletBalance: Double = 0.0,
    val registrationSelfie: String? = null
) : Serializable

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carrierId: String,
    val origin: String,
    val destination: String
) : Serializable

@Entity(tableName = "shipments")
data class ShipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val origin: String,
    val destination: String,
    val size: String, // "Pequeño", "Mediano", "Grande"
    val isFragile: Boolean = false,
    val timeWindow: String,
    val status: String, // "PENDIENTE", "ACEPTADO", "EN_VIAJE", "ENTREGADO", "DISPUTADO"
    val price: Double,
    val declaredValue: Double,
    val insuranceEnabled: Boolean = false,
    val insuranceCost: Double = 0.0,
    val senderId: String,
    val carrierId: String? = null,
    val qrValueCollection: String = "",
    val qrValueDelivery: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shipmentId: Int,
    val carrierId: String,
    val carrierName: String,
    val carrierRating: Float,
    val carrierDni: String,
    val amount: Double,
    val comment: String,
    val status: String = "PENDIENTE" // "PENDIENTE", "ACEPTADO", "RECHAZADO"
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shipmentId: Int,
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shipmentId: Int,
    val writerId: String,
    val targetId: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

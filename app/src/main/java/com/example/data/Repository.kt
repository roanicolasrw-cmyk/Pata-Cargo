package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class Repository(private val context: Context) {

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "pata_cargo_database"
        ).fallbackToDestructiveMigration().build()
    }

    val userDao by lazy { db.userDao() }
    val routeDao by lazy { db.routeDao() }
    val shipmentDao by lazy { db.shipmentDao() }
    val offerDao by lazy { db.offerDao() }
    val chatMessageDao by lazy { db.chatMessageDao() }
    val reviewDao by lazy { db.reviewDao() }

    init {
        // Seed initial mock data asynchronously on start if the database is empty.
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfNeeded()
        }
    }

    // Cities of the VIRCH corridor + Puerto Madryn
    val cities = listOf(
        "Puerto Madryn",
        "Trelew",
        "Rawson",
        "Gaiman",
        "Dolavon"
    )

    // Helper to calculate estimated shipping price based on origin, destination and size
    fun calculatePrice(origin: String, destination: String, size: String): Double {
        if (origin == destination) return 800.0

        val baseDistanceCost = when {
            // Trelew - Rawson (~20km)
            (origin == "Trelew" && destination == "Rawson") || (origin == "Rawson" && destination == "Trelew") -> 1200.0
            // Trelew - Puerto Madryn (~65km)
            (origin == "Trelew" && destination == "Puerto Madryn") || (origin == "Puerto Madryn" && destination == "Trelew") -> 3500.0
            // Rawson - Puerto Madryn (~85km)
            (origin == "Rawson" && destination == "Puerto Madryn") || (origin == "Puerto Madryn" && destination == "Rawson") -> 4500.0
            // Gaiman - Trelew (~17km)
            (origin == "Gaiman" && destination == "Trelew") || (origin == "Trelew" && destination == "Gaiman") -> 1000.0
            // Gaiman - Puerto Madryn (~82km)
            (origin == "Gaiman" && destination == "Puerto Madryn") || (origin == "Puerto Madryn" && destination == "Gaiman") -> 4200.0
            // Dolavon - Trelew (~36km)
            (origin == "Dolavon" && destination == "Trelew") || (origin == "Trelew" && destination == "Dolavon") -> 2000.0
            // Dolavon - Puerto Madryn (~101km)
            (origin == "Dolavon" && destination == "Puerto Madryn") || (origin == "Puerto Madryn" && destination == "Dolavon") -> 5200.0
            // Gaiman - Rawson (~37km)
            (origin == "Gaiman" && destination == "Rawson") || (origin == "Rawson" && destination == "Gaiman") -> 2100.0
            // Dolavon - Rawson (~56km)
            (origin == "Dolavon" && destination == "Rawson") || (origin == "Rawson" && destination == "Dolavon") -> 3000.0
            // Gaiman - Dolavon (~19km)
            (origin == "Gaiman" && destination == "Dolavon") || (origin == "Dolavon" && destination == "Gaiman") -> 1100.0
            else -> 2500.0
        }

        val sizeMultiplier = when (size) {
            "Pequeño" -> 1.0  // Documents, small bags
            "Mediano" -> 1.4  // Shoe boxes, retail bags
            "Grande" -> 2.0   // Heavy box, computer hardware
            else -> 1.0
        }

        return baseDistanceCost * sizeMultiplier
    }

    // Calculating insurance cost (1.5% of declared value)
    fun calculateInsurance(declaredValue: Double): Double {
        return Math.max(150.0, declaredValue * 0.015)
    }

    /**
     * Intelligent Router (Rutas Inteligentes)
     * Matches carrier favorited routes with pending shipments that are on the route.
     * Path segments: Dolavon -> Gaiman -> Trelew -> Rawson
     * and Trelew <-> Puerto Madryn (as a north connection).
     *
     * We map the hierarchy of locations geographically to determine what matches.
     */
    fun isShipmentOnRoute(shipmentOrigin: String, shipmentDest: String, routeOrigin: String, routeDest: String): Boolean {
        // Direct match
        if (shipmentOrigin == routeOrigin && shipmentDest == routeDest) return true

        // Let's define the nodes of the typical VIRCH-Madryn route corridor
        // West to East/North corridor representation
        val corridor = listOf("Dolavon", "Gaiman", "Trelew", "Puerto Madryn")
        
        // Rawson is a branch from Trelew
        // Trelew is intermediate for almost anything going from Gaiman/Dolavon to Madryn or Rawson.

        val routeIdxO = corridor.indexOf(routeOrigin)
        val routeIdxD = corridor.indexOf(routeDest)

        val shipIdxO = corridor.indexOf(shipmentOrigin)
        val shipIdxD = corridor.indexOf(shipmentDest)

        // If both the carrier's route and the shipment's route are completely inside our main corridor
        if (routeIdxO != -1 && routeIdxD != -1 && shipIdxO != -1 && shipIdxD != -1) {
            // Is it moving in the same direction?
            val routeForward = routeIdxD > routeIdxO
            val shipForward = shipIdxD > shipIdxO

            if (routeForward == shipForward) {
                // If forward, shipment origin must be >= route origin, and shipment dest must be <= route dest.
                if (routeForward) {
                    return shipIdxO >= routeIdxO && shipIdxD <= routeIdxD
                } else {
                    // Backward: index is smaller when moving forward, so for backward (decreasing index)
                    // routeOrigin is higher index than routeDest.
                    // shipment origin must be <= route origin and shipment dest must be >= route dest.
                    return shipIdxO <= routeIdxO && shipIdxD >= routeIdxD
                }
            }
        }

        // Branch matches with Rawson:
        // Rawson connects via Trelew. If they travel Gaiman -> Rawson, they visit Gaiman -> Trelew -> Rawson.
        // Therefore active route Gaiman -> Rawson also matches Gaiman -> Trelew.
        if (routeOrigin == "Gaiman" && routeDest == "Rawson") {
            if (shipmentOrigin == "Gaiman" && shipmentDest == "Trelew") return true
            if (shipmentOrigin == "Trelew" && shipmentDest == "Rawson") return true
        }
        if (routeOrigin == "Dolavon" && routeDest == "Rawson") {
            if (shipmentOrigin == "Dolavon" && shipmentDest == "Trelew") return true
            if (shipmentOrigin == "Gaiman" && shipmentDest == "Trelew") return true
            if (shipmentOrigin == "Trelew" && shipmentDest == "Rawson") return true
        }

        // Gaiman -> Madryn includes Trelew.
        if (routeOrigin == "Gaiman" && routeDest == "Puerto Madryn") {
            if (shipmentOrigin == "Gaiman" && shipmentDest == "Trelew") return true
            if (shipmentOrigin == "Trelew" && shipmentDest == "Puerto Madryn") return true
        }

        // Dolavon -> Madryn includes Gaiman, Trelew
        if (routeOrigin == "Dolavon" && routeDest == "Puerto Madryn") {
            if (shipmentOrigin == "Dolavon" && shipmentDest == "Trelew") return true
            if (shipmentOrigin == "Gaiman" && shipmentDest == "Trelew") return true
            if (shipmentOrigin == "Trelew" && shipmentDest == "Puerto Madryn") return true
        }

        return false
    }

    private suspend fun seedDatabaseIfNeeded() {
        val usersCount = db.userDao().getAllUsersFlow().first().size
        if (usersCount > 0) return // Already seeded!

        // 1. Seed Default Users
        val users = listOf(
            UserEntity(
                id = "enviador_juan",
                name = "Juan Carlos Rawson",
                dni = "28.341.520",
                rating = 4.8f,
                isVerified = true,
                isBiometricVerified = true,
                walletBalance = 52000.0,
                registrationSelfie = "selfie_juan"
            ),
            UserEntity(
                id = "enviador_distribuidora",
                name = "Distribuidora El Valle",
                dni = "30-71452934-8", // CUIT
                rating = 4.9f,
                isVerified = true,
                isBiometricVerified = true,
                walletBalance = 154000.0,
                registrationSelfie = "selfie_valle"
            ),
            UserEntity(
                id = "portador_sofia",
                name = "Sofía Gales Gaiman",
                dni = "37.562.901",
                rating = 4.7f,
                isVerified = true,
                isBiometricVerified = true,
                walletBalance = 14500.0,
                registrationSelfie = "selfie_sofia"
            ),
            UserEntity(
                id = "portador_marcos",
                name = "Marcos Madryn",
                dni = "35.118.423",
                rating = 4.9f,
                isVerified = false, // NOT verified, shows Admin Identity approval check!
                isBiometricVerified = false,
                walletBalance = 0.0,
                registrationSelfie = "selfie_marcos"
            ),
            UserEntity(
                id = "admin",
                name = "Administración Pata Cargo",
                dni = "Administrador",
                rating = 5.0f,
                isVerified = true,
                isBiometricVerified = true,
                walletBalance = 1250000.0 // Commissions pool
            )
        )

        for (u in users) {
            db.userDao().insertUser(u)
        }

        // 2. Seed Carrier Routes
        val routes = listOf(
            RouteEntity(carrierId = "portador_sofia", origin = "Gaiman", destination = "Puerto Madryn"),
            RouteEntity(carrierId = "portador_sofia", origin = "Trelew", destination = "Rawson"),
            RouteEntity(carrierId = "portador_marcos", origin = "Trelew", destination = "Puerto Madryn")
        )
        for (r in routes) {
            db.routeDao().insertRoute(r)
        }

        // 3. Seed Shipments
        val shipments = listOf(
            ShipmentEntity(
                title = "Repuesto de bomba agrícola",
                description = "Caja pesada de repuestos mecánicos para tractor John Deere. Retiro urgente.",
                origin = "Gaiman",
                destination = "Puerto Madryn",
                size = "Grande",
                isFragile = true,
                timeWindow = "Antes de las 18:00hs",
                price = calculatePrice("Gaiman", "Puerto Madryn", "Grande"),
                declaredValue = 120000.0,
                insuranceEnabled = true,
                insuranceCost = calculateInsurance(120000.0),
                senderId = "enviador_distribuidora",
                carrierId = null,
                qrValueCollection = "COLL-BOMB",
                qrValueDelivery = "DELI-AGRI",
                status = "PENDIENTE"
            ),
            ShipmentEntity(
                title = "Caja de alfajores galeses",
                description = "4 cajas de alfajores artesanales de crema galesa, frescos.",
                origin = "Rawson",
                destination = "Trelew",
                size = "Pequeño",
                isFragile = false,
                timeWindow = "Hoy, cualquier horario",
                price = calculatePrice("Rawson", "Trelew", "Pequeño"),
                declaredValue = 15000.0,
                insuranceEnabled = false,
                insuranceCost = 0.0,
                senderId = "enviador_juan",
                carrierId = null,
                qrValueCollection = "COLL-ALFA",
                qrValueDelivery = "DELI-GALE",
                status = "PENDIENTE"
            ),
            ShipmentEntity(
                title = "Estuche de laboratorio óptico",
                description = "Lentes calibrados de alta precisión para clínica oftalmológica.",
                origin = "Trelew",
                destination = "Rawson",
                size = "Pequeño",
                isFragile = true,
                timeWindow = "Mañana antes del mediodía",
                price = calculatePrice("Trelew", "Rawson", "Pequeño"),
                declaredValue = 85000.0,
                insuranceEnabled = true,
                insuranceCost = calculateInsurance(85000.0),
                senderId = "enviador_distribuidora",
                carrierId = "portador_sofia",
                qrValueCollection = "COLL-OPTI",
                qrValueDelivery = "DELI-LENS",
                status = "EN_VIAJE"
            ),
            ShipmentEntity(
                title = "Ropa para revender",
                description = "Bolsa de consorcio mediana con prendas tejidas de lana patagónica.",
                origin = "Trelew",
                destination = "Puerto Madryn",
                size = "Mediano",
                isFragile = false,
                timeWindow = "Lunes a viernes",
                price = calculatePrice("Trelew", "Puerto Madryn", "Mediano"),
                declaredValue = 45000.0,
                insuranceEnabled = false,
                insuranceCost = 0.0,
                senderId = "enviador_juan",
                carrierId = "portador_sofia",
                qrValueCollection = "COLL-ROPA",
                qrValueDelivery = "DELI-LANA",
                status = "ENTREGADO"
            )
        )

        val seededShipmentsIds = mutableListOf<Int>()
        for (s in shipments) {
            val sId = db.shipmentDao().insertShipment(s)
            seededShipmentsIds.add(sId.toInt())
        }

        // 4. Seed Offers for pending shipments
        // Marcos (not verified yet) makes an offer on the john deere agricultural pump
        db.offerDao().insertOffer(
            OfferEntity(
                shipmentId = seededShipmentsIds[0],
                carrierId = "portador_marcos",
                carrierName = "Marcos Madryn",
                carrierRating = 4.9f,
                carrierDni = "35.118.423",
                amount = 7500.0,
                comment = "Viajo hoy a las 14:00 por trabajo, te lo puedo dejar en destino a las 15:30 directo."
            )
        )

        // Sofia makes an offer on the agricultural pump too
        db.offerDao().insertOffer(
            OfferEntity(
                shipmentId = seededShipmentsIds[0],
                carrierId = "portador_sofia",
                carrierName = "Sofía Gales Gaiman",
                carrierRating = 4.7f,
                carrierDni = "37.562.901",
                amount = 8200.0,
                comment = "Suelo andar con espacio suficiente en mi baúl. Paso a recolectar al mediodía."
            )
        )

        // Sofia also made offers on shipment 2
        db.offerDao().insertOffer(
            OfferEntity(
                shipmentId = seededShipmentsIds[1],
                carrierId = "portador_sofia",
                carrierName = "Sofía Gales Gaiman",
                carrierRating = 4.7f,
                carrierDni = "37.562.901",
                amount = 1300.0,
                comment = "Hago Rawson-Trelew de regreso a casa hoy."
            )
        )

        // 5. Seed some chat messages for the active shipment
        val activeShipmentId = seededShipmentsIds[2]
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(
                shipmentId = activeShipmentId,
                senderId = "enviador_distribuidora",
                senderName = "Distribuidora El Valle",
                message = "Buenas tardes Sofía, te espero en la puerta principal del laboratorio, por calle España."
            )
        )
        db.chatMessageDao().insertMessage(
            ChatMessageEntity(
                shipmentId = activeShipmentId,
                senderId = "portador_sofia",
                senderName = "Sofía Gales Gaiman",
                message = "Excelente, estoy saliendo para allá en 15 minutos. Llevo baúl vacío."
            )
        )

        // 6. Seed a completed review
        db.reviewDao().insertReview(
            ReviewEntity(
                shipmentId = seededShipmentsIds[3],
                writerId = "enviador_juan",
                targetId = "portador_sofia",
                rating = 5,
                comment = "Súper rápida la entrega de la lana a Madryn. Muy amable Sofía!"
            )
        )
    }
}

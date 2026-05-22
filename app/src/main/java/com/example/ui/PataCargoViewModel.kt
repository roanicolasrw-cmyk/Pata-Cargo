package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class PataCargoViewModel(application: Application) : AndroidViewModel(application) {

    val repository = Repository(application)

    // Identity Simulation State
    val selectedUserId = MutableStateFlow("enviador_juan")

    // Retrieve active User info
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<UserEntity?> = selectedUserId
        .flatMapLatest { uid -> repository.userDao.getUserByIdFlow(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Users list for switching profiles in MVP
    val allUsers: StateFlow<List<UserEntity>> = repository.userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Carriers waiting for validation (Admin check)
    val pendingVerificationUsers: StateFlow<List<UserEntity>> = repository.userDao.getUsersPendingVerificationFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All shipments in the system (Admin overview)
    val allShipments: StateFlow<List<ShipmentEntity>> = repository.shipmentDao.getAllShipmentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shipments where current user is the Sender
    @OptIn(ExperimentalCoroutinesApi::class)
    val mySenderShipments: StateFlow<List<ShipmentEntity>> = selectedUserId
        .flatMapLatest { uid -> repository.shipmentDao.getShipmentsBySenderFlow(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shipments where current user is the Carrier
    @OptIn(ExperimentalCoroutinesApi::class)
    val myCarrierShipments: StateFlow<List<ShipmentEntity>> = selectedUserId
        .flatMapLatest { uid -> repository.shipmentDao.getShipmentsByCarrierFlow(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available shipments (status 'PENDIENTE')
    val pendingShipments: StateFlow<List<ShipmentEntity>> = repository.shipmentDao.getPendingShipmentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current carrier's favorite routes
    @OptIn(ExperimentalCoroutinesApi::class)
    val myRoutes: StateFlow<List<RouteEntity>> = selectedUserId
        .flatMapLatest { uid -> repository.routeDao.getRoutesByCarrierFlow(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active carrier-matching shipments ("Rutas Inteligentes" recommendations)
    val recommendedShipments: StateFlow<List<ShipmentEntity>> = combine(
        pendingShipments,
        myRoutes
    ) { pending, routes ->
        pending.filter { shipment ->
            routes.any { route ->
                repository.isShipmentOnRoute(
                    shipmentOrigin = shipment.origin,
                    shipmentDest = shipment.destination,
                    routeOrigin = route.origin,
                    routeDest = route.destination
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active chat messages flow for open chats
    private val _activeShipmentIdForChat = MutableStateFlow<Int?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessagesForActiveShipment: StateFlow<List<ChatMessageEntity>> = _activeShipmentIdForChat
        .flatMapLatest { sId ->
            if (sId != null) {
                repository.chatMessageDao.getMessagesForShipmentFlow(sId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active chat shipment details
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatShipmentDetails: StateFlow<ShipmentEntity?> = _activeShipmentIdForChat
        .flatMapLatest { sId ->
            if (sId != null) {
                repository.shipmentDao.getShipmentByIdFlow(sId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActiveChatShipmentId(shipmentId: Int?) {
        _activeShipmentIdForChat.value = shipmentId
    }

    // Selected Shipment ID for Viewing Details and Offers
    val selectedShipmentIdDetail = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedShipmentDetail: StateFlow<ShipmentEntity?> = selectedShipmentIdDetail
        .flatMapLatest { sId ->
            if (sId != null) {
                repository.shipmentDao.getShipmentByIdFlow(sId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedShipmentOffers: StateFlow<List<OfferEntity>> = selectedShipmentIdDetail
        .flatMapLatest { sId ->
            if (sId != null) {
                repository.offerDao.getOffersForShipmentFlow(sId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Biometric scanner/validation simulator state
    val carrierUnderSelfieVerification = MutableStateFlow<UserEntity?>(null)

    // Wallet states
    val escrowedFunds: StateFlow<Double> = allShipments.map { shipments ->
        shipments.filter { it.status == "ACEPTADO" || it.status == "EN_VIAJE" }
            .sumOf { (it.price * 1.15) + if (it.insuranceEnabled) it.insuranceCost else 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val adminCommissions: StateFlow<Double> = allShipments.map { shipments ->
        // Total platform commissions (15% of delivered packages pricing paid by the sender)
        shipments.filter { it.status == "ENTREGADO" }
            .sumOf { it.price * 0.15 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Actions
    fun changeActiveRole(userId: String) {
        selectedUserId.value = userId
        // Reset navigation detail models
        selectedShipmentIdDetail.value = null
        _activeShipmentIdForChat.value = null
    }

    fun addFavoriteRoute(origin: String, destination: String) {
        val carrierId = selectedUserId.value
        viewModelScope.launch {
            repository.routeDao.insertRoute(
                RouteEntity(carrierId = carrierId, origin = origin, destination = destination)
            )
        }
    }

    fun removeFavoriteRoute(routeId: Int) {
        viewModelScope.launch {
            repository.routeDao.deleteRouteById(routeId)
        }
    }

    fun createShipment(
        title: String,
        description: String,
        origin: String,
        destination: String,
        size: String,
        isFragile: Boolean,
        timeWindow: String,
        declaredValue: Double,
        insuranceEnabled: Boolean
    ) {
        val senderId = selectedUserId.value
        val price = repository.calculatePrice(origin, destination, size)
        val insuranceCost = if (insuranceEnabled) repository.calculateInsurance(declaredValue) else 0.0

        val qrCol = "COLLECT-${UUID.randomUUID().toString().take(6).uppercase()}"
        val qrDel = "DELIVER-${UUID.randomUUID().toString().take(6).uppercase()}"

        val shipment = ShipmentEntity(
            title = title,
            description = description,
            origin = origin,
            destination = destination,
            size = size,
            isFragile = isFragile,
            timeWindow = timeWindow,
            price = price,
            declaredValue = declaredValue,
            insuranceEnabled = insuranceEnabled,
            insuranceCost = insuranceCost,
            senderId = senderId,
            carrierId = null,
            qrValueCollection = qrCol,
            qrValueDelivery = qrDel,
            status = "PENDIENTE"
        )

        viewModelScope.launch {
            // Deduct immediately as simulated Escrow payment (retained in Pata Cargo treasury: price + 15% platform commission + insurance)
            val sender = repository.userDao.getUserById(senderId)
            if (sender != null) {
                val totalCost = (price * 1.15) + insuranceCost
                repository.userDao.updateUser(sender.copy(walletBalance = sender.walletBalance - totalCost))
            }
            repository.shipmentDao.insertShipment(shipment)
        }
    }

    fun makeOffer(shipmentId: Int, offerAmount: Double, comment: String) {
        val carrierId = selectedUserId.value
        viewModelScope.launch {
            val carrier = repository.userDao.getUserById(carrierId) ?: return@launch
            val offer = OfferEntity(
                shipmentId = shipmentId,
                carrierId = carrierId,
                carrierName = carrier.name,
                carrierRating = carrier.rating,
                carrierDni = carrier.dni,
                amount = offerAmount,
                comment = comment,
                status = "PENDIENTE"
            )
            repository.offerDao.insertOffer(offer)
        }
    }

    fun acceptOffer(shipmentId: Int, offer: OfferEntity) {
        viewModelScope.launch {
            val shipment = repository.shipmentDao.getShipmentById(shipmentId) ?: return@launch
            
            // Mark other offers as rejected, this as accepted
            repository.offerDao.updateOfferStatus(offer.id, "ACEPTADO")
            repository.offerDao.rejectOtherOffers(shipmentId, offer.id)

            // Dynamic escrow budget check
            val previousPrice = shipment.price
            val finalPrice = offer.amount

            // If the offer is different than the shipment pre-calculated price, adjust sender wallet (inclusive of 15% commission difference)
            if (finalPrice != previousPrice) {
                val difference = (finalPrice - previousPrice) * 1.15
                val sender = repository.userDao.getUserById(shipment.senderId)
                if (sender != null) {
                    repository.userDao.updateUser(sender.copy(walletBalance = sender.walletBalance - difference))
                }
            }

            // Update shipment state: Accepted, tied to Carrier
            repository.shipmentDao.updateShipment(
                shipment.copy(
                    carrierId = offer.carrierId,
                    status = "ACEPTADO",
                    price = finalPrice // Use carrier bid
                )
            )
        }
    }

    fun simulateCollectScan(shipmentId: Int, qrCodeContent: String): Boolean {
        var success = false
        viewModelScope.launch {
            val shipment = repository.shipmentDao.getShipmentById(shipmentId) ?: return@launch
            if (shipment.qrValueCollection == qrCodeContent || qrCodeContent == "SIMULATE-BYPASS") {
                repository.shipmentDao.updateShipmentStatus(shipmentId, "EN_VIAJE")
                // Seed initial tracking chat message automatically
                repository.chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        shipmentId = shipmentId,
                        senderId = "system",
                        senderName = "Sistema Pata Cargo",
                        message = "El paquete ha sido recolectado correctamente. Ya está EN VIAJE."
                    )
                )
                success = true
            }
        }
        return success
    }

    fun simulateDeliveryScan(shipmentId: Int, qrCodeContent: String): Boolean {
        var success = false
        viewModelScope.launch {
            val shipment = repository.shipmentDao.getShipmentById(shipmentId) ?: return@launch
            if (shipment.qrValueDelivery == qrCodeContent || qrCodeContent == "SIMULATE-BYPASS") {
                // Set to delivered
                repository.shipmentDao.updateShipmentStatus(shipmentId, "ENTREGADO")
                
                // RELEASE FUNDS:
                // Under Pata Cargo rules, the carrier receives 100% of their bid amount (shipment.price).
                // The sender was charged an additional 15% commission on top, which goes to Pata Cargo as a fee.
                val carrierId = shipment.carrierId ?: return@launch
                val carrier = repository.userDao.getUserById(carrierId)
                val ratePaid = shipment.price
                val commission = ratePaid * 0.15
                val netPaid = ratePaid // Carrier gets full 100% of the bid/offer

                if (carrier != null) {
                    repository.userDao.updateUser(carrier.copy(walletBalance = carrier.walletBalance + netPaid))
                }

                // Add to admin commission pool
                val adminUser = repository.userDao.getUserById("admin")
                if (adminUser != null) {
                    repository.userDao.updateUser(adminUser.copy(walletBalance = adminUser.walletBalance + commission))
                }

                repository.chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        shipmentId = shipmentId,
                        senderId = "system",
                        senderName = "Sistema Pata Cargo",
                        message = "¡Paquete entregado con éxito! Se han liberado $${String.format("%.2f", netPaid)} (100% de la oferta) al portador ${carrier?.name ?: ""}. Pata Cargo retuvo $${String.format("%.2f", commission)} de comisión pagada por el enviador."
                    )
                )
                success = true
            }
        }
        return success
    }

    fun submitDispute(shipmentId: Int) {
        viewModelScope.launch {
            repository.shipmentDao.updateShipmentStatus(shipmentId, "DISPUTADO")
            repository.chatMessageDao.insertMessage(
                ChatMessageEntity(
                    shipmentId = shipmentId,
                    senderId = "system",
                    senderName = "Soporte Pata Cargo",
                    message = "Se ha abierto una disputa sobre este envío. El administrador de plataforma intervendrá para mediar y resolver."
                )
            )
        }
    }

    fun resolveDispute(shipmentId: Int, refundSender: Boolean) {
        viewModelScope.launch {
            val shipment = repository.shipmentDao.getShipmentById(shipmentId) ?: return@launch
            if (refundSender) {
                // Refund sender: refund offer + 15% commission + insurance
                val totalCost = (shipment.price * 1.15) + if (shipment.insuranceEnabled) shipment.insuranceCost else 0.0
                val sender = repository.userDao.getUserById(shipment.senderId)
                if (sender != null) {
                    repository.userDao.updateUser(sender.copy(walletBalance = sender.walletBalance + totalCost))
                }
                repository.shipmentDao.updateShipmentStatus(shipmentId, "PENDIENTE") // Reset back to pending or canceled
                // Delete carrier assignment
                val updated = shipment.copy(carrierId = null, status = "PENDIENTE")
                repository.shipmentDao.updateShipment(updated)
            } else {
                // Complete delivery pay
                simulateDeliveryScan(shipmentId, "SIMULATE-BYPASS")
            }
        }
    }

    fun sendMessage(shipmentId: Int, messageText: String) {
        val senderId = selectedUserId.value
        viewModelScope.launch {
            val sender = repository.userDao.getUserById(senderId) ?: return@launch
            val msg = ChatMessageEntity(
                shipmentId = shipmentId,
                senderId = senderId,
                senderName = sender.name,
                message = messageText
            )
            repository.chatMessageDao.insertMessage(msg)
        }
    }

    fun submitReview(shipmentId: Int, rating: Int, comment: String, writerId: String, targetId: String) {
        viewModelScope.launch {
            val review = ReviewEntity(
                shipmentId = shipmentId,
                writerId = writerId,
                targetId = targetId,
                rating = rating,
                comment = comment
            )
            repository.reviewDao.insertReview(review)

            // Recalculate target's average rating dynamically!
            val targetUser = repository.userDao.getUserById(targetId)
            if (targetUser != null) {
                val reviews = repository.reviewDao.getReviewsForUserFlow(targetId).first()
                val totalStars = reviews.sumOf { it.rating } + rating
                val newAverage = totalStars.toFloat() / (reviews.size + 1)
                repository.userDao.updateUser(targetUser.copy(rating = newAverage))
            }
        }
    }

    // Admin Verification Process
    fun adminApproveCarrier(carrierId: String) {
        viewModelScope.launch {
            val user = repository.userDao.getUserById(carrierId)
            if (user != null) {
                repository.userDao.updateUser(user.copy(isVerified = true, isBiometricVerified = true))
            }
        }
    }

    // Carrier Biometric Selfie validation simulation
    fun triggerBiometricValidation(user: UserEntity) {
        carrierUnderSelfieVerification.value = user
    }

    fun completeBiometricSelfieCheck(isSuccess: Boolean) {
        val carrier = carrierUnderSelfieVerification.value ?: return
        viewModelScope.launch {
            if (isSuccess) {
                // Set biometric checked, which proceeds to admin approval state
                repository.userDao.updateUser(carrier.copy(isBiometricVerified = true))
            }
            carrierUnderSelfieVerification.value = null
        }
    }
}

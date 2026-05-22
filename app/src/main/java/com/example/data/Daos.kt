package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id != 'admin' AND isVerified = 0")
    fun getUsersPendingVerificationFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE carrierId = :carrierId")
    fun getRoutesByCarrierFlow(carrierId: String): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRouteById(id: Int)
}

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments ORDER BY timestamp DESC")
    fun getAllShipmentsFlow(): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE id = :id LIMIT 1")
    fun getShipmentByIdFlow(id: Int): Flow<ShipmentEntity?>

    @Query("SELECT * FROM shipments WHERE id = :id LIMIT 1")
    suspend fun getShipmentById(id: Int): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE senderId = :senderId ORDER BY timestamp DESC")
    fun getShipmentsBySenderFlow(senderId: String): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE carrierId = :carrierId ORDER BY timestamp DESC")
    fun getShipmentsByCarrierFlow(carrierId: String): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE status = 'PENDIENTE' ORDER BY timestamp DESC")
    fun getPendingShipmentsFlow(): Flow<List<ShipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: ShipmentEntity): Long

    @Update
    suspend fun updateShipment(shipment: ShipmentEntity)

    @Query("UPDATE shipments SET status = :status WHERE id = :id")
    suspend fun updateShipmentStatus(id: Int, status: String)

    @Query("UPDATE shipments SET carrierId = :carrierId, status = 'ACEPTADO' WHERE id = :id")
    suspend fun matchShipmentWithCarrier(id: Int, carrierId: String)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers WHERE shipmentId = :shipmentId ORDER BY amount ASC")
    fun getOffersForShipmentFlow(shipmentId: Int): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE carrierId = :carrierId")
    fun getOffersByCarrierFlow(carrierId: String): Flow<List<OfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferEntity)

    @Query("UPDATE offers SET status = :status WHERE id = :id")
    suspend fun updateOfferStatus(id: Int, status: String)

    @Query("UPDATE offers SET status = 'RECHAZADO' WHERE shipmentId = :shipmentId AND id != :acceptedOfferId")
    suspend fun rejectOtherOffers(shipmentId: Int, acceptedOfferId: Int)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE shipmentId = :shipmentId ORDER BY timestamp ASC")
    fun getMessagesForShipmentFlow(shipmentId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE targetId = :targetId ORDER BY timestamp DESC")
    fun getReviewsForUserFlow(targetId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

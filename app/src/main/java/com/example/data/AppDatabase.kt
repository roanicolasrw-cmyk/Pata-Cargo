package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        RouteEntity::class,
        ShipmentEntity::class,
        OfferEntity::class,
        ChatMessageEntity::class,
        ReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun routeDao(): RouteDao
    abstract fun shipmentDao(): ShipmentDao
    abstract fun offerDao(): OfferDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun reviewDao(): ReviewDao
}

package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PicnicDao {
    @Query("SELECT * FROM picnic_spots ORDER BY dateAdded DESC")
    fun getAllSpots(): Flow<List<PicnicSpot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: PicnicSpot): Long

    @Update
    suspend fun updateSpot(spot: PicnicSpot)

    @Delete
    suspend fun deleteSpot(spot: PicnicSpot)

    @Query("DELETE FROM picnic_spots WHERE id = :id")
    suspend fun deleteSpotById(id: Int)

    @Query("DELETE FROM picnic_spots")
    suspend fun clearAll()
}

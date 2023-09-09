package com.example.sewadalparkingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Insert
    suspend fun insert(vehicleEntity: VehicleEntity)

    @Delete
    suspend fun delete(vehicleEntity: VehicleEntity)

    @Update
    suspend fun update(vehicleEntity: VehicleEntity)

    @Query("SELECT * FROM 'vehicle-table'")
    fun fetchallVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM 'vehicle-table' WHERE id=:vehicleId")
    fun fetchvehicleById(vehicleId:Int): Flow<VehicleEntity>

    @Query("SELECT MAX(Id) FROM `vehicle-table`")
    suspend fun getMaxId(): Int?

    @Query("SELECT * FROM 'vehicle-table' WHERE Name=:vehiclename LIMIT 1")
    suspend fun getVechicleByName(vehiclename:String): VehicleEntity?

    @Query("UPDATE 'vehicle-table' SET Id = Id - 1 WHERE Id > :deletedId")
    suspend fun updateIdsAfterDeletion(deletedId: Int)

}
package com.example.sewadalparkingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogVehicleDAO {

    @Insert
    suspend fun insert(dialogVehicleEntity: DialogVehicleEntity)

    @Delete
    suspend fun delete(dialogVehicleEntity: DialogVehicleEntity)

    @Update
    suspend fun update(dialogVehicleEntity: DialogVehicleEntity)

    @Query("SELECT * FROM 'dialoglist-table'")
    fun fetchallDialogVehicles(): Flow<List<DialogVehicleEntity>>

    @Query("SELECT * FROM 'dialoglist-table' where DialogId =:Id")
    fun fetchDialogVehicleById(Id:Int): Flow<DialogVehicleEntity>

    @Query("SELECT MAX(DialogId) FROM 'dialoglist-table'")
    suspend fun getMaxId():Int?

    @Query("UPDATE 'dialoglist-table' SET DialogId = DialogId - 1 WHERE DialogId > :deletedId")
    suspend fun updateIdsAfterDeletion(deletedId: Int)

    @Query("SELECT COUNT(*) FROM 'dialoglist-table'")
    suspend fun getVehicleCount(): Int

    @Query("SELECT * FROM 'dialoglist-table' WHERE DialogVehicle_no = :name")
    suspend fun getVehicleByVehicle_no(name: String): DialogVehicleEntity?
}
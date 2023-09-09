package com.example.sewadalparkingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dialoglist-table")
data class DialogVehicleEntity (
    @PrimaryKey(autoGenerate = true)
    val DialogId:Int,

    val DialogName:String,
    val DialogVehicle_no:String,
    val DialogMobile_no:String,
    val DialogModel:String
    )
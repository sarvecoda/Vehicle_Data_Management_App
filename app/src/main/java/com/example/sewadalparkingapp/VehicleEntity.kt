package com.example.sewadalparkingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle-table")
class VehicleEntity (

    @PrimaryKey(autoGenerate = false)
    val Id:Int = 0,
    val Name:String = "",
    val Vehicle_no:String = "",
    val Model:String = "",
    val Mobile_no:String = ""
    )
package com.example.sewadalparkingapp

import android.app.Application

class VehicleApp:DialogVehicleApp() {
    val db by lazy{
        VehicleDatabase.getInstance(this)
    }
}
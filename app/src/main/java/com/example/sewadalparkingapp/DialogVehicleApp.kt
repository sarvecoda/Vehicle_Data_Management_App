package com.example.sewadalparkingapp

import android.app.Application

open class DialogVehicleApp:Application() {
    val dialogdb by lazy {
        DialogVehicleDatabase.getInstance(this)
    }
}
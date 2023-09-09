package com.example.sewadalparkingapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DialogVehicleEntity::class], version = 9)
abstract class DialogVehicleDatabase:RoomDatabase() {

    abstract fun dialogvehicleDao():DialogVehicleDAO

    companion object{

        @Volatile
        var INSTANCE:DialogVehicleDatabase? = null

        fun getInstance(context: Context):DialogVehicleDatabase{

            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext, DialogVehicleDatabase::class.java, "dialogvehicle_database")
                        .fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
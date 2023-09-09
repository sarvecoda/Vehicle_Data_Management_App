package com.example.sewadalparkingapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VehicleEntity::class], version = 9)
abstract class VehicleDatabase:RoomDatabase() {

    abstract fun vehicleDao():VehicleDao

    companion object{
        @Volatile
        var INSTANCE:VehicleDatabase? = null

        fun getInstance(context: Context):VehicleDatabase{

            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext, VehicleDatabase::class.java, "vehicle_database")
                        .fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}
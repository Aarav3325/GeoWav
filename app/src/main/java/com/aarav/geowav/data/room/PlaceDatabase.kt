package com.aarav.geowav.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.place.Place

@Database(entities = [Place::class, GeoConnection::class], version = 3)
abstract class PlaceDatabase : RoomDatabase() {

    abstract val placeDao : PlacesDAO
    abstract val connectionDao : ConnectionDao

    companion object{

        @Volatile
        private var INSTANCE : PlaceDatabase? = null

        fun getInstance(context: Context) : PlaceDatabase{

            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(context,
                        PlaceDatabase::class.java,
                        "place_database")
                        .fallbackToDestructiveMigration()
                        .build()
                }

                INSTANCE = instance
                return instance
            }
        }
    }
}
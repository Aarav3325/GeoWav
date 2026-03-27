package com.aarav.geowav.data.datasource.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.SessionHistory

@Database(entities = [Place::class], version = 9)
abstract class PlaceDatabase : RoomDatabase() {

    abstract val placeDao : PlacesDAO

    companion object {

        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        fun getInstance(context: Context): PlaceDatabase {

            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        PlaceDatabase::class.java,
                        "place_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }

                INSTANCE = instance
                return instance
            }
        }
    }
}
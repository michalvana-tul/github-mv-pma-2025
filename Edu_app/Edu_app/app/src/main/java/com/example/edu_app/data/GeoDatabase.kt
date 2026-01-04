package com.example.edu_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, CountryEntity::class, GameResultEntity::class], version = 3)
abstract class GeoDatabase : RoomDatabase() {
    abstract fun geoDao(): GeoDao

    companion object {
        @Volatile
        private var INSTANCE: GeoDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GeoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeoDatabase::class.java,
                    "geo_master_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Ensure data is there every time it opens
                        INSTANCE?.let { database ->
                            scope.launch(Dispatchers.IO) {
                                val dao = database.geoDao()
                                if (dao.getAllCountries().isEmpty()) {
                                    dao.insertCountries(listOf(
                                        CountryEntity(countryName = "France", capitalCity = "Paris", region = "Europe"),
                                        CountryEntity(countryName = "Germany", capitalCity = "Berlin", region = "Europe"),
                                        CountryEntity(countryName = "Japan", capitalCity = "Tokyo", region = "Asia"),
                                        CountryEntity(countryName = "UK", capitalCity = "London", region = "Europe"),
                                        CountryEntity(countryName = "Italy", capitalCity = "Rome", region = "Europe"),
                                        CountryEntity(countryName = "USA", capitalCity = "Washington D.C.", region = "Americas")
                                    ))
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

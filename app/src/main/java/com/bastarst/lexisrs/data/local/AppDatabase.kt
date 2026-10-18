package com.bastarst.lexisrs.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bastarst.lexisrs.data.local.dao.WordDao
import com.bastarst.lexisrs.data.local.entity.Word

@Database(entities = [Word::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        private const val DB_NAME = "lexisrs.db"
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DB_NAME
                    ).fallbackToDestructiveMigration(false).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
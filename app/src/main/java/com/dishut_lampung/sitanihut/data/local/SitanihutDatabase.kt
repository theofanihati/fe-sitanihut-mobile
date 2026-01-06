package com.dishut_lampung.sitanihut.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dishut_lampung.sitanihut.data.local.dao.CommodityDao
import com.dishut_lampung.sitanihut.data.local.dao.KphDao
import com.dishut_lampung.sitanihut.data.local.dao.KthDao
import com.dishut_lampung.sitanihut.data.local.dao.PenyuluhDao
import com.dishut_lampung.sitanihut.data.local.dao.RemoteKeysDao
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.local.entity.KphEntity
import com.dishut_lampung.sitanihut.data.local.entity.KthEntity
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import com.dishut_lampung.sitanihut.data.local.entity.RemoteKeys
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ReportEntity::class,
        RoleEntity::class,
        RemoteKeys::class,
        CommodityEntity::class,
        PenyuluhEntity::class,
//        PetaniEntity::class,
        KthEntity::class,
        KphEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class SitanihutDatabase : RoomDatabase(){
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao
    abstract fun roleDao(): RoleDao
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun commodityDao(): CommodityDao
    abstract fun penyuluhDao(): PenyuluhDao
//    abstract fun petaniDao(): PetaniDao
    abstract fun kthDao(): KthDao
    abstract fun kphDao(): KphDao

    companion object {
//        val MIGRATION = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // perintah SQL
//                database.execSQL("ALTER TABLE petani ADD COLUMN jabatan TEXT")
//            }
//        }

        @Volatile
        private var INSTANCE: SitanihutDatabase? = null
        fun getInstance(context: Context): SitanihutDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SitanihutDatabase::class.java,
                    "sitanihut_database.db"
                )
//                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

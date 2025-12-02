package com.dishut_lampung.sitanihut.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dishut_lampung.sitanihut.data.local.dao.RemoteKeysDao
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
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
//        PetaniEntity::class,
//        PenyuluhEntity::class,
//        KthEntity::class,
//        KomoditasEntity::class,
//        LaporanEntity::class,
//        MasaTanamEntity::class,
//        MasaPanenEntity::class,
//        LampiranEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SitanihutDatabase : RoomDatabase(){
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao
    abstract fun roleDao(): RoleDao
    abstract fun remoteKeysDao(): RemoteKeysDao
//    abstract fun petaniDao(): PetaniDao
//    abstract fun penyuluhDao(): PenyuluhDao
//    abstract fun kthDao(): KthDao
//    abstract fun komoditasDao(): KomoditasDao
//    abstract fun laporanDao(): LaporanDao
//    abstract fun masaTanamDao(): MasaTanamDao
//    abstract fun masaPanenDao(): MasaPanenDao
//    abstract fun lampiranDao(): LampiranDao

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

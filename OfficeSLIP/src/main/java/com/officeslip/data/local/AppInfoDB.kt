package com.officeslip.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.officeslip.data.local.dao.AppInfoDao
import com.officeslip.data.model.AppInfo


@Database(entities = [AppInfo::class], version = 1, exportSchema = false)
abstract class AppInfoDB: RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao

//    companion object {
//
//        fun getInstance(context: Context, scope: CoroutineScope): AppInfoDB? {
//
//            if (INSTANCE == null) {
//                synchronized(AppInfoDB::class) {
//
//                    //Default sqlite
////                    INSTANCE = Room.databaseBuilder(context.applicationContext,
////                        AppInfoDB::class.java, AppInfoDB::class.java.simpleName)
////                        .fallbackToDestructiveMigration()
////                        .build()
////
//                    //sqlcipher
//                 //   I
////                    val passphrase = SQLiteDatabase.getBytes(APP_SQLCIPHER_KEY.toCharArray())
////                    val factory = SupportFactory(passphrase)
////                    INSTANCE = Room.databaseBuilder(context.applicationContext,
////                        AppInfoDB::class.java, AppInfoDB::class.java.simpleName)
////                        .openHelperFactory(factory)
////                        .fallbackToDestructiveMigration()
////                        .addCallback(Callback(scope))
////                        .build()
//                }
//            }
//            return INSTANCE
//        }
//
//        fun destroyInstance() {
//            INSTANCE = null
//        }
//    }
//
//    private class Callback(
//        private val scope: CoroutineScope
//    ) : RoomDatabase.Callback() {
//
//        override fun onOpen(db: SupportSQLiteDatabase) {
//            super.onOpen(db)
//            INSTANCE?.let { database ->
//                scope.launch(Dispatchers.IO) {
//                    populateDatabase(database.appInfoDao())
//                }
//            }
//        }
//
//        /**
//         * Populate the database in a new coroutine.
//         * If you want to start with more words, just add them.
//         */
//        suspend fun populateDatabase(systemDao: AppInfoDao) {
//            // Start the app with a clean database every time.
//            // Not needed if you only populate on creation.
////            systemDao.deleteAll()
////
////            var word = Word("Hello")
////            wordDao.insert(word)
////            word = Word("World!")
////            wordDao.insert(word)
//        }
//    }
}

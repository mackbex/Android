package com.officeslip.di.module

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.officeslip.APP_SQLCIPHER_KEY
import com.officeslip.data.local.AppInfoDB
import com.officeslip.data.local.dao.AppInfoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SqliteModuleModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppInfoDB {

        //Default sqlite
//         INSTANCE = Room.databaseBuilder(context.applicationContext,
//          AppInfoDB::class.java, AppInfoDB::class.java.simpleName)
//          .fallbackToDestructiveMigration()
//          .build()

        val passphrase = SQLiteDatabase.getBytes(APP_SQLCIPHER_KEY.toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context,
            AppInfoDB::class.java, AppInfoDB::class.java.simpleName)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {

                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    /**
                     * Insert default value.
                     */

                    //  db.searchMenuDao().insert(SearchMenu(R.drawable.something, androidContext().getString(R.string.something)))
                    //  db.searchMenuDao().insert(SearchMenu(R.drawable.something, androidContext().getString(R.string.something)))
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)

                    val scope = CoroutineScope(GlobalScope.coroutineContext)
                    db.let { _ ->
                        scope.launch(Dispatchers.IO) {
                            /**
                             * Callback every open
                             */
                        }
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideDao(database: AppInfoDB): AppInfoDao {
        return database.appInfoDao()
    }
}

package com.officeslip.data.local.dao

import androidx.room.*
import com.officeslip.data.model.AppInfo


@Dao
interface AppInfoDao {
    @Query("SELECT * FROM SYSINFO_T")
    fun getAll(): List<AppInfo>

    @Query("SELECT * FROM SYSINFO_T WHERE OPTION LIKE :option LIMIT 1")
    fun getItem(option: String): AppInfo

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(system: AppInfo): Long

    @Delete
    fun delete(system: AppInfo)

    @Update
    fun update(vararg system: AppInfo)

    @Query("DELETE FROM SYSINFO_T")
    fun deleteAll()

    @Transaction
    fun upsert(system: AppInfo) {
        val id = insert(system)
        if (id == -1L) {
            update(system)
        }
    }
}

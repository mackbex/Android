package com.officeslip.data.local

import com.officeslip.data.local.dao.AppInfoDao
import com.officeslip.data.model.AppInfo
import javax.inject.Inject

class AppInfoRepositoryImpl @Inject constructor(
    private val db:AppInfoDao
):AppInfoRepository {
    override fun getAll(): List<AppInfo> {
        return db.getAll()
    }

    override fun getItem(option: String): AppInfo {
        return db.getItem(option)
    }

    override fun insert(system: AppInfo): Long {
        return db.insert(system)
    }

    override fun delete(system: AppInfo) {
        return db.delete(system)
    }

    override fun update(vararg system: AppInfo) {
        db.update(*system)
    }

    override fun deleteAll() {
        db.deleteAll()
    }

    override fun upsert(system: AppInfo) {
        db.upsert(system)
    }
}
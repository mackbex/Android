package com.officeslip.data.local

import com.officeslip.data.model.AppInfo

interface AppInfoRepository {
    fun getAll():List<AppInfo>
    fun getItem(option: String):AppInfo
    fun insert(system:AppInfo):Long
    fun delete(system:AppInfo)
    fun update(vararg system:AppInfo)
    fun deleteAll()
    fun upsert(system:AppInfo)
}
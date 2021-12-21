package com.officeslip.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SYSINFO_T")
data class AppInfo(

  //  @PrimaryKey(autoGenerate = true) val id:Int,
   // @ColumnInfo(name = "OPTION") val OPTION: String?,
    @PrimaryKey val OPTION : String,
    @ColumnInfo(name = "VALUE") val VALUE: String?
)
//{
//    constructor() : this(0, "", "")
//}
package com.sevengod.maibud

import android.app.Application
import com.sevengod.maibud.instances.RetrofitInstance
import com.sevengod.maibud.utils.DBUtils
import com.sevengod.maibud.utils.DSUtils
import com.sevengod.maibud.utils.SongUtil


class MaiBudApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化数据库
        DBUtils.getDatabase(applicationContext)
        //初始化RetrofitInstance的Context
        RetrofitInstance.initialize(applicationContext)
    }
}
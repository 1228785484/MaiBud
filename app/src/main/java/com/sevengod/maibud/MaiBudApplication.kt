package com.sevengod.maibud

import android.app.Application
import com.sevengod.maibud.instances.DataBaseInstance
import com.sevengod.maibud.instances.RetrofitInstance
import com.sevengod.maibud.utils.DSUtils
import com.sevengod.maibud.utils.SongUtil


class MaiBudApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化数据库
        DataBaseInstance.getInstance(applicationContext)
        //初始化RetrofitInstance的Context
        RetrofitInstance.initialize(applicationContext)
    }
}
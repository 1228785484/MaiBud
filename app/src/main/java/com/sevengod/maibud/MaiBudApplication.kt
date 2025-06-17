package com.sevengod.maibud

import android.app.Application
import com.sevengod.maibud.utils.DBUtils
import com.sevengod.maibud.utils.DSUtils
import com.sevengod.maibud.utils.SongUtil


class MaiBudApplication: Application() {

    override fun onCreate() {

        super.onCreate()
        //初始化
        DBUtils.getDatabase(applicationContext)
    }
}
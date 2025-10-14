package com.nomade.movilremiscar.remiscarmovil

import android.app.Application
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPrefsUtil.init(this)

        /*Bugfender.init(this, "xnj71KFflAedkW1RE1hB1j8EW62ZkUjo", BuildConfig.DEBUG, true)
        Bugfender.enableCrashReporting()
        Bugfender.enableUIEventLogging(this)*/
        //Bugfender.enableLogcatLogging() // optional, if you want logs automatically collected from logcat
    }

}
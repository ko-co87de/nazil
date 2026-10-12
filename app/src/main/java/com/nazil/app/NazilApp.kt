package com.nazil.app

import android.app.Application
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class NazilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            // Library failed to initialize; downloads will report errors at use time.
        }
    }
}

package ru.netology.craftify.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.netology.craftify.auth.AppAuth

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        AppAuth.init(this)
    }
}
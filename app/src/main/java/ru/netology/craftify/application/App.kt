package ru.netology.craftify.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.craftify.BuildConfig
import ru.netology.craftify.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("${BuildConfig.API_KEY}")
        MapKitFactory.initialize(this);
        AppAuth.init(this)
        setupAuth()
    }

    private fun setupAuth() {
        appScope.launch {
        }
    }
}
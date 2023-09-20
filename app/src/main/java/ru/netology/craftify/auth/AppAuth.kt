package ru.netology.craftify.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.craftify.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"
    private val nameKey = "name"
    private val _data = MutableStateFlow(Token())
    val data = _data.asStateFlow()
    private val _authStateFlow: MutableStateFlow<AuthState>

    companion object{
        private var INSTANCE: AppAuth? = null

        fun init(context: Context){
            INSTANCE = AppAuth(context)
        }
    }

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)
        val name = prefs.getString(nameKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token, name))
        }
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String?, name: String?) {
        _authStateFlow.value = AuthState(id, token, name)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            putString(nameKey, name)
            apply()
        }
    }

    @Synchronized
    fun remove() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null, val name: String? = null)
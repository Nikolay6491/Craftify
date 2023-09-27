package ru.netology.craftify.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.R
import ru.netology.craftify.activity.JobFragment.Companion.user_Id
import ru.netology.craftify.activity.WallFragment.Companion.userId
import ru.netology.craftify.auth.AppAuth
import ru.netology.craftify.databinding.ActivityAppBinding
import ru.netology.craftify.viewmodel.SignInViewModel
import javax.inject.Inject

val coordinatesSaratov = Point(51.530161, 46.061777)

val date = "dd.MM.yyyy"
val time = "HH:mm"


@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: AppAuth
    private val singInViewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        singInViewModel.data.observe(this){
            invalidateOptionsMenu()
            if (it.id == 0L) {
                findNavController(R.id.container)
                    .navigate(R.id.signInFragment)
            } else {
                val welcome = getString(R.string.welcome)
                Toast.makeText(this@AppActivity,"$welcome ${it.name}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.wall -> {
                findNavController(R.id.container).navigate(R.id.wallFragment,
                    Bundle().apply {
                        userId = auth.authStateFlow.value.id
                    })
                true
            }
            R.id.posts -> {
                findNavController(R.id.container).navigate(R.id.feedFragment)
                true
            }
            R.id.events -> {
                findNavController(R.id.container).navigate(R.id.feedEventFragment)
                true
            }
            R.id.jobs -> {
                findNavController(R.id.container).navigate(R.id.jobFragment,
                    Bundle().apply {
                        user_Id = auth.authStateFlow.value.id
                    })

                true
            }
            R.id.signout -> {
                auth.remove()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
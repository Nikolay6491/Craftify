package ru.netology.craftify.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.databinding.FragmentSignInBinding
import ru.netology.craftify.viewmodel.PostViewModel
import ru.netology.craftify.R
import ru.netology.craftify.util.AndroidUtils.hideKeyboard
import ru.netology.craftify.view.afterTextChanged
import ru.netology.craftify.view.loadCircleCrop
import ru.netology.craftify.viewmodel.SignInViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private var fragmentBinding: FragmentSignInBinding? = null

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private val viewModelSing: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        with(binding) {
            login.requestFocus()

            checkBoxRegister.setOnClickListener {
                name.isVisible = checkBoxRegister.isChecked
                avatarImage.isVisible = checkBoxRegister.isChecked
                avatarImage.setImageResource(R.mipmap.ic_launcher_craftify_round)
            }

            login.afterTextChanged {
                viewModelSing.loginDataChanged(
                    login.text.toString(),
                    password.text.toString()
                )
            }

            password.afterTextChanged {
                viewModelSing.loginDataChanged(
                    login.text.toString(),
                    password.text.toString()
                )
            }

            val pickPhotoLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    when (it.resultCode) {
                        ImagePicker.RESULT_ERROR -> {
                            Snackbar.make(
                                binding.root,
                                ImagePicker.getError(it.data),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.data
                            viewModelSing.changeAvatar(uri, uri?.toFile())
                        }
                    }
                }

            binding.avatarImage.setOnClickListener {
                ImagePicker.with(this@SignInFragment)
                    .crop()
                    .compress(512)
                    .provider(ImageProvider.GALLERY)
                    .galleryMimeTypes(
                        arrayOf(
                            "image/png",
                            "image/jpeg",
                        )
                    )
                    .createIntent(pickPhotoLauncher::launch)
            }

            button.setOnClickListener {
                hideKeyboard(requireView())

                if (checkBoxRegister.isChecked) {
                    viewModelSing.userRegistration(
                        binding.login.text.toString(),
                        binding.password.text.toString(),
                        binding.name.text.toString()
                    )
                } else {
                    viewModelSing.userAuthentication(
                        binding.login.text.toString(),
                        binding.password.text.toString()
                    )
                }
            }

            viewModelSing.loginFormState.observe(viewLifecycleOwner) { state ->
                button.isEnabled = state.isDataValid
                loading.isVisible = state.isLoading
                if (state.isError) {
                    Toast.makeText(context, "Ошибка при авторизации", Toast.LENGTH_LONG)
                        .show()
                }
            }

            viewModelSing.data.observe(viewLifecycleOwner) {
                if (it.id != 0L)
                    findNavController().navigateUp()
            }

            viewModelSing.photoAvatar.observe(viewLifecycleOwner) {
                if (it.uri == null) {
                    avatarImage.setImageResource(R.mipmap.ic_launcher_craftify_round)
                    return@observe
                }
                avatarImage.loadCircleCrop(it.uri.toString())
            }

            return root
        }
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
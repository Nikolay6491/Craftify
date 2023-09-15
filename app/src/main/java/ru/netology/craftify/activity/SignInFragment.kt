package ru.netology.craftify.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.databinding.FragmentSignInBinding
import ru.netology.craftify.util.AndroidUtils
import ru.netology.craftify.viewmodel.PostViewModel
import ru.netology.craftify.viewmodel.SignInViewModel

@AndroidEntryPoint
class SignInFragment: Fragment() {

    private val viewModel: SignInViewModel by activityViewModels()

    private val postViewModel: PostViewModel by activityViewModels()

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



        viewModel.dataState.observe(viewLifecycleOwner){state ->
            binding.loading.isVisible = state.loading
            binding.signIn.isVisible = !state.loading
            binding.incorrect.isVisible = state.error
            if (state.success){
                postViewModel.refresh()
                viewModel.clean()
                findNavController().navigateUp()
            }
        }

        binding.signIn.setOnClickListener{
            binding.incorrect.isVisible = false
            binding.emptyField.isVisible = false

            AndroidUtils.hideKeyboard(binding.root)

            val login = binding.loginField.text.toString().trim()
            val password = binding.passwordField.text.toString()

            if (login.isBlank() || password.isBlank()){
                binding.emptyField.isVisible = true
            } else {
                viewModel.signIn(login,password)
            }
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    viewModel.clean()
                    findNavController().navigateUp()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )

        return binding.root
    }
}
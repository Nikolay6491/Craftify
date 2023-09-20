package ru.netology.craftify.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.databinding.FragmentImageBinding
import ru.netology.craftify.util.StringArg
import ru.netology.craftify.view.load

@AndroidEntryPoint
class ImageFragment  : Fragment()  {

    companion object{
        var Bundle.textArg: String? by StringArg
    }

    private var fragmentBinding: FragmentImageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        val url = arguments?.textArg
        if (url !=null) binding.imageView.load(url)
        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
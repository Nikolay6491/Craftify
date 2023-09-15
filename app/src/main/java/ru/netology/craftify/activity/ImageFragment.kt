package ru.netology.craftify.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.R
import ru.netology.craftify.databinding.FragmentImageBinding
import ru.netology.craftify.viewmodel.PostViewModel

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentImageBinding.inflate(
        inflater,
        container,
        false
    ).also { binding ->

        viewModel.currentPost.observe(viewLifecycleOwner) {
            val currentPost = it
            if (currentPost != null) {

                if (currentPost.attachment?.url != null) {
                    Glide.with(binding.postImage)
                        .load("http://10.0.2.2:9999/media/${currentPost.attachment.url}")
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(binding.postImage)
                    binding.postImage.isVisible = true
                } else binding.postImage.isVisible = false
                if (currentPost.likedByMe) {
                    binding.bottomAppBar.menu?.findItem(R.id.like)
                        ?.setIcon(R.drawable.ic_baseline_like_24)
                } else {
                    binding.bottomAppBar.menu?.findItem(R.id.like)
                        ?.setIcon(R.drawable.ic_baseline_like_border_24)
                }

                binding.bottomAppBar.setNavigationOnClickListener {
                    viewModel.getPostById(null)
                    findNavController().navigateUp()
                }
                binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.like -> {
                            viewModel.likesById(currentPost.id, currentPost.likedByMe)
                            true
                        }
                        R.id.share -> {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, currentPost.content)
                                type = "text/plain"
                            }

                            val shareIntent =
                                Intent.createChooser(
                                    intent,
                                    getString(R.string.share)
                                )
                            startActivity(shareIntent)
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }.root
}
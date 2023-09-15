package ru.netology.craftify.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.R
import ru.netology.craftify.activity.NewPostFragment.Companion.textArg
import ru.netology.craftify.adapter.OnInteractionListener
import ru.netology.craftify.adapter.PostsAdapter
import ru.netology.craftify.databinding.FragmentPostBinding
import ru.netology.craftify.dto.Post
import ru.netology.craftify.util.AuthReminder
import ru.netology.craftify.viewmodel.AuthViewModel
import ru.netology.craftify.viewmodel.PostViewModel

@AndroidEntryPoint
class PostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by activityViewModels()
        val authViewModel: AuthViewModel by activityViewModels()
        PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onLike(post: Post) {
                if(authViewModel.authorized){
                    super.onLike(post)
                }else{
                    AuthReminder.remind(binding.root, "You should sign in to like posts!", this@PostFragment)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                if(authViewModel.authorized){
                    super.onShare(post)
                }else{
                    AuthReminder.remind(binding.root, "You should sign in to share posts!", this@PostFragment)
                }
            }

            override fun playVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }

            override fun getPostById(id: Long){
                viewModel.getPostById(id)
            }
        })

        return binding.root
    }
}
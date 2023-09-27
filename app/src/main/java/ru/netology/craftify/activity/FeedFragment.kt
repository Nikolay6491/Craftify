package ru.netology.craftify.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.R
import ru.netology.craftify.activity.ImageFragment.Companion.textArg
import ru.netology.craftify.activity.MapsFragment.Companion.doubleArg1
import ru.netology.craftify.activity.MapsFragment.Companion.doubleArg2
import ru.netology.craftify.activity.WallFragment.Companion.userId
import ru.netology.craftify.activity.WallFragment.Companion.userAvatar
import ru.netology.craftify.activity.WallFragment.Companion.userName
import ru.netology.craftify.activity.WallFragment.Companion.userPosition
import ru.netology.craftify.adapter.OnInteractionListener
import ru.netology.craftify.adapter.PostsAdapter
import ru.netology.craftify.databinding.FragmentFeedBinding
import ru.netology.craftify.dto.Post
import ru.netology.craftify.model.FeedModelState
import ru.netology.craftify.viewmodel.PostViewModel

@Suppress("DEPRECATION")
@AndroidEntryPoint
class FeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostViewModel by activityViewModels()

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }

            override fun onLike(post: Post) {
                viewModel.likesById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)

            }

            override fun onMap(post: Post) {
                if (post.coordinates?.lat != null && post.coordinates.long != null) {
                    findNavController().navigate(R.id.action_feedFragment_to_mapsFragment,
                        Bundle().apply {
                            doubleArg1 = post.coordinates.lat.toDouble()
                            doubleArg2 = post.coordinates.long.toDouble()
                        })
                }
            }

            override fun onImage(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    })

            }

            override fun onWall(
                userId: Long,
                userName: String,
                userPosition: String?,
                userAvatar: String?
            ) {
                findNavController().navigate(R.id.action_feedFragment_to_wallFragment,
                    Bundle().apply {
                        this.userId = userId
                        this.userName = userName
                        this.userPosition = userPosition
                        this.userAvatar = userAvatar
                    })
            }
        })
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            binding.refresh.isRefreshing = state is FeedModelState.Refresh
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.load() }
                    .show()
            }
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.empty.isVisible = state.empty
        }

        binding.refresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}
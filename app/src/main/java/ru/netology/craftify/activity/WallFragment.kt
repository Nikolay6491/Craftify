package ru.netology.craftify.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.craftify.R
import ru.netology.craftify.activity.JobFragment.Companion.user_Id
import ru.netology.craftify.activity.ImageFragment.Companion.textArg
import ru.netology.craftify.activity.MapsFragment.Companion.doubleArg1
import ru.netology.craftify.activity.MapsFragment.Companion.doubleArg2
import ru.netology.craftify.adapter.OnInteractionWallListener
import ru.netology.craftify.adapter.PostWallAdapter
import ru.netology.craftify.databinding.FragmentWallBinding
import ru.netology.craftify.dto.Post
import ru.netology.craftify.model.FeedModelState
import ru.netology.craftify.util.LongArg
import ru.netology.craftify.util.StringArg
import ru.netology.craftify.view.loadCircleCrop
import ru.netology.craftify.viewmodel.PostViewModel

@AndroidEntryPoint
class WallFragment : Fragment() {

    companion object {
        var Bundle.userId: Long by LongArg
        var Bundle.userName: String? by StringArg
        var Bundle.userPosition: String? by StringArg
        var Bundle.userAvatar: String? by StringArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWallBinding.inflate(inflater, container, false)

        val currentUser = viewModel.getCurrentUser()

        val userId = (arguments?.userId ?: 0).toLong()
        val userName = arguments?.userName
        val userPosition = arguments?.userPosition
        val userAvatar = arguments?.userAvatar

        if (currentUser == userId) {
            binding.fab.visibility = View.VISIBLE
            binding.avatar.visibility = View.GONE
            binding.author.visibility = View.GONE
            binding.authorJob.visibility = View.GONE
        } else {
            binding.fab.visibility = View.GONE
            binding.avatar.visibility = View.VISIBLE
            binding.author.visibility = View.VISIBLE

            if (userAvatar != null)
                binding.avatar.loadCircleCrop(userAvatar)
            else binding.avatar.setImageResource(R.mipmap.ic_launcher_craftify_round)

            binding.author.text = userName

            if (userPosition.isNullOrBlank()) {
                binding.authorJob.visibility = View.GONE
            } else {
                binding.authorJob.text = userPosition
                binding.authorJob.visibility = View.VISIBLE
            }
        }

        val adapter = PostWallAdapter(object : OnInteractionWallListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_wallFragment_to_newPostFragment)
            }

            override fun onLike(post: Post) {
                viewModel.likesById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)

            }

            override fun onMap(post: Post) {
                if (post.coordinates != null && post.coordinates.lat != null && post.coordinates.long != null) {
                    findNavController().navigate(R.id.action_wallFragment_to_mapsFragment,
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
        })
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            binding.swiperefresh.isRefreshing = state is FeedModelState.Refresh
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.load() }
                    .show()
            }
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts.filter {
                it.authorId == userId
            })
            binding.emptyText.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_wallFragment_to_newPostFragment)
        }

        binding.author.setOnClickListener {
            findNavController().navigate(R.id.action_wallFragment_to_feedJobsFragment,
                Bundle().apply {
                    user_Id = userId
                })

        }

        binding.avatar.setOnClickListener {
            findNavController().navigate(R.id.action_wallFragment_to_feedJobsFragment,
                Bundle().apply {
                    user_Id = userId
                })

        }

        return binding.root
    }
}
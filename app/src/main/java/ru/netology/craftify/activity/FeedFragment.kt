package ru.netology.craftify.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.craftify.R
import ru.netology.craftify.activity.NewPostFragment.Companion.textArg
import ru.netology.craftify.adapter.OnInteractionListener
import ru.netology.craftify.adapter.PostLoadingStateAdapter
import ru.netology.craftify.adapter.PostsAdapter
import ru.netology.craftify.databinding.FragmentFeedBinding
import ru.netology.craftify.dto.Post
import ru.netology.craftify.model.FeedModelState

import ru.netology.craftify.util.AuthReminder
import ru.netology.craftify.viewmodel.AuthViewModel

import ru.netology.craftify.viewmodel.PostViewModel

object PostService {
    fun showValues(value: Long): String {
        val valueToString = value.toString()
        var displayValue = ""
        when (value) {
            in 0..999 -> displayValue = value.toString()
            in 1_000..9_999 -> {
                displayValue = valueToString[0].toString() + "." + valueToString[1].toString() + "К"
            }
            in 10_000..99_999 -> {
                displayValue = valueToString[0].toString() + valueToString[1].toString() + "К"
            }
            in 100_000..999_999 -> {
                displayValue =
                    valueToString[0].toString() + valueToString[1].toString() + valueToString[2].toString() + "К"
            }
            in 1_000_000..Long.MAX_VALUE -> {
                displayValue = valueToString[0].toString() + "." + valueToString[1].toString() + "М"
            }
        }
        return displayValue
    }
}

@Suppress("DEPRECATION")
@AndroidEntryPoint
class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by activityViewModels()
        val authViewModel: AuthViewModel by activityViewModels()
        val adapter = PostsAdapter(object : OnInteractionListener {
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
                viewModel.likesById(post.id, post.likedByMe)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(intent, getString(R.string.share))
                startActivity(shareIntent)
            }

            override fun playVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }

            override fun getPostById(id: Long) {
                viewModel.getPostById(id)
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter {
                adapter.retry()
            },
            footer = PostLoadingStateAdapter {
                adapter.retry()
            }
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        authViewModel.state.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.refresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry) {
                        viewModel.load()
                    }
                    .show()
            }

            binding.refresh.isRefreshing = state is FeedModelState.Refresh
        }

//        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                if (positionStart == 0) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        })

        binding.fabTop.setOnClickListener {
            viewModel.loadVisiblePosts()
            binding.fabTop.isVisible = false
        }


        binding.refresh.setOnRefreshListener {
            viewModel.loadVisiblePosts()
            adapter.refresh()

        }

        binding.fab.setOnClickListener {
            if (authViewModel.authorized) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                AuthReminder.remind(
                    binding.root,
                    "You should sign in to share posts!",
                    this@FeedFragment
                )
            }
        }

        return binding.root
    }
}
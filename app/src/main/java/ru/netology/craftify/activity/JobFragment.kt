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
import ru.netology.craftify.adapter.JobAdapter
import ru.netology.craftify.adapter.OnInteractionJobListener
import ru.netology.craftify.databinding.FragmentJobBinding
import ru.netology.craftify.dto.Job
import ru.netology.craftify.model.FeedModelState
import ru.netology.craftify.util.LongArg
import ru.netology.craftify.viewmodel.PostViewModel

@AndroidEntryPoint
class JobFragment : Fragment() {
    companion object{
        var Bundle.user_Id: Long by LongArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostViewModel by activityViewModels()

        val binding = FragmentJobBinding.inflate(inflater, container, false)

        val userId = (arguments?.user_Id ?: 0).toLong()

        val currentUser = viewModel.getCurrentUser()
        viewModel.loadJobs(userId, currentUser)

        if (currentUser == userId)
            binding.fab.visibility = View.VISIBLE
        else
            binding.fab.visibility = View.GONE

        val adapter = JobAdapter(object : OnInteractionJobListener {
            override fun onEdit(job: Job) {
                viewModel.editJob(job)
                findNavController().navigate(R.id.action_jobFragment_to_newJobFragment)
            }

            override fun onRemove(job: Job) {
                viewModel.removeJobById(job.id)

            }
        })

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            binding.swiperefresh.isRefreshing = state is FeedModelState.Refresh
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.loadEvent() }
                    .show()
            }
        }
        viewModel.dataJobs.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.jobs.filter {
                it.userId == userId
            })
            binding.emptyText.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshJobs(userId, currentUser)
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_jobFragment_to_newJobFragment)
        }

        return binding.root
    }
}
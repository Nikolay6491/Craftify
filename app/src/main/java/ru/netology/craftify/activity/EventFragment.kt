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
import ru.netology.craftify.activity.MapsFragment.Companion.doubleArg1
import ru.netology.craftify.activity.MapsFragment.Companion.doubleArg2
import ru.netology.craftify.adapter.EventAdapter
import ru.netology.craftify.adapter.OnInteractionEventListener
import ru.netology.craftify.databinding.FragmentEventBinding
import ru.netology.craftify.dto.Event
import ru.netology.craftify.model.FeedModelState
import ru.netology.craftify.viewmodel.PostViewModel


@AndroidEntryPoint
class EventFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventBinding.inflate(inflater, container, false)

        val adapter = EventAdapter(object : OnInteractionEventListener {
            override fun onEdit(event: Event) {
                viewModel.editEvent(event)
                findNavController().navigate(R.id.action_eventFragment_to_newEventFragment)
            }

            override fun onLike(event: Event) {
                viewModel.likeEventById(event.id, event.likedByMe)
            }

            override fun onRemove(event: Event) {
                viewModel.removeEventById(event.id)

            }

            override fun onParticipate(event: Event) {
                viewModel.participated(event.id, event.participatedByMe)
            }

            override fun onPreviewMap(event: Event) {
                if (event.coordinates != null && event.coordinates.lat != null && event.coordinates.long != null) {
                    findNavController().navigate(R.id.action_eventFragment_to_mapsFragment,
                        Bundle().apply {
                            doubleArg1 = event.coordinates.lat.toDouble()
                            doubleArg2 = event.coordinates.long.toDouble()
                        })
                }
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
        viewModel.dataEvents.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.events)
            binding.emptyText.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshEvents()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_eventFragment_to_newEventFragment)
        }

        return binding.root
    }
}
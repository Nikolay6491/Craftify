package ru.netology.craftify.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.craftify.BuildConfig
import ru.netology.craftify.R
import ru.netology.craftify.activity.PostService
import ru.netology.craftify.databinding.CardAdBinding
import ru.netology.craftify.databinding.CardPostBinding
import ru.netology.craftify.dto.Ad
import ru.netology.craftify.dto.FeedItem
import ru.netology.craftify.dto.Post
import ru.netology.craftify.type.AttachmentType

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun playVideo(post: Post) {}
    fun getPostById(id: Long)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("Unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> error("Unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("Unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("CheckResult")
    fun bind(ad: Ad) {
        binding.image.apply {
            Glide.with(itemView)
                .load("${BuildConfig.BASE_URL}/media/${ad.image}")
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likes.text = PostService.showValues(post.likes)
            like.isChecked = post.likedByMe
            shares.text = PostService.showValues(post.shares)
            videoContent.isVisible = !post.video.isNullOrBlank()
            playButton.isVisible = !post.video.isNullOrBlank()
            attachment.visibility = View.GONE
            menu.isVisible = post.ownedByMe

            val urlAuthor = "${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}"
            Glide.with(itemView)
                .load(urlAuthor)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)

            val urlAttachment = "${BuildConfig.BASE_URL}/media/${post.attachment?.url}"
            if (post.attachment != null) {
                Glide.with(attachment.context)
                    .load(urlAttachment)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(attachment)
                attachment.isVisible = true
            } else {
                attachment.isVisible = false
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
            playButton.setOnClickListener {
                onInteractionListener.playVideo(post)
            }
            videoContent.setOnClickListener {
                onInteractionListener.playVideo(post)
            }

            postImage.setOnClickListener {
                onInteractionListener.getPostById(post.id)
                it.findNavController()
                    .navigate(
                        R.id.action_feedFragment_to_imageFragment
                    )
            }

            if ((post.attachment != null) && (post.attachment.type == AttachmentType.IMAGE)) {
                Glide.with(binding.attachment)
                    .load(urlAttachment)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.attachment)
                attachment.isVisible = true
            } else attachment.isVisible = false
        }
    }

    private fun ImageView.load(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .circleCrop()
            .into(this)
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
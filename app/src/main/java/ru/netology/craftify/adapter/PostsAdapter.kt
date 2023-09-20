package ru.netology.craftify.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.craftify.R
import ru.netology.craftify.databinding.CardPostBinding
import ru.netology.craftify.dto.Post
import ru.netology.craftify.type.AttachmentType
import ru.netology.craftify.util.convertString2DateTime2String
import ru.netology.craftify.view.load
import ru.netology.craftify.view.loadCircleCrop

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onImage(post: Post) {}
    fun onMap(post: Post) {}
    fun onWall(userId: Long, userName: String, userPosition: String?, userAvatar: String?) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author

            if (post.authorJob.isNullOrBlank()) {
                authorJob.visibility = View.GONE
            } else {
                authorJob.text = post.authorJob
                authorJob.visibility = View.VISIBLE
            }
            published.text = convertString2DateTime2String(post.published)

            content.text = post.content

            if (post.link != null) content.text = "${content.text} \n${post.link}"

            if (post.authorAvatar != null)
                avatar.loadCircleCrop(post.authorAvatar)
            else avatar.setImageResource(R.mipmap.ic_launcher_craftify_round)

            buttonLike.isChecked = post.likedByMe

            buttonMap.isVisible = post.coordinates != null

            if (post.mentionList?.isEmpty() == true) {
                mentions.visibility = View.GONE
                mentionsInfo.visibility = View.GONE
            } else {
                mentions.visibility = View.VISIBLE
                mentionsInfo.visibility = View.VISIBLE
                mentions.text = post.mentionList?.joinToString(", ", "", "", 10, "...", null)
            }

            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.VISIBLE
                    AttachmentVideo.visibility = View.GONE
                    AttachmentImage.load(post.attachment.url)
                }
                AttachmentType.VIDEO -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.GONE
                    AttachmentVideo.apply {
                        visibility = View.VISIBLE
                        setMediaController(MediaController(binding.root.context))
                        setVideoURI(Uri.parse(post.attachment.url))
                        setOnPreparedListener {
                            animate().alpha(1F)
                            seekTo(0)
                            setZOrderOnTop(false)
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }

                }
                null -> {
                    AttachmentFrame.visibility = View.GONE
                }
            }

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit_content -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            buttonLike.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            buttonMap.setOnClickListener {
                onInteractionListener.onMap(post)
            }

            avatar.setOnClickListener {
                onInteractionListener.onWall(
                    post.authorId,
                    post.author,
                    post.authorJob,
                    post.authorAvatar
                )
            }
            author.setOnClickListener {
                onInteractionListener.onWall(
                    post.authorId,
                    post.author,
                    post.authorJob,
                    post.authorAvatar
                )
            }


        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
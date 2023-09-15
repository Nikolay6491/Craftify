package ru.netology.craftify.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.craftify.auth.AppAuth
import ru.netology.craftify.dto.FeedItem
import ru.netology.craftify.dto.MediaUpload
import ru.netology.craftify.dto.Post
import ru.netology.craftify.model.FeedModelState
import ru.netology.craftify.model.PhotoModel
import ru.netology.craftify.repository.PostRepository
import ru.netology.craftify.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject


private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0L,
    authorAvatar = "",
    published = "",
    likedByMe = false,
    sharesByMe = false,
    video = null,
    attachment = null,
    hidden = true,
    ownedByMe = false

)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    val edited = MutableLiveData(empty)

    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    if(post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
            }
        }


    private val noPhoto = PhotoModel()
    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val _currentPost = SingleLiveEvent<Post?>()
    val currentPost: LiveData<Post?>
        get() = _currentPost
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun getPostById(id: Long?) {
        viewModelScope.launch {
            try {
                val result = repository.getById(id)
                _currentPost.value = result
            } catch (e: Exception) {
                _dataState.value = FeedModelState.Error
            }
        }
    }

    fun load() = viewModelScope.launch {
        _dataState.value = FeedModelState.Loading
        try {
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun refresh() = viewModelScope.launch {
        _dataState.value = FeedModelState.Refresh
        try {
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun edit(post: Post) = viewModelScope.launch {
        edited.value = post
    }


    fun likesById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likes(id, likedByMe)
            _postCreated.postValue(Unit)
            _dataState.value = FeedModelState.Idle

        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refresh
            repository.removeById(id)
            _dataState.value = FeedModelState.Idle

        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }


    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                }
                _postCreated.postValue(Unit)
            }
            edited.postValue(empty)
            _photo.value = noPhoto
        }
    }

    fun loadVisiblePosts() = viewModelScope.launch {
        try {
            repository.showAll()
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}
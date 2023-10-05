package ru.netology.craftify.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.craftify.auth.AppAuth
import ru.netology.craftify.dto.*
import ru.netology.craftify.type.EventType
import ru.netology.craftify.model.*
import ru.netology.craftify.repository.PostRepository
import ru.netology.craftify.util.SingleLiveEvent
import ru.netology.craftify.util.convertDateTime2ISO_Instant
import java.io.File
import javax.inject.Inject


private val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false,
    coords = null
)

private val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    type = EventType.OFFLINE,
    likedByMe = false,
    participatedByMe = false,
    ownedByMe = false,
    coords = null
)

private val emptyJob = Job(
    userId = 0,
    id = 0,
    name = "",
    position = "",
    start = "",
    ownedByMe = false
)

private val noPhoto = PhotoModel()
private val noCoordinates = Coordinates()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {
    val data: LiveData<FeedModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.posts
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    val dataEvents: LiveData<EventModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.events
                .map { event ->
                    EventModel(
                        event.map { it.copy(ownedByMe = it.authorId == myId) },
                        event.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    val dataJobs: LiveData<JobModel> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.jobs
                .map { job ->
                    JobModel(
                        job.map {
                            val ownedByMe = if (it.userId == myId) true else false
                            println("===== " + it.userId + "  " + myId + " " + ownedByMe)
                            it.copy(ownedByMe = ownedByMe)
                        },
                        job.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val editedPost = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val editedEvent = MutableLiveData(emptyEvent)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val editedJob = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _coordinates = MutableLiveData(noCoordinates)
    val coordinates: LiveData<Coordinates>
        get() = _coordinates

    init {
        load()
        loadEvent()
    }

    fun load() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Loading
            repository.getPosts()
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refresh
            repository.getPosts()
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun savePosts() {
        editedPost.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            var postNew = post
                            if (post.attachment != null)
                                postNew = post.copy(attachment = null)
                            repository.save(postNew)
                        }
                        else ->
                            if (_photo.value?.file != null)
                                repository.saveWithAttachment(
                                    post,
                                    MediaRequest(_photo.value?.file!!)
                                )
                            else repository.save(post)
                    }
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                }
            }
        }
        editedPost.value = emptyPost
        _photo.value = noPhoto
        _coordinates.value = noCoordinates
    }

    fun edit(post: Post) {
        editedPost.value = post
    }

    fun changeContentPosts(content: String) {
        val text = content.trim()
        if (editedPost.value?.content == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(content = text)
    }

    fun changeLinkPosts(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedPost.value?.link == text) {
            return
        }
        editedPost.value = editedPost.value?.copy(link = text)
    }

    fun changeMentionList(mentionList: String) {

        try {
            val mentionIds = if (mentionList.isNotEmpty())
                mentionList.split(",").map {it.trim().toLong()}
            else emptyList()
            editedPost.value = editedPost.value?.copy(mentionIds = mentionIds)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun changeCoordsPosts(lat: String?, long: String?) {
        val coordinates = if (lat == null && long == null || lat == "" && long == "")
            null
        else
            Coordinates(lat, long)

        if (editedPost.value?.coords == coordinates) {
            return
        }
        editedPost.value = editedPost.value?.copy(coords = coordinates)
        editedEvent.value = editedEvent.value?.copy(coords = coordinates)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun changeCoordinatesFromMap(lat: String, long: String){
        _coordinates.value = if (lat.isBlank() && long.isBlank())
            null
        else
            Coordinates(lat,long)
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun likesById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun getEdit(): Post? {
        return editedPost.value
    }

    fun loadEvent() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Loading
            repository.getEvents()
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun refreshEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refresh
            repository.getEvents()
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun saveEvent() {
        editedEvent.value?.let { event ->
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            var eventNew = event
                            if (event.attachment != null)
                                eventNew = event.copy(attachment = null)
                            repository.saveEvent(eventNew)
                        }
                        else -> {
                            if (_photo.value?.file != null)
                                repository.saveEventWithAttachment(
                                    event,
                                    MediaRequest(_photo.value?.file!!)
                                )
                            else repository.saveEvent(event)
                        }
                    }
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                }
            }
        }
        editedEvent.value = emptyEvent
        _photo.value = noPhoto
        _coordinates.value = noCoordinates
    }

    fun editEvent(event: Event) {
        editedEvent.value = event
    }

    fun changeDateTimeEvent(date: String, time: String) {
        val datetime = convertDateTime2ISO_Instant(date, time)
        editedEvent.value = editedEvent.value?.copy(datetime = datetime)
    }

    fun changeContentEvent(content: String) {
        val text = content.trim()
        if (editedEvent.value?.content == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(content = text)
    }

    fun changeLinkEvent(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedEvent.value?.link == text) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(link = text)
    }

    fun changeCoordinatesEvent(lat: String?, long: String?) {
        val coordinates = if (lat == null && long == null || lat == "" && long == "")
            null
        else
            Coordinates(lat, long)

        if (editedEvent.value?.coords == coordinates) {
            return
        }
        editedEvent.value = editedEvent.value?.copy(coords = coordinates)
    }

    fun changeSpeakersEvent(speakersStr: String) {
        if (speakersStr.isNotEmpty())
            try {
                val speakers = speakersStr.split(",").map {
                    it.trim().toLong()
                }
                editedEvent.value = editedEvent.value?.copy(speakerIds = speakers)
            } catch (e: Exception) {
                _dataState.value = FeedModelState.Error
            }
    }

    fun changeTypeEvent(isOnline: Boolean) {
        if (isOnline)
            editedEvent.value = editedEvent.value?.copy(type = EventType.ONLINE)
        else
            editedEvent.value = editedEvent.value?.copy(type = EventType.OFFLINE)
    }

    fun removeEventById(id: Long) = viewModelScope.launch {
        try {
            repository.removeEventById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun likeEventById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeEventById(id, likedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun getEditEvent(): Event? {
        return editedEvent.value
    }

    fun participated(id: Long, participatedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.partEventById(id, participatedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun loadJobs(userId: Long, currentUserId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Loading
            repository.getJobs(userId, currentUserId)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun getCurrentUser(): Long {
        return auth.authStateFlow.value.id
    }

    fun getEditJob(): Job? {
        return editedJob.value
    }

    fun editJob(job: Job) {
        editedJob.value = job
    }

    fun saveJob(userId: Long) {
        editedJob.value?.let { job ->
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.saveJob(userId, job)
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                }
            }
        }
        editedJob.value = emptyJob
    }

    fun changeJobStart(start: String) {
        val dateStart = convertDateTime2ISO_Instant(start, "00:00")
        editedJob.value = editedJob.value?.copy(start = dateStart)
    }

    fun changeJobFinish(finish: String) {
        val finishStr =
            if (finish.isNotEmpty()) convertDateTime2ISO_Instant(finish, "00:00") else null
        editedJob.value = editedJob.value?.copy(finish = finishStr)
    }

    fun changeNameJob(name: String) {
        val text = name.trim()
        if (editedJob.value?.name == text) {
            return
        }
        editedJob.value = editedJob.value?.copy(name = text)
    }

    fun changePositionJob(position: String) {
        val text = position.trim()
        if (editedJob.value?.name == text) {
            return
        }
        editedJob.value = editedJob.value?.copy(position = text)
    }

    fun changeLinkJob(link: String) {
        val text = if (link.isEmpty())
            null
        else
            link.trim()

        if (editedJob.value?.link == text) {
            return
        }
        editedJob.value = editedJob.value?.copy(link = text)
    }

    fun removeJobById(id: Long) = viewModelScope.launch {
        try {
            repository.removeJobById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun refreshJobs(userId: Long, currentUserId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refresh
            repository.getJobs(userId, currentUserId)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }
}
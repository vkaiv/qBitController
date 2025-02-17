package dev.bartuzen.qbitcontroller.ui.rss.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bartuzen.qbitcontroller.data.repositories.rss.RssArticlesRepository
import dev.bartuzen.qbitcontroller.model.RssFeedWithData
import dev.bartuzen.qbitcontroller.model.deserializers.parseRssFeedWithData
import dev.bartuzen.qbitcontroller.network.RequestResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RssArticlesViewModel @Inject constructor(
    private val repository: RssArticlesRepository
) : ViewModel() {
    private val _rssFeed = MutableStateFlow<RssFeedWithData?>(null)
    val rssFeed = _rssFeed.asStateFlow()

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var isInitialLoadStarted = false

    private fun updateRssFeed(serverId: Int, feedPath: List<String>) = viewModelScope.launch {
        when (val result = repository.getRssFeeds(serverId)) {
            is RequestResult.Success -> {
                val feed = parseRssFeedWithData(result.data, feedPath)
                if (feed != null) {
                    _rssFeed.value = feed
                } else {
                    eventChannel.send(Event.RssFeedNotFound)
                }
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result))
            }
        }
    }

    fun loadRssFeed(serverId: Int, feedPath: List<String>) {
        if (!isLoading.value) {
            _isLoading.value = true
            updateRssFeed(serverId, feedPath).invokeOnCompletion {
                _isLoading.value = false
            }
        }
    }

    fun refreshRssFeed(serverId: Int, feedPath: List<String>) {
        if (!isRefreshing.value) {
            _isRefreshing.value = true
            updateRssFeed(serverId, feedPath).invokeOnCompletion {
                _isRefreshing.value = false
            }
        }
    }

    fun markAsRead(serverId: Int, feedPath: List<String>, articleId: String?) = viewModelScope.launch {
        when (val result = repository.markAsRead(serverId, feedPath, articleId)) {
            is RequestResult.Success -> {
                if (articleId == null) {
                    eventChannel.send(Event.AllArticlesMarkedAsRead)
                } else {
                    eventChannel.send(Event.ArticleMarkedAsRead)
                }
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result))
            }
        }
    }

    fun refreshFeed(serverId: Int, feedPath: List<String>) = viewModelScope.launch {
        when (val result = repository.refreshItem(serverId, feedPath)) {
            is RequestResult.Success -> {
                eventChannel.send(Event.FeedRefreshed)
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result))
            }
        }
    }

    sealed class Event {
        data class Error(val error: RequestResult.Error) : Event()
        object RssFeedNotFound : Event()
        object ArticleMarkedAsRead : Event()
        object AllArticlesMarkedAsRead : Event()
        object FeedRefreshed : Event()
    }
}

package dev.bartuzen.qbitcontroller.ui.torrent.tabs.trackers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bartuzen.qbitcontroller.data.repositories.TorrentRepository
import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.model.TorrentTracker
import dev.bartuzen.qbitcontroller.network.RequestError
import dev.bartuzen.qbitcontroller.network.RequestResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TorrentTrackersViewModel @Inject constructor(
    private val repository: TorrentRepository
) : ViewModel() {
    val torrentTrackers = MutableStateFlow<List<TorrentTracker>?>(null)

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    val isLoading = MutableStateFlow(true)

    fun updateTrackers(serverConfig: ServerConfig, hash: String) = viewModelScope.launch {
        when (val result = repository.getTrackers(serverConfig, hash)) {
            is RequestResult.Success -> {
                torrentTrackers.value = result.data
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result.error))
            }
        }
    }

    fun addTrackers(serverConfig: ServerConfig, hash: String, urls: String) =
        viewModelScope.launch {
            when (val result = repository.addTrackers(serverConfig, hash, urls)) {
                is RequestResult.Success -> {
                    eventChannel.send(Event.TrackersAdded)
                }
                is RequestResult.Error -> {
                    eventChannel.send(Event.Error(result.error))
                }
            }
        }

    sealed class Event {
        data class Error(val error: RequestError) : Event()
        object TrackersAdded : Event()
    }
}
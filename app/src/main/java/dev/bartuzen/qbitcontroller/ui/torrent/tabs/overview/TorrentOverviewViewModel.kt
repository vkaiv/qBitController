package dev.bartuzen.qbitcontroller.ui.torrent.tabs.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bartuzen.qbitcontroller.data.repositories.TorrentRepository
import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.model.Torrent
import dev.bartuzen.qbitcontroller.network.RequestError
import dev.bartuzen.qbitcontroller.network.RequestResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TorrentOverviewViewModel @Inject constructor(
    private val repository: TorrentRepository
) : ViewModel() {
    val torrent = MutableStateFlow<Torrent?>(null)

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    val isLoading = MutableStateFlow(true)

    fun updateTorrent(serverConfig: ServerConfig, torrentHash: String) = viewModelScope.launch {
        when (val result = repository.getTorrent(serverConfig, torrentHash)) {
            is RequestResult.Success -> {
                if (result.data.size == 1) {
                    torrent.value = result.data.first()
                }
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result.error))
            }
        }
    }

    fun pauseTorrent(serverConfig: ServerConfig, torrentHash: String) = viewModelScope.launch {
        when (val result = repository.pauseTorrent(serverConfig, torrentHash)) {
            is RequestResult.Success -> {
                eventChannel.send(Event.OnTorrentPause)
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result.error))
            }
        }
    }

    fun resumeTorrent(serverConfig: ServerConfig, torrentHash: String) = viewModelScope.launch {
        when (val result = repository.resumeTorrent(serverConfig, torrentHash)) {
            is RequestResult.Success -> {
                eventChannel.send(Event.OnTorrentResume)
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result.error))
            }
        }
    }

    sealed class Event {
        data class Error(val error: RequestError) : Event()
        object OnTorrentPause : Event()
        object OnTorrentResume : Event()
    }
}
package dev.bartuzen.qbitcontroller.ui.settings.addeditserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.JsonMappingException
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bartuzen.qbitcontroller.data.ServerManager
import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.network.RequestResult
import dev.bartuzen.qbitcontroller.network.TimeoutInterceptor
import dev.bartuzen.qbitcontroller.network.TorrentService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    private val serverManager: ServerManager,
    private val timeoutInterceptor: TimeoutInterceptor
) : ViewModel() {
    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting = _isTesting.asStateFlow()

    private var testJob: Job? = null

    fun addServer(serverConfig: ServerConfig) = viewModelScope.launch {
        serverManager.addServer(serverConfig)
    }

    fun editServer(serverConfig: ServerConfig) = viewModelScope.launch {
        serverManager.editServer(serverConfig)
    }

    fun removeServer(serverConfig: ServerConfig) = viewModelScope.launch {
        serverManager.removeServer(serverConfig)
    }

    fun testConnection(serverConfig: ServerConfig) {
        testJob?.cancel()

        _isTesting.value = true
        val job = viewModelScope.launch {
            val service = Retrofit.Builder()
                .baseUrl(serverConfig.url)
                .client(
                    OkHttpClient().newBuilder()
                        .addInterceptor(timeoutInterceptor)
                        .build()
                )
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create<TorrentService>()

            val error = try {
                val response = service.login(serverConfig.username, serverConfig.password)

                if (response.code() == 403) {
                    RequestResult.Error.RequestError.Banned
                } else if (response.body() == "Fails.") {
                    RequestResult.Error.RequestError.InvalidCredentials
                } else if (response.body() != "Ok.") {
                    RequestResult.Error.RequestError.Unknown
                } else {
                    null
                }
            } catch (e: ConnectException) {
                RequestResult.Error.RequestError.CannotConnect
            } catch (e: SocketTimeoutException) {
                RequestResult.Error.RequestError.Timeout
            } catch (e: UnknownHostException) {
                RequestResult.Error.RequestError.UnknownHost
            } catch (e: JsonMappingException) {
                if (e.cause is SocketTimeoutException) {
                    RequestResult.Error.RequestError.Timeout
                } else {
                    throw e
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                RequestResult.Error.RequestError.Unknown
            }

            eventChannel.send(
                if (error == null) {
                    Event.TestSuccess
                } else {
                    Event.TestFailure(error)
                }
            )
        }

        job.invokeOnCompletion { e ->
            if (e !is CancellationException) {
                _isTesting.value = false
                testJob = null
            }
        }

        testJob = job
    }

    sealed class Event {
        data class TestFailure(val error: RequestResult.Error) : Event()
        object TestSuccess : Event()
    }
}

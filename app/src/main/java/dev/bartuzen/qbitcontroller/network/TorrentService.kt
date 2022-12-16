package dev.bartuzen.qbitcontroller.network

import dev.bartuzen.qbitcontroller.model.Category
import dev.bartuzen.qbitcontroller.model.PieceState
import dev.bartuzen.qbitcontroller.model.Torrent
import dev.bartuzen.qbitcontroller.model.TorrentFile
import dev.bartuzen.qbitcontroller.model.TorrentProperties
import dev.bartuzen.qbitcontroller.model.TorrentTracker
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface TorrentService {
    @FormUrlEncoded
    @POST("api/v2/auth/login")
    suspend fun login(@Field("username") username: String, @Field("password") password: String): Response<String>

    @GET("api/v2/torrents/info")
    suspend fun getTorrentList(@Query("hashes") hashes: String? = null): Response<List<Torrent>>

    @GET("api/v2/torrents/files")
    suspend fun getFiles(@Query("hash") hash: String): Response<List<TorrentFile>>

    @FormUrlEncoded
    @POST("api/v2/torrents/delete")
    suspend fun deleteTorrents(@Field("hashes") hashes: String, @Field("deleteFiles") deleteFiles: Boolean): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/pause")
    suspend fun pauseTorrents(@Field("hashes") hashes: String): Response<String>

    @FormUrlEncoded
    @POST("api/v2/torrents/resume")
    suspend fun resumeTorrents(@Field("hashes") hashes: String): Response<String>

    @FormUrlEncoded
    @POST("api/v2/torrents/recheck")
    suspend fun recheckTorrents(@Field("hashes") hashes: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/reannounce")
    suspend fun reannounceTorrents(@Field("hashes") hashes: String): Response<Unit>

    @GET("api/v2/torrents/pieceStates")
    suspend fun getTorrentPieces(@Query("hash") hash: String): Response<List<PieceState>>

    @GET("api/v2/torrents/properties")
    suspend fun getTorrentProperties(@Query("hash") hash: String): Response<TorrentProperties>

    @GET("api/v2/torrents/trackers")
    suspend fun getTorrentTrackers(@Query("hash") hash: String): Response<List<TorrentTracker>>

    @FormUrlEncoded
    @POST("api/v2/torrents/addTrackers")
    suspend fun addTorrentTrackers(@Field("hash") hash: String, @Field("urls") urls: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/removeTrackers")
    suspend fun deleteTorrentTrackers(@Field("hash") hash: String, @Field("urls") urls: String): Response<Unit>

    @GET("api/v2/torrents/categories")
    suspend fun getCategories(): Response<Map<String, Category>>

    @GET("api/v2/torrents/tags")
    suspend fun getTags(): Response<List<String>>

    @FormUrlEncoded
    @POST("api/v2/torrents/removeCategories")
    suspend fun deleteCategories(@Field("categories") categories: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/deleteTags")
    suspend fun deleteTags(@Field("tags") tags: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/increasePrio")
    suspend fun increaseTorrentPriority(@Field("hashes") hashes: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/decreasePrio")
    suspend fun decreaseTorrentPriority(@Field("hashes") hashes: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/topPrio")
    suspend fun maximizeTorrentPriority(@Field("hashes") hashes: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/bottomPrio")
    suspend fun minimizeTorrentPriority(@Field("hashes") hashes: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/createCategory")
    suspend fun createCategory(@Field("category") name: String, @Field("savePath") savePath: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/createTags")
    suspend fun createTags(@Field("tags") names: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/toggleSequentialDownload")
    suspend fun toggleSequentialDownload(@Field("hashes") hashes: String): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/toggleFirstLastPiecePrio")
    suspend fun togglePrioritizeFirstLastPiecesDownload(@Field("hashes") hashes: String): Response<Unit>

    @Multipart
    @POST("api/v2/torrents/add")
    suspend fun addTorrent(
        @Part("urls") links: String?,
        @Part filePart: MultipartBody.Part?,
        @Part("savepath") savePath: String?,
        @Part("category") category: String?,
        @Part("tags") tags: String?,
        @Part("rename") torrentName: String?,
        @Part("dlLimit") downloadSpeedLimit: Int?,
        @Part("upLimit") uploadSpeedLimit: Int?,
        @Part("ratioLimit") ratioLimit: Double?,
        @Part("seedingTimeLimit") seedingTimeLimit: Int?,
        @Part("paused") isPaused: Boolean,
        @Part("skip_checking") skipHashChecking: Boolean,
        @Part("autoTMM") isAutoTorrentManagementEnabled: Boolean,
        @Part("sequentialDownload") isSequentialDownloadEnabled: Boolean,
        @Part("firstLastPiecePrio") isFirstLastPiecePrioritized: Boolean
    ): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/setAutoManagement")
    suspend fun setAutomaticTorrentManagement(
        @Field("hashes") hashes: String,
        @Field("enable") enable: Boolean
    ): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/setDownloadLimit")
    suspend fun setDownloadSpeedLimit(@Field("hashes") hashes: String, @Field("limit") limit: Int): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/setUploadLimit")
    suspend fun setUploadSpeedLimit(@Field("hashes") hashes: String, @Field("limit") limit: Int): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/setForceStart")
    suspend fun setForceStart(@Field("hashes") hashes: String, @Field("value") value: Boolean): Response<Unit>

    @FormUrlEncoded
    @POST("api/v2/torrents/setSuperSeeding")
    suspend fun setSuperSeeding(@Field("hashes") hashes: String, @Field("value") value: Boolean): Response<Unit>
}

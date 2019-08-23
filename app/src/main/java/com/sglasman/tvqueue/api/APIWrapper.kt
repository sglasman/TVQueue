package com.sglasman.tvqueue.api

import android.util.Log
import androidx.core.util.LogWriter
import com.sglasman.tvqueue.TVQResponse
import com.sglasman.tvqueue.getTVQResponse
import com.sglasman.tvqueue.models.TVDBCredentials
import com.sglasman.tvqueue.models.search.SearchResult
import com.sglasman.tvqueue.models.series.Episode
import com.sglasman.tvqueue.models.series.Season
import com.sglasman.tvqueue.models.series.Series
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class APIWrapper(private val service: APIService) {

    suspend fun login(creds: TVDBCredentials): TVQResponse<String> = withContext(Dispatchers.IO) {
        service.login(creds).getTVQResponse().map { it.token }
    }

    suspend fun search(text: String): TVQResponse<List<SearchResult>> = withContext(Dispatchers.IO)
    {
        service.search(text).getTVQResponse().map {
            it.data.mapNotNull { it.toSearchResult() }
        }
    }

    suspend fun getSeries(id: Int, name: String): TVQResponse<Series> = withContext(Dispatchers.IO)
    {
        service.getEpisodes(id).getTVQResponse().flatMap { response -> try {
        TVQResponse.Success(Series(
            id = id,
            name = name,
            seasons = response.data.map { it.airedSeason }.distinct().map { seasonNumber ->
                val seasonEpisodes = response.data.filter { it.airedSeason == seasonNumber}
                val TVQEpisodes = seasonEpisodes.map { episode ->
                    val parsedDate = episode.firstAired!!.parseTVDBDate()!!
                    Episode(seriesTitle = name,
                        title = episode.episodeName!!,
                        numberInSeason = episode.airedEpisodeNumber!!,
                        airDate = parsedDate,
                        dateToWatch = parsedDate)
                }
                Season(number = seasonNumber!!,
                    episodes = TVQEpisodes,
                    dump = TVQEpisodes.map { it.airDate }.distinct().size == 1)
            }))
        } catch (e: Exception) {
            Log.e("getSeries","Trouble interpreting response", e)
            TVQResponse.Error<Series>(-1, "")
        }
    }}

    private fun String.parseTVDBDate(): Date? = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .parse(this.takeWhile { it != ' ' })
}
package com.sglasman.tvqueue.api

import android.util.Log
import com.sglasman.tvqueue.*
import com.sglasman.tvqueue.models.TVDBCredentials
import com.sglasman.tvqueue.models.addDays
import com.sglasman.tvqueue.models.search.SearchResult
import com.sglasman.tvqueue.models.series.Episode
import com.sglasman.tvqueue.models.series.EpisodeResponse
import com.sglasman.tvqueue.models.series.Season
import com.sglasman.tvqueue.models.series.Series
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class APIWrapper(private val service: APIService) {

    suspend fun login(creds: TVDBCredentials): TVQResponse<String> = withContext(ioContext) {
        service.login(creds).getTVQResponse().map { it.token }
    }

    suspend fun search(text: String): TVQResponse<List<SearchResult>> = withContext(ioContext) {
        service.search(text).getTVQResponse().map {
            it.data.mapNotNull { it.toSearchResult() }
        }
    }

    suspend fun getSeries(id: Int, name: String): TVQResponse<Series> = withContext(ioContext) {
        service.getEpisodes(id).getTVQResponse().suspendFlatMap { response ->
            try {
                val episodes: List<EpisodeResponse> = if (response.links.last == 1) response.data
                else response.data + (2..response.links.last).flatMap {
                        service.getEpisodes(id, page = it).getTVQResponse().let {
                            if (it is TVQResponse.Success) it.value.data else listOf()
                        }
                }
                val foundSeasons = episodes.map { it.airedSeason }.distinct()
                    .apply { if (isEmpty()) throw Exception("No seasons of show") }
                    .map { seasonNumber ->
                        val seasonEpisodes =
                            episodes.filter { it.airedSeason == seasonNumber }
                        val TVQEpisodes = seasonEpisodes.map { episode ->
                            val parsedDate = episode.firstAired!!.parseTVDBDate()
                            Episode(
                                seriesTitle = name,
                                title = episode.episodeName ?: "",
                                seasonNumber = seasonNumber,
                                airDate = parsedDate,
                                numberInSeason = episode.airedEpisodeNumber!!,
                                dateToWatch = parsedDate,
                                internalID = UUID.randomUUID().toString()
                            )
                        }
                        val dump = TVQEpisodes.map { it.airDate }.distinct().run {
                            size == 1 && this != listOf<Date?>(null)
                        } && TVQEpisodes.size > 3
                        val startDate =
                            TVQEpisodes.mapNotNull { it.airDate }.min() ?: getCurrentDate()
                        val updatedEpisodes = if (!dump) TVQEpisodes
                        else TVQEpisodes.map { episode ->
                            episode.copy(
                                dateToWatch =
                                startDate.addDays(7 * (episode.numberInSeason - 1))
                            )
                        }
                        Season(
                            number = seasonNumber!!,
                            episodes = updatedEpisodes,
                            dump = dump,
                            useOriginalAirdates =
                            if (TVQEpisodes.mapNotNull { it.airDate }.isEmpty()) null
                            else !dump,
                            startDate = startDate,
                            intervalDays = 7,
                            startingEpisode = null
                        )
                    }
                TVQResponse.Success(Series(
                    id = id,
                    name = name,
                    seasons = foundSeasons,
                    latestSeason = foundSeasons.map { it.number }.max()
                )
                )
            } catch (e: Exception) {
                Log.e("getSeries", "Trouble interpreting response", e)
                TVQResponse.Error<Series>(-1, "")
            }
        }
    }

    private fun String.parseTVDBDate(): Date? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .parse(this.takeWhile { it != ' ' })
    } catch (e: Exception) {
        null
    }
}
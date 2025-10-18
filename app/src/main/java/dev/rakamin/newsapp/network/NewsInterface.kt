package dev.rakamin.newsapp.network

import dev.rakamin.newsapp.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = "39684e7ec6bb4b1faeab3d2dd2806bef"
const val BASE_URL = "https://newsapi.org/v2/"

interface NewsInterface {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = API_KEY,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<NewsResponse>

    @GET("everything")
    suspend fun getAllNews(
        @Query("q") query: String = "news",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("apiKey") apiKey: String = API_KEY,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<NewsResponse>
}
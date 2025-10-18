package dev.rakamin.newsapp.repository

import androidx.lifecycle.LiveData
import dev.rakamin.newsapp.database.ArticleDao
import dev.rakamin.newsapp.database.ArticleEntity
import dev.rakamin.newsapp.model.Article

class ArticleRepository(private val articleDao: ArticleDao) {

    val allFavorites: LiveData<List<ArticleEntity>> = articleDao.getAllFavorites()

    suspend fun insertFavorite(article: Article) {
        val entity = ArticleEntity(
            url = article.url,
            sourceId = article.source.id,
            sourceName = article.source.name,
            author = article.author,
            title = article.title,
            description = article.description,
            urlToImage = article.urlToImage,
            publishedAt = article.publishedAt,
            content = article.content
        )
        articleDao.insertArticle(entity)
    }

    suspend fun deleteFavorite(url: String) {
        articleDao.deleteByUrl(url)
    }

    suspend fun isFavorite(url: String): Boolean {
        return articleDao.getFavoriteByUrl(url) != null
    }
}
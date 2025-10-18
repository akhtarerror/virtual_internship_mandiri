package dev.rakamin.newsapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dev.rakamin.newsapp.database.ArticleDatabase
import dev.rakamin.newsapp.database.ArticleEntity
import dev.rakamin.newsapp.model.Article
import dev.rakamin.newsapp.repository.ArticleRepository
import kotlinx.coroutines.launch

class ArticleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArticleRepository
    val allFavorites: LiveData<List<ArticleEntity>>

    init {
        val articleDao = ArticleDatabase.getDatabase(application).articleDao()
        repository = ArticleRepository(articleDao)
        allFavorites = repository.allFavorites
    }

    fun addToFavorites(article: Article) = viewModelScope.launch {
        repository.insertFavorite(article)
    }

    fun removeFromFavorites(url: String) = viewModelScope.launch {
        repository.deleteFavorite(url)
    }

    suspend fun isFavorite(url: String): Boolean {
        return repository.isFavorite(url)
    }
}
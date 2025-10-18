package dev.rakamin.newsapp.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("SELECT * FROM favorite_articles ORDER BY savedAt DESC")
    fun getAllFavorites(): LiveData<List<ArticleEntity>>

    @Query("SELECT * FROM favorite_articles WHERE url = :url")
    suspend fun getFavoriteByUrl(url: String): ArticleEntity?

    @Query("DELETE FROM favorite_articles WHERE url = :url")
    suspend fun deleteByUrl(url: String)
}
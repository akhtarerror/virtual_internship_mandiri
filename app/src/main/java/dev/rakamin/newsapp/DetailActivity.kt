package dev.rakamin.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.rakamin.newsapp.model.Article
import dev.rakamin.newsapp.model.Source
import dev.rakamin.newsapp.viewmodel.ArticleViewModel
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var sourceTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var readMoreButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var fabFavorite: FloatingActionButton

    private lateinit var viewModel: ArticleViewModel
    private var currentArticle: Article? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[ArticleViewModel::class.java]

        initViews()
        displayArticle()
        checkFavoriteStatus()
        setupFavoriteButton()
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageDetail)
        titleTextView = findViewById(R.id.textDetailTitle)
        authorTextView = findViewById(R.id.textAuthor)
        dateTextView = findViewById(R.id.textDate)
        sourceTextView = findViewById(R.id.textDetailSource)
        descriptionTextView = findViewById(R.id.textDescription)
        contentTextView = findViewById(R.id.textContent)
        readMoreButton = findViewById(R.id.btnReadMore)
        backButton = findViewById(R.id.btnBack)
        fabFavorite = findViewById(R.id.fabFavorite)

        backButton.setOnClickListener {
            onBackPressed()
        }

        readMoreButton.setOnClickListener {
            currentArticle?.url?.let { url ->
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
        }
    }

    private fun setupFavoriteButton() {
        fabFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun checkFavoriteStatus() {
        currentArticle?.let { article ->
            lifecycleScope.launch {
                isFavorite = viewModel.isFavorite(article.url)
                updateFavoriteIcon()
            }
        }
    }

    private fun toggleFavorite() {
        currentArticle?.let { article ->
            isFavorite = !isFavorite
            updateFavoriteIcon()

            if (isFavorite) {
                viewModel.addToFavorites(article)
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.removeFromFavorites(article.url)
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun displayArticle() {
        val title = intent.getStringExtra("title")
        val author = intent.getStringExtra("author")
        val source = intent.getStringExtra("source")
        val sourceId = intent.getStringExtra("sourceId")
        val imageUrl = intent.getStringExtra("imageUrl")
        val content = intent.getStringExtra("content")
        val description = intent.getStringExtra("description")
        val publishedAt = intent.getStringExtra("publishedAt")
        val articleUrl = intent.getStringExtra("url")

        // Create Article object
        if (articleUrl != null) {
            currentArticle = Article(
                source = Source(sourceId, source ?: "Unknown"),
                author = author,
                title = title ?: "No Title",
                description = description,
                url = articleUrl,
                urlToImage = imageUrl,
                publishedAt = publishedAt ?: "",
                content = content
            )
        }

        titleTextView.text = title ?: "No Title"
        authorTextView.text = "By ${author ?: "Unknown"}"
        sourceTextView.text = source ?: "Unknown Source"
        dateTextView.text = formatDate(publishedAt)

        if (!description.isNullOrEmpty() && description != "null") {
            descriptionTextView.text = description
        } else {
            descriptionTextView.text = "No description available"
        }

        if (!content.isNullOrEmpty() && content != "null") {
            val cleanContent = content.replace(Regex("\\[\\+\\d+ chars\\]"), "")
            contentTextView.text = cleanContent
        } else {
            contentTextView.text = "Content not available. Click 'Read Full Article' to view on original website."
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(imageView)
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Unknown Date"

        return try {
            val parts = dateString.split("T")
            if (parts.isNotEmpty()) {
                parts[0]
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
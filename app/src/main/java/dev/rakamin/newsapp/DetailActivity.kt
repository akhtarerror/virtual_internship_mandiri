package dev.rakamin.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

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

    private var articleUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Sembunyikan action bar karena kita pakai custom back button
        supportActionBar?.hide()

        initViews()
        displayArticle()
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

        // Back button listener
        backButton.setOnClickListener {
            onBackPressed()
        }

        readMoreButton.setOnClickListener {
            articleUrl?.let { url ->
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
        }
    }

    private fun displayArticle() {
        val title = intent.getStringExtra("title")
        val author = intent.getStringExtra("author")
        val source = intent.getStringExtra("source")
        val imageUrl = intent.getStringExtra("imageUrl")
        val content = intent.getStringExtra("content")
        val description = intent.getStringExtra("description")
        val publishedAt = intent.getStringExtra("publishedAt")
        articleUrl = intent.getStringExtra("url")

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
package dev.rakamin.newsapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.rakamin.newsapp.adapter.MainAdapter
import dev.rakamin.newsapp.model.Article
import dev.rakamin.newsapp.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var mainAdapter: MainAdapter

    // ==================== PAGINATION VARIABLES ====================
    // Variables untuk pagination Headlines
    private var currentHeadlinesPage = 1
    private var isLoadingHeadlines = false
    private var hasMoreHeadlines = true

    // Variables untuk pagination News
    private var currentNewsPage = 1
    private var isLoadingNews = false
    private var hasMoreNews = true
    // ==============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainAdapter = MainAdapter { article ->
            openDetailActivity(article)
        }

        initViews()
        setupRecyclerView()

        // Load initial data
        loadHeadlines(currentHeadlinesPage)
        loadNews(currentNewsPage)
    }

    private fun initViews() {
        mainRecyclerView = findViewById(R.id.recyclerMain)
        progressBar = findViewById(R.id.progressBar)
    }

    // ==================== SETUP RECYCLERVIEW WITH SCROLL LISTENER ====================
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        mainRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = mainAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Trigger pagination saat mendekati akhir list
                    if (!isLoadingNews && hasMoreNews && dy > 0) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                            && firstVisibleItemPosition >= 0) {
                            currentNewsPage++
                            loadNews(currentNewsPage)
                        }
                    }
                }
            })
        }

        // Set listener untuk horizontal scroll headlines
        mainAdapter.setOnHeadlinesScrollListener { isNearEnd ->
            if (isNearEnd && !isLoadingHeadlines && hasMoreHeadlines) {
                currentHeadlinesPage++
                loadHeadlines(currentHeadlinesPage)
            }
        }
    }
    // ================================================================================

    // ==================== LOAD HEADLINES WITH PAGINATION ====================
    private fun loadHeadlines(page: Int) {
        if (isLoadingHeadlines) return

        isLoadingHeadlines = true

        // Show progress bar hanya di page pertama
        if (page == 1) {
            progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                // Ambil semua headlines tanpa limit (pageSize = 100 adalah max dari NewsAPI)
                val response = RetrofitClient.newsApi.getTopHeadlines(
                    page = page,
                    pageSize = 100
                )

                if (response.isSuccessful && response.body() != null) {
                    val headlines = response.body()!!.articles

                    if (headlines.isEmpty()) {
                        hasMoreHeadlines = false
                        if (page > 1) {
                            showToast("No more headlines available")
                        }
                    } else {
                        if (page == 1) {
                            // Submit list baru untuk page pertama
                            mainAdapter.submitHeadlines(headlines)
                        } else {
                            // Tambahkan ke list yang sudah ada untuk page selanjutnya
                            mainAdapter.addHeadlines(headlines)
                        }
                    }
                } else {
                    showToast("Failed to load headlines: ${response.code()}")
                    hasMoreHeadlines = false
                }
            } catch (e: Exception) {
                showToast("Error loading headlines: ${e.message}")
                e.printStackTrace()
                hasMoreHeadlines = false
            } finally {
                isLoadingHeadlines = false
                if (page == 1) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }
    // ========================================================================

    // ==================== LOAD NEWS WITH PAGINATION ====================
    private fun loadNews(page: Int) {
        if (isLoadingNews) return

        isLoadingNews = true

        // Show progress bar hanya di page pertama
        if (page == 1) {
            progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.newsApi.getAllNews(
                    page = page,
                    pageSize = 20
                )

                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body()!!.articles

                    if (articles.isEmpty()) {
                        hasMoreNews = false
                        showToast("No more news available")
                    } else {
                        if (page == 1) {
                            // Submit list baru untuk page pertama
                            mainAdapter.submitNews(articles)
                        } else {
                            // Tambahkan ke list yang sudah ada untuk page selanjutnya
                            mainAdapter.addNews(articles)
                        }
                    }
                } else {
                    showToast("Failed to load news: ${response.code()}")
                    hasMoreNews = false
                }
            } catch (e: Exception) {
                showToast("Error loading news: ${e.message}")
                e.printStackTrace()
                hasMoreNews = false
            } finally {
                isLoadingNews = false
                progressBar.visibility = View.GONE
            }
        }
    }
    // ===================================================================

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ==================== OPEN DETAIL ACTIVITY ====================
    private fun openDetailActivity(article: Article) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("title", article.title)
            putExtra("author", article.author)
            putExtra("source", article.source.name)
            putExtra("imageUrl", article.urlToImage)
            putExtra("content", article.content)
            putExtra("description", article.description)
            putExtra("publishedAt", article.publishedAt)
            putExtra("url", article.url)
        }
        startActivity(intent)
    }
    // ==============================================================
}
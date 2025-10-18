package dev.rakamin.newsapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.rakamin.newsapp.DetailActivity
import dev.rakamin.newsapp.R
import dev.rakamin.newsapp.adapter.MainAdapter
import dev.rakamin.newsapp.model.Article
import dev.rakamin.newsapp.network.RetrofitClient
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var mainAdapter: MainAdapter

    // ==================== PAGINATION VARIABLES ====================
    private var currentHeadlinesPage = 1
    private var isLoadingHeadlines = false
    private var hasMoreHeadlines = true

    private var currentNewsPage = 1
    private var isLoadingNews = false
    private var hasMoreNews = true
    // ==============================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainAdapter = MainAdapter { article ->
            openDetailActivity(article)
        }

        initViews(view)
        setupRecyclerView()

        // Load initial data
        loadHeadlines(currentHeadlinesPage)
        loadNews(currentNewsPage)
    }

    private fun initViews(view: View) {
        mainRecyclerView = view.findViewById(R.id.recyclerMain)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun navigateToDetail(article: Article) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("title", article.title)
            putExtra("author", article.author)
            putExtra("source", article.source.name)
            putExtra("sourceId", article.source.id)  // TAMBAHKAN INI
            putExtra("imageUrl", article.urlToImage)
            putExtra("content", article.content)
            putExtra("description", article.description)
            putExtra("publishedAt", article.publishedAt)
            putExtra("url", article.url)
        }
        startActivity(intent)
    }

    // ==================== SETUP RECYCLERVIEW WITH SCROLL LISTENER ====================
    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
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

        if (page == 1) {
            progressBar.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
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
                            mainAdapter.submitHeadlines(headlines)
                        } else {
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

        if (page == 1) {
            progressBar.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
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
                            mainAdapter.submitNews(articles)
                        } else {
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // ==================== OPEN DETAIL ACTIVITY ====================
    private fun openDetailActivity(article: Article) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
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

package dev.rakamin.newsapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import dev.rakamin.newsapp.DetailActivity
import dev.rakamin.newsapp.R
import dev.rakamin.newsapp.adapter.MainAdapter
import dev.rakamin.newsapp.model.Article
import dev.rakamin.newsapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException

class HomeFragment : Fragment() {

    private lateinit var mainRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var mainAdapter: MainAdapter
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyStateImage: ImageView
    private lateinit var emptyStateTitle: TextView
    private lateinit var emptyStateMessage: TextView
    private lateinit var btnRetry: MaterialButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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
        setupSwipeRefresh()
        setupRecyclerView()
        setupRetryButton()

        // Load initial data
        loadInitialData()
    }

    private fun initViews(view: View) {
        mainRecyclerView = view.findViewById(R.id.recyclerMain)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        emptyStateImage = view.findViewById(R.id.emptyStateImage)
        emptyStateTitle = view.findViewById(R.id.emptyStateTitle)
        emptyStateMessage = view.findViewById(R.id.emptyStateMessage)
        btnRetry = view.findViewById(R.id.btnRetry)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
    }

    // ==================== SETUP SWIPE TO REFRESH ====================
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500,
            R.color.purple_700,
            R.color.teal_200
        )

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        // Reset pagination
        currentHeadlinesPage = 1
        currentNewsPage = 1
        hasMoreHeadlines = true
        hasMoreNews = true

        // Clear existing data
        mainAdapter.submitHeadlines(emptyList())
        mainAdapter.submitNews(emptyList())

        // Hide empty state if visible
        hideEmptyState()

        // Load fresh data
        loadHeadlines(currentHeadlinesPage)
        loadNews(currentNewsPage)
    }
    // ================================================================

    private fun setupRetryButton() {
        btnRetry.setOnClickListener {
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        hideEmptyState()
        currentHeadlinesPage = 1
        currentNewsPage = 1
        hasMoreHeadlines = true
        hasMoreNews = true
        loadHeadlines(currentHeadlinesPage)
        loadNews(currentNewsPage)
    }

    private fun showEmptyState(errorType: ErrorType) {
        mainRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false

        when (errorType) {
            ErrorType.NO_CONNECTION -> {
                emptyStateImage.setImageResource(R.drawable.ic_no_connection)
                emptyStateTitle.text = getString(R.string.no_connection_title)
                emptyStateMessage.text = getString(R.string.no_connection_message)
            }
            ErrorType.TIMEOUT -> {
                emptyStateImage.setImageResource(R.drawable.ic_timeout)
                emptyStateTitle.text = getString(R.string.timeout_title)
                emptyStateMessage.text = getString(R.string.timeout_message)
            }
            ErrorType.SERVER_ERROR -> {
                emptyStateImage.setImageResource(R.drawable.ic_server_error)
                emptyStateTitle.text = getString(R.string.server_error_title)
                emptyStateMessage.text = getString(R.string.server_error_message)
            }
            ErrorType.UNKNOWN -> {
                emptyStateImage.setImageResource(R.drawable.ic_error)
                emptyStateTitle.text = getString(R.string.error_title)
                emptyStateMessage.text = getString(R.string.error_message)
            }
        }
    }

    private fun hideEmptyState() {
        emptyStateLayout.visibility = View.GONE
        mainRecyclerView.visibility = View.VISIBLE
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

        if (page == 1 && !swipeRefreshLayout.isRefreshing) {
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
                        hideEmptyState()
                    }
                } else {
                    if (page == 1) {
                        showEmptyState(ErrorType.SERVER_ERROR)
                    } else {
                        showToast("Failed to load headlines: ${response.code()}")
                    }
                    hasMoreHeadlines = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (page == 1) {
                    when (e) {
                        is UnknownHostException -> showEmptyState(ErrorType.NO_CONNECTION)
                        is SocketTimeoutException -> showEmptyState(ErrorType.TIMEOUT)
                        else -> showEmptyState(ErrorType.UNKNOWN)
                    }
                } else {
                    showToast("Error loading headlines: ${e.message}")
                }
                hasMoreHeadlines = false
            } finally {
                isLoadingHeadlines = false
                if (page == 1) {
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }
    // ========================================================================

    // ==================== LOAD NEWS WITH PAGINATION ====================
    private fun loadNews(page: Int) {
        if (isLoadingNews) return

        isLoadingNews = true

        if (page == 1 && !swipeRefreshLayout.isRefreshing) {
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
                        hideEmptyState()
                    }
                } else {
                    if (page == 1) {
                        showEmptyState(ErrorType.SERVER_ERROR)
                    } else {
                        showToast("Failed to load news: ${response.code()}")
                    }
                    hasMoreNews = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (page == 1) {
                    when (e) {
                        is UnknownHostException -> showEmptyState(ErrorType.NO_CONNECTION)
                        is SocketTimeoutException -> showEmptyState(ErrorType.TIMEOUT)
                        else -> showEmptyState(ErrorType.UNKNOWN)
                    }
                } else {
                    showToast("Error loading news: ${e.message}")
                }
                hasMoreNews = false
            } finally {
                isLoadingNews = false
                if (page == 1) {
                    progressBar.visibility = View.GONE
                }
                swipeRefreshLayout.isRefreshing = false
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
            putExtra("sourceId", article.source.id)
            putExtra("imageUrl", article.urlToImage)
            putExtra("content", article.content)
            putExtra("description", article.description)
            putExtra("publishedAt", article.publishedAt)
            putExtra("url", article.url)
        }
        startActivity(intent)
    }
    // ==============================================================

    enum class ErrorType {
        NO_CONNECTION,
        TIMEOUT,
        SERVER_ERROR,
        UNKNOWN
    }
}
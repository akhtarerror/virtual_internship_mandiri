package dev.rakamin.newsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.rakamin.newsapp.R
import dev.rakamin.newsapp.model.Article

class MainAdapter(
    private val onNewsClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER_HEADLINES = 0
        private const val VIEW_TYPE_HEADLINES = 1
        private const val VIEW_TYPE_HEADER_NEWS = 2
        private const val VIEW_TYPE_NEWS = 3
    }

    private val items = mutableListOf<Any>()
    private val headlines = mutableListOf<Article>()
    private val news = mutableListOf<Article>()

    // ==================== PAGINATION CALLBACK ====================
    private var onHeadlinesScrollListener: ((Boolean) -> Unit)? = null

    fun setOnHeadlinesScrollListener(listener: (Boolean) -> Unit) {
        onHeadlinesScrollListener = listener
    }
    // ============================================================

    init {
        items.add("Top Headlines")
        items.add(headlines)
        items.add("Latest News")
    }

    // ==================== HEADLINES METHODS ====================
    fun submitHeadlines(newHeadlines: List<Article>) {
        headlines.clear()
        headlines.addAll(newHeadlines)
        notifyItemChanged(1)
    }

    fun addHeadlines(newHeadlines: List<Article>) {
        headlines.addAll(newHeadlines)
        notifyItemChanged(1)
    }
    // ===========================================================

    // ==================== NEWS METHODS ====================
    fun submitNews(newNews: List<Article>) {
        val startRemoval = 3
        val itemsToRemove = items.size - 3
        if (itemsToRemove > 0) {
            items.subList(3, items.size).clear()
            notifyItemRangeRemoved(startRemoval, itemsToRemove)
        }

        news.clear()
        news.addAll(newNews)
        items.addAll(newNews)
        notifyItemRangeInserted(3, newNews.size)
    }

    fun addNews(newNews: List<Article>) {
        val startPosition = items.size
        news.addAll(newNews)
        items.addAll(newNews)
        notifyItemRangeInserted(startPosition, newNews.size)
    }
    // ======================================================

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER_HEADLINES
            1 -> VIEW_TYPE_HEADLINES
            2 -> VIEW_TYPE_HEADER_NEWS
            else -> VIEW_TYPE_NEWS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER_HEADLINES, VIEW_TYPE_HEADER_NEWS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_HEADLINES -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_headlines_container, parent, false)
                HeadlinesViewHolder(view, onNewsClick, onHeadlinesScrollListener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_news, parent, false)
                NewsViewHolder(view, onNewsClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as String)
            is HeadlinesViewHolder -> holder.bind(headlines)
            is NewsViewHolder -> holder.bind(items[position] as Article)
        }
    }

    override fun getItemCount(): Int = items.size

    // ==================== HEADER VIEW HOLDER ====================
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textHeader)

        fun bind(title: String) {
            titleTextView.text = title
        }
    }
    // ===========================================================

    // ==================== HEADLINES VIEW HOLDER WITH PAGINATION ====================
    class HeadlinesViewHolder(
        itemView: View,
        private val onNewsClick: (Article) -> Unit,
        private val onScrollListener: ((Boolean) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerHeadlines)
        private val headlineAdapter = HeadlineAdapter(onNewsClick)

        init {
            val layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            recyclerView.apply {
                this.layoutManager = layoutManager
                adapter = headlineAdapter

                // Add scroll listener untuk pagination headlines
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                        // Trigger pagination saat mendekati akhir list (3 item sebelum akhir)
                        val isNearEnd = (lastVisibleItemPosition + 3) >= totalItemCount

                        if (isNearEnd && dx > 0) {
                            onScrollListener?.invoke(true)
                        }
                    }
                })
            }
        }

        fun bind(headlines: List<Article>) {
            headlineAdapter.submitList(headlines)
        }
    }
    // ===============================================================================

    // ==================== NEWS VIEW HOLDER ====================
    class NewsViewHolder(itemView: View, private val onNewsClick: (Article) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageNews)
        private val titleTextView: TextView = itemView.findViewById(R.id.textTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textDescription)
        private val sourceTextView: TextView = itemView.findViewById(R.id.textSource)

        fun bind(article: Article) {
            titleTextView.text = article.title
            descriptionTextView.text = article.description ?: "No description available"
            sourceTextView.text = article.source.name

            Glide.with(itemView.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imageView)

            itemView.setOnClickListener {
                onNewsClick(article)
            }
        }
    }
    // ==========================================================
}
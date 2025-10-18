package dev.rakamin.newsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.rakamin.newsapp.R
import dev.rakamin.newsapp.model.Article

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private val articles = mutableListOf<Article>()

    fun submitList(newArticles: List<Article>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }

    fun addArticles(newArticles: List<Article>) {
        val startPosition = articles.size
        articles.addAll(newArticles)
        notifyItemRangeInserted(startPosition, newArticles.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        }
    }
}

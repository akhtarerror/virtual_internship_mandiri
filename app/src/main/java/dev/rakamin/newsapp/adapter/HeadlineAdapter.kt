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

class HeadlineAdapter(
    private val onHeadlineClick: (Article) -> Unit
) : RecyclerView.Adapter<HeadlineAdapter.HeadlineViewHolder>() {

    private val headlines = mutableListOf<Article>()

    fun submitList(newHeadlines: List<Article>) {
        headlines.clear()
        headlines.addAll(newHeadlines)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadlineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_headline, parent, false)
        return HeadlineViewHolder(view, onHeadlineClick)
    }

    override fun onBindViewHolder(holder: HeadlineViewHolder, position: Int) {
        holder.bind(headlines[position])
    }

    override fun getItemCount(): Int = headlines.size

    class HeadlineViewHolder(
        itemView: View,
        private val onHeadlineClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageHeadline)
        private val titleTextView: TextView = itemView.findViewById(R.id.textHeadlineTitle)

        fun bind(article: Article) {
            titleTextView.text = article.title

            Glide.with(itemView.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imageView)

            itemView.setOnClickListener {
                onHeadlineClick(article)
            }
        }
    }
}
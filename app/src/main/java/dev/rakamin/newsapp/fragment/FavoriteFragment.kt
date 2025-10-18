package dev.rakamin.newsapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.rakamin.newsapp.DetailActivity
import dev.rakamin.newsapp.R
import dev.rakamin.newsapp.adapter.FavoriteAdapter
import dev.rakamin.newsapp.viewmodel.ArticleViewModel

class FavoriteFragment : Fragment() {

    private lateinit var viewModel: ArticleViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var adapter: FavoriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ArticleViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerFavorites)
        emptyView = view.findViewById(R.id.emptyView)

        setupRecyclerView()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter { article ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("title", article.title)
                putExtra("author", article.author)
                putExtra("source", article.sourceName)
                putExtra("sourceId", article.sourceId)
                putExtra("imageUrl", article.urlToImage)
                putExtra("content", article.content)
                putExtra("description", article.description)
                putExtra("publishedAt", article.publishedAt)
                putExtra("url", article.url)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeFavorites() {
        viewModel.allFavorites.observe(viewLifecycleOwner) { favorites ->
            if (favorites.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                adapter.submitList(favorites)
            }
        }
    }
}
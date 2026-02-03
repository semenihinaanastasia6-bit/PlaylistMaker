package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView

    private lateinit var recyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var noResultsView: LinearLayout
    private lateinit var errorView: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var searchHistoryTitle: TextView
    private lateinit var apiService: ApiService
    private lateinit var searchHistory: SearchHistory

    private var lastQuery: String? = null
    private var lastRequestHadError: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searchHistoryTitle = findViewById(R.id.searchHistoryTitle)
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        noResultsView = findViewById(R.id.noResultsView)
        errorView = findViewById(R.id.errorView)
        retryButton = findViewById(R.id.retryButton)
        val backButton = findViewById<ImageButton>(R.id.backbutton)
        backButton.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)

        apiService = ApiClient.apiService

        val sharedPrefs = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)

        showHistory()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                val text = query?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    performSearch(text)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val text = newText?.trim().orEmpty()
                if (text.isEmpty()) {

                    showHistory()
                }
                return true
            }
        })


        retryButton.setOnClickListener {
            if (lastRequestHadError) {
                lastQuery?.let { performSearch(it) }
            }
        }


        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            showHistory()
        }
    }

    private fun performSearch(query: String) {
        lastQuery = query
        lastRequestHadError = false

        Log.d("API_REQUEST", "Searching for: $query")


        noResultsView.visibility = View.GONE
        errorView.visibility = View.GONE

        apiService.search(query).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                Log.d("API_RESPONSE_CODE", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d("API_RESPONSE", "Response body: $apiResponse")

                    val tracks = apiResponse?.results ?: emptyList()

                    if (tracks.isNotEmpty()) {
                        showSearchResults(tracks)
                        lastRequestHadError = false
                    } else {
                        showNoResultsView()
                    }
                } else {
                    Log.e("API_ERROR", "Error code: ${response.code()}")
                    lastRequestHadError = true
                    showErrorView()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Failure: ${t.message}")
                lastRequestHadError = true
                showErrorView()
            }
        })
    }

    private fun showSearchResults(tracks: List<Track>) {
        recyclerView.adapter = TrackAdapter(tracks) { track ->
            // сохраняем в историю, если нужно
            searchHistory.saveTrack(track)

            val intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
            startActivity(intent)
        }

        recyclerView.visibility = View.VISIBLE
        noResultsView.visibility = View.GONE
        errorView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showNoResultsView() {
        recyclerView.visibility = View.GONE
        noResultsView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showErrorView() {
        recyclerView.visibility = View.GONE
        noResultsView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showHistory() {
        val historyList = searchHistory.getHistory()

        if (historyList.isNotEmpty()) {
            recyclerView.adapter = TrackAdapter(historyList) { track ->
                searchHistory.saveTrack(track)

                val intent = Intent(this, AudioPlayerActivity::class.java)
                intent.putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
                startActivity(intent)
            }

            recyclerView.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
        }

        noResultsView.visibility = View.GONE
        errorView.visibility = View.GONE
    }
}
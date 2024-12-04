package com.example.bookfinderapp

import android.content.Intent
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import androidx.appcompat.widget.SearchView
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var bookList: MutableList<Book>
    private lateinit var bookAdapter: BookAdapter
    private lateinit var searchView: SearchView
    private var favoriteBooks = mutableListOf<Book>() // List to track favorite books

    private val FAVORITE_BOOKS_REQUEST_CODE = 100 // Unique request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load favorite books from SharedPreferences
        favoriteBooks = SharedPrefsHelper.loadFavoriteBooks(this)

        // Initialize RecyclerView and the list to store Book data
        bookRecyclerView = findViewById(R.id.bookRecyclerView)
        bookRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        val infoIcon: ImageView = findViewById(R.id.InfromationIcon)
        infoIcon.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }

        bookList = mutableListOf()
        bookAdapter = BookAdapter(bookList) { book ->
            // Handle adding/removing from favorites
            if (!favoriteBooks.contains(book)) {
                favoriteBooks.add(book) // Add the book to favorites
                Toast.makeText(this, "${book.title} added to favorites!", Toast.LENGTH_SHORT).show()
                // Save updated favorite books list to SharedPreferences
                SharedPrefsHelper.saveFavoriteBooks(this, favoriteBooks)
            } else {
                favoriteBooks.remove(book) // Remove the book from favorites
                Toast.makeText(this, "${book.title} removed from favorites!", Toast.LENGTH_SHORT).show()
                // Save updated favorite books list to SharedPreferences
                SharedPrefsHelper.saveFavoriteBooks(this, favoriteBooks)
            }
        }

        // Set up the RecyclerView
        bookRecyclerView.layoutManager = LinearLayoutManager(this)
        bookRecyclerView.adapter = bookAdapter

        // Add custom spacing between items
        bookRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.top = 4
                outRect.bottom = 4
            }
        })

        // Set up the SearchView
        searchView = findViewById(R.id.search_view)
        searchView.setIconifiedByDefault(false) // Automatically expand the search bar on tap

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    fetchBooksBySearch(query) // Call the function to fetch books based on the search query
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Fetch random books when the search field is empty
                    fetchRandomBooks(10)
                } else {
                    fetchBooksBySearch(newText) // Fetch books as the user types
                }
                return true
            }
        })

        // Initially fetch random books
        fetchRandomBooks(10)

        // Handle favorite icon click to navigate to Favorite screen
        val favoriteIcon = findViewById<ImageView>(R.id.favoriteIcon)
        favoriteIcon.setOnClickListener {
            val intent = Intent(this, Favorite::class.java)
            intent.putParcelableArrayListExtra("favorite_books", ArrayList(favoriteBooks))
            startActivityForResult(intent, FAVORITE_BOOKS_REQUEST_CODE)
        }

        // Close the keyboard when tapping outside the search bar
        findViewById<View>(R.id.bookRecyclerView).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rect = Rect()
                searchView.getGlobalVisibleRect(rect)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    hideKeyboard() // Hide the keyboard if the user taps outside the search view
                }
            }
            false
        }
    }

    private fun fetchBooksBySearch(query: String) {
        if (isNetworkAvailable()) {
            val client = AsyncHttpClient()
            val addedTitles = mutableSetOf<String>() // Set to track added titles

            // Clear the list to avoid showing old books
            bookList.clear()

            // Notify the adapter after clearing the list
            bookAdapter.notifyDataSetChanged()

            // URL for fetching books based on search query
            val url = "https://www.googleapis.com/books/v1/volumes?q=$query&maxResults=10"

            client.get(url, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                    // Handle success response
                    val items = json.jsonObject.optJSONArray("items") ?: return
                    for (i in 0 until items.length()) {
                        val item = items.optJSONObject(i) ?: continue
                        val volumeInfo = item.optJSONObject("volumeInfo") ?: continue
                        val bookTitle = volumeInfo.optString("title", "No title")
                        val imageLinks = volumeInfo.optJSONObject("imageLinks")
                        val bookImageUrl = imageLinks?.optString("thumbnail", "") ?: ""
                        val bookAuthors = volumeInfo.optJSONArray("authors")?.let { authorsJsonArray ->
                            List(authorsJsonArray.length()) { index -> authorsJsonArray.getString(index) }
                        }?.joinToString(", ") ?: "Unknown Author"
                        val bookDescription = volumeInfo.optString("description", "No description available")

                        // Avoid duplicate books
                        if (!addedTitles.contains(bookTitle)) {
                            val newBook = Book(bookTitle, bookImageUrl, bookAuthors, bookDescription)
                            addedTitles.add(bookTitle)
                            bookList.add(newBook)
                        }
                    }

                    // Notify adapter on the main thread after all data is added
                    runOnUiThread {
                        bookAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                    // Handle failure response
                    Log.e("API Error", "Status Code: $statusCode, Error: $errorResponse", throwable)
                    Toast.makeText(this@MainActivity, "Failed to fetch books", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRandomBooks(count: Int) {
        if (isNetworkAvailable()) {
            val client = AsyncHttpClient()
            val addedTitles = mutableSetOf<String>() // Set to track added titles

            // Clear the list to avoid showing old books
            bookList.clear()

            // Notify the adapter after clearing the list
            bookAdapter.notifyDataSetChanged()

            // Fetch random fiction books
            val randomStartIndex = (0..1000).random() // Random start index for pagination
            val url = "https://www.googleapis.com/books/v1/volumes?q=fiction&maxResults=$count&startIndex=$randomStartIndex"

            client.get(url, object : JsonHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                    val items = json.jsonObject.optJSONArray("items") ?: return
                    for (i in 0 until items.length()) {
                        val item = items.optJSONObject(i) ?: continue
                        val volumeInfo = item.optJSONObject("volumeInfo") ?: continue

                        val bookTitle = volumeInfo.optString("title", "No title")
                        val imageLinks = volumeInfo.optJSONObject("imageLinks")
                        val bookImageUrl = imageLinks?.optString("thumbnail", "") ?: ""
                        val bookAuthors = volumeInfo.optJSONArray("authors")?.let { authorsJsonArray ->
                            List(authorsJsonArray.length()) { index -> authorsJsonArray.getString(index) }
                        }?.joinToString(", ") ?: "Unknown Author"
                        val bookDescription = volumeInfo.optString("description", "No description available")

                        // Avoid duplicate books
                        if (!addedTitles.contains(bookTitle)) {
                            val newBook = Book(bookTitle, bookImageUrl, bookAuthors, bookDescription)
                            addedTitles.add(bookTitle)
                            bookList.add(newBook)
                        }
                    }

                    // Notify adapter on the main thread after all data is added
                    runOnUiThread {
                        bookAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                    // Handle failure response
                    Log.e("API Error", "Status Code: $statusCode, Error: $errorResponse", throwable)
                    Toast.makeText(this@MainActivity, "Failed to fetch books", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        currentFocusView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FAVORITE_BOOKS_REQUEST_CODE && resultCode == RESULT_OK) {
            // Update the favorite books list when returning from the Favorite activity
            favoriteBooks = SharedPrefsHelper.loadFavoriteBooks(this)
        }
    }
}

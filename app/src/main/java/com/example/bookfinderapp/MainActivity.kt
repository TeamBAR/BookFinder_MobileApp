package com.example.bookfinderapp

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class MainActivity : AppCompatActivity() {
    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var bookList: MutableList<Book>
    private lateinit var bookAdapter: BookAdapter
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView and the list to store Book data
        bookRecyclerView = findViewById(R.id.bookRecyclerView)
        bookList = mutableListOf()
        bookAdapter = BookAdapter(bookList)

        // Set up the RecyclerView
        bookRecyclerView.layoutManager = LinearLayoutManager(this)
        bookRecyclerView.adapter = bookAdapter

        // Add custom spacing between items
        bookRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                // Control the spacing between items
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
        val client = AsyncHttpClient()
        val addedTitles = mutableSetOf<String>() // Set to track added titles

        // Clear the list to avoid showing old books
        bookList.clear()
        bookAdapter.notifyDataSetChanged()

        // URL for fetching books based on search query
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query&maxResults=10"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                Log.d("BooksAPI", "Response: $json")

                // Safely extract the 'items' array from the JSON response
                val items = json.jsonObject.optJSONArray("items") ?: return
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val volumeInfo = item.optJSONObject("volumeInfo") ?: continue

                    // Get the book title
                    val bookTitle = volumeInfo.optString("title", "No title")

                    // Safely handle the imageLinks field
                    val imageLinks = volumeInfo.optJSONObject("imageLinks")
                    val bookImageUrl = imageLinks?.optString("thumbnail", "") ?: "" // Default to empty string if not found

                    // Get the authors (handle if authors is missing or null)
                    val bookAuthors = volumeInfo.optJSONArray("authors")?.let { authorsJsonArray ->
                        List(authorsJsonArray.length()) { index ->
                            authorsJsonArray.getString(index) // Use 'index' to get each author
                        }
                    }?.joinToString(", ") ?: "Unknown Author"

                    // Get the book description (handle missing descriptions)
                    val bookDescription = volumeInfo.optString("description", "No description available")

                    // Check if this book is already added
                    if (!addedTitles.contains(bookTitle)) {
                        // Create a new Book object
                        val newBook = Book(bookTitle, bookImageUrl, bookAuthors, bookDescription)
                        // Add the title to the set
                        addedTitles.add(bookTitle)

                        // Add the book to the list
                        bookList.add(newBook)

                        // Notify the adapter of the new item
                        bookAdapter.notifyItemInserted(bookList.size - 1)
                    }
                }

                // Notify the adapter that the data has changed
                bookAdapter.notifyDataSetChanged()
            }

            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.e("BooksAPI", "Failed to fetch Book data: $errorResponse")
            }
        })
    }

    private fun fetchRandomBooks(count: Int) {
        val client = AsyncHttpClient()
        val addedTitles = mutableSetOf<String>() // Set to track added titles

        // Clear the list to avoid showing old books
        bookList.clear()
        bookAdapter.notifyDataSetChanged()

        // Use a broad search term like "bestseller" and randomly pick a start index
        val randomStartIndex = (0..1000).random()  // Random start index for pagination
        val url = "https://www.googleapis.com/books/v1/volumes?q=bestseller&maxResults=$count&startIndex=$randomStartIndex"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                Log.d("BooksAPI", "Response: $json")

                // Parse the response JSON to get Book data
                val items = json.jsonObject.optJSONArray("items") ?: return
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val volumeInfo = item.optJSONObject("volumeInfo") ?: continue

                    val bookTitle = volumeInfo.optString("title", "No title")
                    val imageLinks = volumeInfo.optJSONObject("imageLinks")
                    val bookImageUrl = imageLinks?.optString("thumbnail", "") ?: ""
                    val bookAuthors = volumeInfo.optJSONArray("authors")?.let { authorsJsonArray ->
                        List(authorsJsonArray.length()) { index ->
                            authorsJsonArray.getString(index)
                        }
                    }?.joinToString(", ") ?: "Unknown Author"
                    val bookDescription = volumeInfo.optString("description", "No description available")

                    if (!addedTitles.contains(bookTitle)) {
                        val newBook = Book(bookTitle, bookImageUrl, bookAuthors, bookDescription)
                        addedTitles.add(bookTitle)
                        bookList.add(newBook)
                        bookAdapter.notifyItemInserted(bookList.size - 1)
                    }
                }

                bookAdapter.notifyDataSetChanged()
            }

            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.e("BooksAPI", "Failed to fetch Book data: $errorResponse")
            }
        })
    }

    // Function to hide the keyboard
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        currentFocusView?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}

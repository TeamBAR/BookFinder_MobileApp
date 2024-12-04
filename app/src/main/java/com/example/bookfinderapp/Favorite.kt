package com.example.bookfinderapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Favorite : AppCompatActivity() {

    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteBooksAdapter: BookAdapter
    private lateinit var favoriteBooks: MutableList<Book> // List to store favorite books

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorite_book)

        // Retrieve the favorite books passed from MainActivity
        favoriteBooks = intent.getParcelableArrayListExtra("favorite_books") ?: mutableListOf()

        // Set up the RecyclerView for displaying favorite books
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView)
        favoriteRecyclerView.layoutManager = LinearLayoutManager(this)

        favoriteBooksAdapter = BookAdapter(favoriteBooks) { book ->
            // Handle remove from favorites action
            favoriteBooks.remove(book)
            favoriteBooksAdapter.notifyDataSetChanged()
            Toast.makeText(this, "${book.title} removed from favorites", Toast.LENGTH_SHORT).show()

            // Send updated list back to MainActivity
            val resultIntent = intent
            resultIntent.putParcelableArrayListExtra("favorite_books", ArrayList(favoriteBooks))
            setResult(RESULT_OK, resultIntent)  // Send updated favorites back to MainActivity
        }
        favoriteRecyclerView.adapter = favoriteBooksAdapter

        // Handle back icon to navigate back to MainActivity
        val backIcon: ImageView = findViewById(R.id.backIcon)
        backIcon.setOnClickListener {
            finish() // Finish the activity to return to MainActivity
        }

        // Handle "Home" icon (Library button) click
        val homeIcon: ImageView = findViewById(R.id.libraryIcon)
        homeIcon.setOnClickListener {
            finish() // Close current activity and return to MainActivity
        }
    }
}

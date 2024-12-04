package com.example.bookfinderapp

import android.content.Intent
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

        // Load the favorite books from SharedPreferences
        favoriteBooks = SharedPrefsHelper.loadFavoriteBooks(this)

        // Set up the RecyclerView for displaying favorite books
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView)
        favoriteRecyclerView.layoutManager = LinearLayoutManager(this)

        favoriteBooksAdapter = BookAdapter(favoriteBooks) { book ->
            // Handle remove from favorites action
            favoriteBooks.remove(book)
            favoriteBooksAdapter.notifyDataSetChanged()
            Toast.makeText(this, "${book.title} removed from favorites", Toast.LENGTH_SHORT).show()

            // Save the updated favorite books list to SharedPreferences
            SharedPrefsHelper.saveFavoriteBooks(this, favoriteBooks)

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
        // Library button functionality
        val libraryButton: ImageView = findViewById(R.id.libraryIcon) // Changed to ImageView
        libraryButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Info button functionality
        val infoButton: ImageView = findViewById(R.id.InfromationIcon) // Changed to ImageView
        infoButton.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }

        // If the user is returning from MainActivity with new books to add
        val newFavoriteBooks: List<Book>? = intent.getParcelableArrayListExtra("new_favorite_books")
        newFavoriteBooks?.let {
            // Add new books to the list
            favoriteBooks.addAll(it)
            favoriteBooksAdapter.notifyDataSetChanged() // Notify the adapter that the data has changed

            // Save the updated list back to SharedPreferences
            SharedPrefsHelper.saveFavoriteBooks(this, favoriteBooks)
        }
    }

    override fun onPause() {
        super.onPause()
        // Save the favorite books when the activity is paused or closed
        SharedPrefsHelper.saveFavoriteBooks(this, favoriteBooks)
    }
}

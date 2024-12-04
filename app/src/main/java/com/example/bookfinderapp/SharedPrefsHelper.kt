package com.example.bookfinderapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefsHelper {

    private const val PREFS_NAME = "favorites"
    private const val FAVORITE_BOOKS_KEY = "favorite_books"  // Key for storing books

    // Save the list of favorite books to SharedPreferences
    fun saveFavoriteBooks(context: Context, books: List<Book>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Convert the list of books to JSON using Gson
        val gson = Gson()
        val json = gson.toJson(books)

        // Save the JSON string to SharedPreferences
        editor.putString(FAVORITE_BOOKS_KEY, json)
        editor.apply()
    }

    // Load the list of favorite books from SharedPreferences
    fun loadFavoriteBooks(context: Context): MutableList<Book> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Retrieve the JSON string from SharedPreferences
        val json = sharedPreferences.getString(FAVORITE_BOOKS_KEY, null)

        // If the JSON string is null, return an empty list
        if (json == null) {
            return mutableListOf()
        }

        // Convert the JSON string back into a list of Book objects using Gson
        val gson = Gson()
        val type = object : TypeToken<List<Book>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}

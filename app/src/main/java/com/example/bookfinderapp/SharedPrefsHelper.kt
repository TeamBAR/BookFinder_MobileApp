package com.example.bookfinderapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefsHelper {

    private const val PREFS_NAME = "bookfinder_prefs"
    private const val FAVORITES_KEY = "favorite_books"

    // Save favorite books to SharedPreferences
    fun saveFavoriteBooks(context: Context, books: List<Book>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(books)
        editor.putString(FAVORITES_KEY, json)
        editor.apply()
    }

    // Load favorite books from SharedPreferences
    fun loadFavoriteBooks(context: Context): MutableList<Book> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(FAVORITES_KEY, null)
        val type = object : TypeToken<List<Book>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}

package com.example.bookfinderapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(private val bookList: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    // ViewHolder class to hold references to the views in the layout for each item
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        val authorTextView: TextView = itemView.findViewById(R.id.bookAuthor)
        val bookImageView: ImageView = itemView.findViewById(R.id.bookImage)
        val descriptionTextView: TextView = itemView.findViewById(R.id.bookDescription) // Add this line
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.authors
        holder.descriptionTextView.text = book.description // Bind the description to the TextView

        // Log the image URL for debugging
        Log.d("BookAdapter", "Loading image from URL: ${book.imageUrl}")

        // Ensure the URL starts with https:// instead of http://
        var imageUrl = book.imageUrl
        if (imageUrl.startsWith("http://")) {
            imageUrl = imageUrl.replace("http://", "https://")
        }

        // Load the image using Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl) // URL for the image
            .into(holder.bookImageView)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}

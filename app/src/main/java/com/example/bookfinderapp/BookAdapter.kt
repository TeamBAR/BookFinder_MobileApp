package com.example.bookfinderapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(
    private val bookList: List<Book>,
    private val onFavoriteClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        val authorTextView: TextView = itemView.findViewById(R.id.bookAuthor)
        val bookImageView: ImageView = itemView.findViewById(R.id.bookImage)
        val descriptionTextView: TextView = itemView.findViewById(R.id.bookDescription)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.bookFavoriteIcon)

        init {
            favoriteIcon.setOnClickListener {
                val book = bookList[adapterPosition]
                onFavoriteClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.authors
        holder.descriptionTextView.text = book.description

        // Check if imageUrl starts with http://, and replace it with https:// if necessary
        var imageUrl = book.imageUrl
        if (imageUrl.startsWith("http://")) {
            imageUrl = imageUrl.replace("http://", "https://")
        }

        // Use Glide to load the image into the ImageView
        Glide.with(holder.bookImageView.context)
            .load(imageUrl)
            .into(holder.bookImageView)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}

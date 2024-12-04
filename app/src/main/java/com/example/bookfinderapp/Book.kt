package com.example.bookfinderapp

import android.os.Parcel
import android.os.Parcelable

data class Book(
    val title: String,
    val imageUrl: String,
    val authors: String,
    val description: String
) : Parcelable {

    // Constructor that takes a Parcel to recreate the object
    constructor(parcel: Parcel) : this(
        title = parcel.readString() ?: "",
        imageUrl = parcel.readString() ?: "",
        authors = parcel.readString() ?: "",
        description = parcel.readString() ?: ""
    )

    // Write the object to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(imageUrl)
        parcel.writeString(authors)
        parcel.writeString(description)
    }

    // Describe contents (usually returns 0)
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        // Create a new Book from a Parcel
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        // Create an array of Books
        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}

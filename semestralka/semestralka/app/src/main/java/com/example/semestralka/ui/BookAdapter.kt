package com.example.semestralka.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.semestralka.data.Book
import com.example.semestralka.databinding.ItemBookBinding

class BookAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onBookLongClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
        holder.itemView.setOnClickListener { onBookClick(book) }
        holder.itemView.setOnLongClickListener {
            onBookLongClick(book)
            true
        }
    }

    class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.apply {
                tvTitle.text = book.title
                tvAuthor.text = book.author
                rbRating.rating = book.rating
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
    }
}

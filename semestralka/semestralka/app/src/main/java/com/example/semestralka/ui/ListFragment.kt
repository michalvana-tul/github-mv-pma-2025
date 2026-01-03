package com.example.semestralka.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semestralka.data.AppDatabase
import com.example.semestralka.data.UserPreferencesRepository
import com.example.semestralka.databinding.FragmentListBinding
import com.example.semestralka.viewmodel.BookViewModel
import com.example.semestralka.viewmodel.BookViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = UserPreferencesRepository(requireContext())
        BookViewModelFactory(database.bookDao(), repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BookAdapter(
            onBookClick = { book ->
                val action = ListFragmentDirections.actionListFragmentToAddEditFragment(book.id)
                findNavController().navigate(action)
            },
            onBookLongClick = { book ->
                shareBook(book)
            }
        )

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAdd.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToAddEditFragment(-1)
            findNavController().navigate(action)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(ListFragmentDirections.actionListFragmentToSettingsFragment())
        }

        // Swipe to delete implementation
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val book = adapter.currentList[position]
                
                viewModel.deleteBook(book)
                
                Snackbar.make(binding.root, "Book deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.insertBook(book)
                    }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerView)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allBooks.collect { books ->
                    adapter.submitList(books)
                }
            }
        }
    }

    private fun shareBook(book: com.example.semestralka.data.Book) {
        val shareText = "I recommend reading ${book.title} by ${book.author}!"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooser = Intent.createChooser(intent, "Share Book")
        startActivity(chooser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

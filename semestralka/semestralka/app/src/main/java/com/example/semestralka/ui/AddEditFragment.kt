package com.example.semestralka.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.semestralka.data.AppDatabase
import com.example.semestralka.data.Book
import com.example.semestralka.data.UserPreferencesRepository
import com.example.semestralka.databinding.FragmentAddEditBinding
import com.example.semestralka.viewmodel.BookViewModel
import com.example.semestralka.viewmodel.BookViewModelFactory
import kotlinx.coroutines.launch

class AddEditFragment : Fragment() {
    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditFragmentArgs by navArgs()

    private val viewModel: BookViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = UserPreferencesRepository(requireContext())
        BookViewModelFactory(database.bookDao(), repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = args.bookId

        if (bookId != -1) {
            binding.btnDelete.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                val book = viewModel.getBookById(bookId)
                book?.let {
                    binding.etTitle.setText(it.title)
                    binding.etAuthor.setText(it.author)
                    binding.rbRating.rating = it.rating
                    binding.cbIsRead.isChecked = it.isRead
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val author = binding.etAuthor.text.toString()
            val rating = binding.rbRating.rating
            val isRead = binding.cbIsRead.isChecked

            if (title.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val book = Book(
                id = if (bookId == -1) 0 else bookId,
                title = title,
                author = author,
                rating = rating,
                isRead = isRead
            )

            if (bookId == -1) {
                viewModel.insertBook(book)
            } else {
                viewModel.updateBook(book)
            }

            findNavController().navigateUp()
        }

        binding.btnDelete.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val book = viewModel.getBookById(bookId)
                book?.let {
                    viewModel.deleteBook(it)
                    Toast.makeText(requireContext(), "Book deleted", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

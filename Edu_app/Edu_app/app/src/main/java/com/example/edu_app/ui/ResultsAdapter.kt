package com.example.edu_app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edu_app.data.GameResultEntity
import com.example.edu_app.databinding.ItemResultBinding
import java.text.SimpleDateFormat
import java.util.*

class ResultsAdapter(private var results: List<GameResultEntity>) : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

    class ResultViewHolder(val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.binding.tvDate.text = sdf.format(Date(result.timestamp))
        holder.binding.tvScore.text = "Score: ${result.score}"
    }

    override fun getItemCount() = results.size

    fun updateData(newResults: List<GameResultEntity>) {
        results = newResults
        notifyDataSetChanged()
    }
}

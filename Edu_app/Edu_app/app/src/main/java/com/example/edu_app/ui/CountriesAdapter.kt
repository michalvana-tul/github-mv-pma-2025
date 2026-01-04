package com.example.edu_app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edu_app.data.CountryEntity
import com.example.edu_app.databinding.ItemCountryBinding

class CountriesAdapter(
    private var countries: List<CountryEntity>,
    private val onEdit: (CountryEntity) -> Unit,
    private val onDelete: (CountryEntity) -> Unit
) : RecyclerView.Adapter<CountriesAdapter.CountryViewHolder>() {

    class CountryViewHolder(val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = countries[position]
        holder.binding.tvCountryName.text = country.countryName
        holder.binding.tvCapitalName.text = "Capital: ${country.capitalCity} (${country.region})"
        
        holder.binding.btnEditCountry.setOnClickListener { onEdit(country) }
        holder.binding.btnDeleteCountry.setOnClickListener { onDelete(country) }
    }

    override fun getItemCount() = countries.size

    fun updateData(newCountries: List<CountryEntity>) {
        countries = newCountries
        notifyDataSetChanged()
    }
}

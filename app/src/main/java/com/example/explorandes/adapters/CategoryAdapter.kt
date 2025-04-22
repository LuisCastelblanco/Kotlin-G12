package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.R
import com.example.explorandes.models.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
        val categoryIndicator: View = itemView.findViewById(R.id.category_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Make sure this layout exists and contains the views CategoryViewHolder is looking for
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.categoryName.text = category.name

        // Set icon if available
        if (category.iconResId > 0) {
            holder.categoryIcon.setImageResource(category.iconResId)
            holder.categoryIcon.visibility = View.VISIBLE
        } else {
            holder.categoryIcon.visibility = View.GONE
        }

        // Highlight selected category
        if (position == selectedPosition) {
            holder.categoryIndicator.visibility = View.VISIBLE
            holder.categoryName.setTextColor(holder.itemView.context.getColor(R.color.colorPrimary))
        } else {
            holder.categoryIndicator.visibility = View.INVISIBLE
            holder.categoryName.setTextColor(holder.itemView.context.getColor(R.color.colorTextSecondary))
        }

        holder.itemView.setOnClickListener {
            // Update selection
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            // Notify adapter to redraw the changed items
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)

            // Trigger callback
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        selectedPosition = 0  // Reset selection to first item
        notifyDataSetChanged()
    }
}
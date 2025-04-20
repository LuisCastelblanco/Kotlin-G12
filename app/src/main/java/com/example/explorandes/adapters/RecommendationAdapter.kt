package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.R
import com.example.explorandes.models.Recommendation
import com.example.explorandes.models.RecommendationType

class RecommendationAdapter(
    private val recommendations: List<Recommendation>,
    private val onRecommendationClick: (Recommendation) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recommendationImage: ImageView = itemView.findViewById(R.id.recommendation_image)
        val recommendationTitle: TextView = itemView.findViewById(R.id.recommendation_title)
        val recommendationType: TextView = itemView.findViewById(R.id.recommendation_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]

        holder.recommendationTitle.text = recommendation.title

        // Set the type text based on the enum
        holder.recommendationType.text = when(recommendation.type) {
            RecommendationType.PODCAST -> "Podcast"
            RecommendationType.DOCUMENTARY -> "Documentary"
            RecommendationType.THEATER -> "Theater"
            RecommendationType.EVENT -> "Event"
        }

        // Set image resource
        holder.recommendationImage.setImageResource(recommendation.imageResId)

        holder.itemView.setOnClickListener {
            onRecommendationClick(recommendation)
        }
    }

    override fun getItemCount() = recommendations.size
}
package com.example.explorandes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.R
import com.example.explorandes.models.Recommendation

class RecommendationAdapter(
    private val recommendations: List<Recommendation>,
    private val onRecommendationClickListener: (Recommendation) -> Unit = {}
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recommendationImage: ImageView = itemView.findViewById(R.id.recommendation_image)
        val recommendationType: TextView = itemView.findViewById(R.id.recommendation_type)
        val recommendationDescription: TextView = itemView.findViewById(R.id.recommendation_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]
        
        // Format the title based on recommendation type
        val formattedTitle = when (recommendation.type) {
            com.example.explorandes.models.RecommendationType.PODCAST -> "Podcast: \"${recommendation.title}\""
            com.example.explorandes.models.RecommendationType.DOCUMENTARY -> "Documental: ${recommendation.title}"
            com.example.explorandes.models.RecommendationType.THEATER -> "Teatro: ${recommendation.title}"
            com.example.explorandes.models.RecommendationType.EVENT -> "Evento: ${recommendation.title}"
        }
        
        holder.recommendationType.text = formattedTitle
        holder.recommendationDescription.text = recommendation.description
        holder.recommendationImage.setImageResource(recommendation.imageResId)
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onRecommendationClickListener(recommendation)
        }
    }

    override fun getItemCount() = recommendations.size
}
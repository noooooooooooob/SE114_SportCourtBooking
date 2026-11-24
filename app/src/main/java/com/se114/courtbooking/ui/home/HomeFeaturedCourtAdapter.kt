package com.se114.courtbooking.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.se114.courtbooking.databinding.ItemHomeFeaturedCourtBinding

internal data class HomeFeaturedCourt(
    val name: String,
    val address: String,
    val price: String,
    val rating: String,
    val imageRes: Int
)

internal class HomeFeaturedCourtAdapter :
    androidx.recyclerview.widget.ListAdapter<HomeFeaturedCourt, HomeFeaturedCourtAdapter.CourtHolder>(Diff) {

    private var adaptiveCardWidthPx = 0
    private val widthListener = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        (view as RecyclerView).post { resizeCards(view) }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnLayoutChangeListener(widthListener)
        recyclerView.post { resizeCards(recyclerView) }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnLayoutChangeListener(widthListener)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    /** Uses the screen-width card size and leaves a small next-card peek in the carousel. */
    private fun resizeCards(recyclerView: RecyclerView) {
        val maxWidth = recyclerView.resources.getDimensionPixelSize(_root_ide_package_.com.se114.courtbooking.R.dimen.home_dp_320)
        // The adaptive spacing scales with the card dimensions selected for this screen width.
        val trailingSpace = recyclerView.resources.getDimensionPixelSize(_root_ide_package_.com.se114.courtbooking.R.dimen.home_dp_22)
        val nextWidth = minOf(maxWidth, recyclerView.width - trailingSpace).coerceAtLeast(1)
        if (adaptiveCardWidthPx == nextWidth) return

        adaptiveCardWidthPx = nextWidth
        if (itemCount > 0) notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtHolder {
        val binding = ItemHomeFeaturedCourtBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CourtHolder(binding)
    }

    override fun onBindViewHolder(holder: CourtHolder, position: Int) {
        holder.setCardWidth(adaptiveCardWidthPx)
        holder.bind(getItem(position))
    }

    class CourtHolder(private val binding: ItemHomeFeaturedCourtBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setCardWidth(widthPx: Int) {
            if (widthPx > 0 && binding.root.layoutParams.width != widthPx) {
                binding.root.layoutParams = binding.root.layoutParams.apply {
                    width = widthPx
                }
            }
        }

        fun bind(court: HomeFeaturedCourt) {
            binding.courtName.text = court.name
            binding.courtAddress.text = court.address
            binding.courtRating.text = court.rating
            binding.courtPrice.text = binding.root.context.getString(
                _root_ide_package_.com.se114.courtbooking.R.string.home_featured_price,
                court.price
            )
            Glide.with(binding.courtPhoto)
                .load(court.imageRes)
                .centerCrop()
                .into(binding.courtPhoto)
        }
    }

    private companion object Diff : DiffUtil.ItemCallback<HomeFeaturedCourt>() {
        override fun areItemsTheSame(oldItem: HomeFeaturedCourt, newItem: HomeFeaturedCourt): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: HomeFeaturedCourt, newItem: HomeFeaturedCourt): Boolean =
            oldItem == newItem
    }
}

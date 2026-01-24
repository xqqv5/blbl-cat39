package blbl.cat3399.feature.search

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import blbl.cat3399.R
import blbl.cat3399.core.ui.UiScale
import blbl.cat3399.databinding.ItemSearchSuggestBinding
import kotlin.math.roundToInt

class SearchSuggestAdapter(
    private val onClick: (String) -> Unit,
) : RecyclerView.Adapter<SearchSuggestAdapter.Vh>() {
    private val items = ArrayList<String>()

    init {
        setHasStableIds(true)
    }

    fun invalidateSizing() {
        if (itemCount <= 0) return
        notifyItemRangeChanged(0, itemCount)
    }

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemSearchSuggestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class Vh(private val binding: ItemSearchSuggestBinding) : RecyclerView.ViewHolder(binding.root) {
        private var lastUiScale: Float? = null

        fun bind(keyword: String, onClick: (String) -> Unit) {
            val uiScale = UiScale.factor(binding.root.context)
            if (lastUiScale != uiScale) {
                applySizing(uiScale)
                lastUiScale = uiScale
            }
            binding.tvKeyword.text = keyword
            binding.root.setOnClickListener { onClick(keyword) }
        }

        private fun applySizing(uiScale: Float) {
            fun px(id: Int): Int = binding.root.resources.getDimensionPixelSize(id)
            fun pxF(id: Int): Float = binding.root.resources.getDimension(id)
            fun scaledPx(id: Int): Int = (px(id) * uiScale).roundToInt().coerceAtLeast(0)
            fun scaledPxF(id: Int): Float = pxF(id) * uiScale

            (binding.card.layoutParams as? MarginLayoutParams)?.let { lp ->
                val mb = scaledPx(R.dimen.search_suggest_margin_bottom_tv)
                if (lp.bottomMargin != mb) {
                    lp.bottomMargin = mb
                    binding.card.layoutParams = lp
                }
            }

            val padH = scaledPx(R.dimen.search_suggest_padding_h_tv)
            val padV = scaledPx(R.dimen.search_suggest_padding_v_tv)
            if (binding.tvKeyword.paddingLeft != padH || binding.tvKeyword.paddingTop != padV || binding.tvKeyword.paddingRight != padH || binding.tvKeyword.paddingBottom != padV) {
                binding.tvKeyword.setPadding(padH, padV, padH, padV)
            }

            binding.tvKeyword.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                scaledPxF(R.dimen.search_suggest_text_size_tv),
            )
        }
    }
}

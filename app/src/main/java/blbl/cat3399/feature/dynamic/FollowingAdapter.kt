package blbl.cat3399.feature.dynamic

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import blbl.cat3399.R
import blbl.cat3399.core.image.ImageLoader
import blbl.cat3399.core.image.ImageUrl
import blbl.cat3399.core.ui.UiScale
import blbl.cat3399.databinding.ItemFollowingBinding
import kotlin.math.roundToInt

class FollowingAdapter(
    private val onClick: (FollowingUi) -> Unit,
) : RecyclerView.Adapter<FollowingAdapter.Vh>() {
    data class FollowingUi(
        val mid: Long,
        val name: String,
        val avatarUrl: String?,
        val isAll: Boolean = false,
    )

    private val items = ArrayList<FollowingUi>()
    private var selectedMid: Long = MID_ALL

    init {
        setHasStableIds(true)
    }

    fun invalidateSizing() {
        if (itemCount <= 0) return
        notifyItemRangeChanged(0, itemCount)
    }

    fun submit(list: List<FollowingUi>, selected: Long = MID_ALL) {
        val prevSelected = selectedMid
        val sameItems =
            items.size == list.size &&
                items.indices.all { idx -> items[idx].mid == list[idx].mid }

        selectedMid = selected
        if (sameItems) {
            val prevPos = items.indexOfFirst { it.mid == prevSelected }
            val newPos = items.indexOfFirst { it.mid == selectedMid }
            if (prevPos >= 0) notifyItemChanged(prevPos)
            if (newPos >= 0 && newPos != prevPos) notifyItemChanged(newPos)
            return
        }

        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun append(list: List<FollowingUi>) {
        if (list.isEmpty()) return
        val start = items.size
        items.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }

    override fun getItemId(position: Int): Long = items[position].mid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemFollowingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) =
        holder.bind(items[position], items[position].mid == selectedMid, onClick)

    override fun getItemCount(): Int = items.size

    class Vh(private val binding: ItemFollowingBinding) : RecyclerView.ViewHolder(binding.root) {
        private var lastUiScale: Float? = null

        fun bind(item: FollowingUi, selected: Boolean, onClick: (FollowingUi) -> Unit) {
            val uiScale = UiScale.factor(binding.root.context)
            if (lastUiScale != uiScale) {
                applySizing(uiScale)
                lastUiScale = uiScale
            }

            binding.tvName.text = item.name
            if (item.isAll) {
                binding.ivAvatar.setImageResource(blbl.cat3399.R.drawable.ic_all)
                binding.ivAvatar.imageTintList =
                    android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(binding.root.context, blbl.cat3399.R.color.blbl_text),
                    )
            } else {
                binding.ivAvatar.imageTintList = null
                ImageLoader.loadInto(binding.ivAvatar, ImageUrl.avatar(item.avatarUrl))
            }
            binding.vSelected.visibility = if (selected) android.view.View.VISIBLE else android.view.View.GONE
            binding.root.isSelected = selected
            binding.tvName.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (selected) blbl.cat3399.R.color.blbl_text else blbl.cat3399.R.color.blbl_text_secondary,
                ),
            )
            binding.root.setOnClickListener { onClick(item) }
        }

        private fun applySizing(uiScale: Float) {
            fun px(id: Int): Int = binding.root.resources.getDimensionPixelSize(id)
            fun pxF(id: Int): Float = binding.root.resources.getDimension(id)
            fun scaledPx(id: Int): Int = (px(id) * uiScale).roundToInt().coerceAtLeast(0)
            fun scaledPxF(id: Int): Float = pxF(id) * uiScale

            val height = scaledPx(R.dimen.following_item_height_tv).coerceAtLeast(1)
            val rootLp = binding.root.layoutParams
            if (rootLp != null && rootLp.height != height) {
                rootLp.height = height
                binding.root.layoutParams = rootLp
            }

            (binding.root.layoutParams as? MarginLayoutParams)?.let { lp ->
                val mv = scaledPx(R.dimen.following_item_margin_v_tv)
                val mh = scaledPx(R.dimen.following_item_margin_h_tv)
                if (lp.topMargin != mv || lp.bottomMargin != mv || lp.leftMargin != mh || lp.rightMargin != mh) {
                    lp.topMargin = mv
                    lp.bottomMargin = mv
                    lp.leftMargin = mh
                    lp.rightMargin = mh
                    binding.root.layoutParams = lp
                }
            }

            val padH = scaledPx(R.dimen.following_container_padding_h_tv)
            if (binding.container.paddingLeft != padH || binding.container.paddingRight != padH) {
                binding.container.setPadding(
                    padH,
                    binding.container.paddingTop,
                    padH,
                    binding.container.paddingBottom,
                )
            }

            val selectedWidth = scaledPx(R.dimen.following_selected_width_tv)
            val selectedHeight = scaledPx(R.dimen.following_selected_height_tv)
            val selectedMarginEnd = scaledPx(R.dimen.following_selected_margin_end_tv)
            val selectedLp = binding.vSelected.layoutParams as? ViewGroup.MarginLayoutParams
            if (selectedLp != null) {
                if (selectedLp.width != selectedWidth || selectedLp.height != selectedHeight || selectedLp.marginEnd != selectedMarginEnd) {
                    selectedLp.width = selectedWidth
                    selectedLp.height = selectedHeight
                    selectedLp.marginEnd = selectedMarginEnd
                    binding.vSelected.layoutParams = selectedLp
                }
            }

            val avatarSize = scaledPx(R.dimen.following_avatar_size_tv).coerceAtLeast(1)
            val avatarLp = binding.ivAvatar.layoutParams
            if (avatarLp.width != avatarSize || avatarLp.height != avatarSize) {
                avatarLp.width = avatarSize
                avatarLp.height = avatarSize
                binding.ivAvatar.layoutParams = avatarLp
            }

            val nameLp = binding.tvName.layoutParams as? ViewGroup.MarginLayoutParams
            if (nameLp != null) {
                val ms = scaledPx(R.dimen.following_name_margin_start_tv)
                if (nameLp.marginStart != ms || nameLp.height != avatarSize) {
                    nameLp.marginStart = ms
                    nameLp.height = avatarSize
                    binding.tvName.layoutParams = nameLp
                }
            }
            binding.tvName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                scaledPxF(R.dimen.following_name_text_size_tv),
            )
        }
    }

    companion object {
        const val MID_ALL: Long = -1L
    }
}

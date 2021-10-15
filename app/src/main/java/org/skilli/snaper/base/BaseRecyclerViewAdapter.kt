package org.skilli.snaper.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import org.skilli.snaper.R
import org.skilli.snaper.base.adapters.BaseRecyclerViewHolderClickable
import org.skilli.snaper.databinding.GridOnewBinding
import org.skilli.snaper.repos.structure.ResponseData
import org.skilli.snaper.utils.logit
import java.lang.ref.WeakReference

class BaseRecyclerViewAdapter<X, Y : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> Y,
    private var list: List<X>,
    private val onClickI: OnItemClickI? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var recBinding: Y

    var lastPosition = -1
    var context: Context? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = WeakReference(recyclerView.context).get()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        recBinding = bindingFactory(LayoutInflater.from(context))
        return BaseRecyclerViewHolderClickable(recBinding.root, onClickI)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        setAnimation(holder.itemView, position)
        if (recBinding is GridOnewBinding) {
            val item = list[position] as ResponseData
            val binding = recBinding as GridOnewBinding
            binding.data = item

            when (item.file) {
                null -> {
                    Picasso.get().load(item.picture).placeholder(R.drawable.placeholder)
                        .into(binding.img)
                }
                else -> Picasso.get().load(item.file!!).placeholder(R.drawable.placeholder)
                    .into(binding.img)
            }
        }
    }


    fun update(newList: List<X>?) {
        if (newList == null)
            return
        val m = BaseDiffUtil(newList, list)
        logit("old=${list.size} new=${newList.size}")
        list = newList
        DiffUtil.calculateDiff(m).dispatchUpdatesTo(this)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.fade_in_1000)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun getItemCount(): Int = list.size

    interface OnItemClickI {
        fun click(pos: Int, v: View)
    }
}
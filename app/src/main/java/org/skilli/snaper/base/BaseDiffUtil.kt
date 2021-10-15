package org.skilli.snaper.base

import androidx.recyclerview.widget.DiffUtil

@Suppress("UNCHECKED_CAST")
class BaseDiffUtil<X, Y>(
    private val newList: List<X>,
    private val oldList: List<Y>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
        /*return when {
            newList.first() is ResponseData -> {
                val item = oldList[oldItemPosition] as ResponseData
                val item2 = newList[newItemPosition] as ResponseData
                if (item.file == null)
                    item.picture == item2.picture
                else
                    item.file!! == item2.file!!
            }
            else -> newList[newItemPosition] == oldList[oldItemPosition]
        }*/
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
    }
}
package com.elenakozachenko.instaphoto

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class PhotosAdapter(
        private var listener: OnItemClickListener
) : RecyclerView.Adapter<PhotosAdapter.Holder>() {

    private var photos = mutableListOf<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false);
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val photo = photos[position]
        holder.bind(photo, position, listener)
    }

    fun setPhotos(data: MutableList<Photo>) {
        photos.clear()
        photos.addAll(data)
        notifyDataSetChanged()
    }

    fun addPhoto(photo: Photo) {
        photos.add(photo)
        notifyItemInserted(photos.size - 1)
        notifyItemChanged(photos.size - 1)
    }

    fun updatePhoto(photo: Photo, position: Int) {
        photos[position] = photo
        notifyItemChanged(position)
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.iv_photo)
        val author = view.findViewById<TextView>(R.id.tv_author)
        val count = view.findViewById<TextView>(R.id.tv_count)
        val users = view.findViewById<TextView>(R.id.tv_users)
        var like = view.findViewById<Button>(R.id.b_like)

        fun bind(photo: Photo, position: Int, listener: OnItemClickListener) {
            author.text = photo.author
            count.text = Integer.toString(photo.likes.size)
            var idDrawable = R.drawable.ic_like
            if (photo.likes.size != 0) {
                val likes = photo.likes.toString()
                users.text = likes
                if (likes.contains(NICKNAME)) {
                    idDrawable = R.drawable.ic_dislike
                }
            } else {
                users.setText(R.string.empty_like)
            }
            like.setCompoundDrawablesWithIntrinsicBounds(idDrawable, 0, 0, 0)
            Glide.with(itemView)
                    .load(photo.image)
                    .into(image)
            like.setOnClickListener {
                listener.likePhoto(photo.id, position)
            }
        }
    }

    interface OnItemClickListener {
        fun likePhoto(id: Long, position: Int)
    }
}

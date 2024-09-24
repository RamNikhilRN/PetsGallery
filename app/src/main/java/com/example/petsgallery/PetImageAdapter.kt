package com.example.petsgallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class PetImageAdapter(
    private var imageList: List<PetImage>
) : RecyclerView.Adapter<PetImageAdapter.ViewHolder>() {

    var onSaveImage: ((PetImage) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.petImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val creationDateTextView: TextView = itemView.findViewById(R.id.creationDateTextView)
        private val saveButton: Button = itemView.findViewById(R.id.saveImageButton)

        init {
            saveButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedImage = imageList[position]
                    onSaveImage?.invoke(selectedImage)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petImage = imageList[position]
        holder.titleTextView.text = petImage.title
        holder.descriptionTextView.text = petImage.description
        holder.creationDateTextView.text = holder.itemView.context.getString(R.string.created_on, petImage.created)

        // Loading image using Coil
        holder.imageView.load(petImage.url) {
            crossfade(true)
            placeholder(android.R.color.darker_gray)
            error(android.R.drawable.ic_dialog_alert)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun updateData(newImages: List<PetImage>) {
        imageList = newImages
        notifyDataSetChanged()
    }
}

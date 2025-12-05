package com.example.viecampus.ui.gpa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.viecampus.R

class GpaAdapter(
    private val onCourseClick: (GpaCourse) -> Unit,
    private val onCourseLongClick: (GpaCourse) -> Unit
) : ListAdapter<GpaCourse, GpaAdapter.GpaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GpaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gpa_course, parent, false)
        return GpaViewHolder(view, onCourseClick, onCourseLongClick)
    }

    override fun onBindViewHolder(holder: GpaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GpaViewHolder(
        itemView: View,
        private val onCourseClick: (GpaCourse) -> Unit,
        private val onCourseLongClick: (GpaCourse) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.courseName)
        private val info: TextView = itemView.findViewById(R.id.courseInfo)

        fun bind(course: GpaCourse) {
            name.text = course.name
            info.text = itemView.context.getString(
                R.string.gpa_course_info_format,
                course.score,
                course.credits
            )
            itemView.setOnClickListener { onCourseClick(course) }
            itemView.setOnLongClickListener {
                onCourseLongClick(course)
                true
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<GpaCourse>() {
        override fun areItemsTheSame(oldItem: GpaCourse, newItem: GpaCourse): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GpaCourse, newItem: GpaCourse): Boolean =
            oldItem == newItem
    }
}

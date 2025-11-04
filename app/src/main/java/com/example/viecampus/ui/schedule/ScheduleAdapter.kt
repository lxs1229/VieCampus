package com.example.viecampus.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.viecampus.R
import com.example.viecampus.data.entity.CourseEntity

class ScheduleAdapter(
    private val onCourseClick: (CourseEntity) -> Unit,
    private val onCourseLongClick: (CourseEntity) -> Unit
) : ListAdapter<CourseEntity, ScheduleAdapter.CourseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view, onCourseClick, onCourseLongClick)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CourseViewHolder(
        itemView: View,
        private val onCourseClick: (CourseEntity) -> Unit,
        private val onCourseLongClick: (CourseEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.courseName)
        private val time: TextView = itemView.findViewById(R.id.courseTime)
        private val location: TextView = itemView.findViewById(R.id.courseLocation)
        private val instructor: TextView = itemView.findViewById(R.id.courseInstructor)
        private val notes: TextView = itemView.findViewById(R.id.courseNotes)

        fun bind(course: CourseEntity) {
            name.text = course.name
            time.text = itemView.context.getString(
                R.string.course_time_format,
                dayOfWeekLabel(course.dayOfWeek),
                formatTime(course.startHour, course.startMinute),
                formatTime(course.endHour, course.endMinute)
            )
            location.text = course.location
            instructor.visibility = if (course.instructor.isNullOrBlank()) View.GONE else View.VISIBLE
            instructor.text = course.instructor
            notes.visibility = if (course.notes.isNullOrBlank()) View.GONE else View.VISIBLE
            notes.text = course.notes

            itemView.setOnClickListener { onCourseClick(course) }
            itemView.setOnLongClickListener {
                onCourseLongClick(course)
                true
            }
        }

        private fun dayOfWeekLabel(day: Int): String {
            val res = itemView.resources
            return when (day) {
                1 -> res.getString(R.string.day_monday)
                2 -> res.getString(R.string.day_tuesday)
                3 -> res.getString(R.string.day_wednesday)
                4 -> res.getString(R.string.day_thursday)
                5 -> res.getString(R.string.day_friday)
                6 -> res.getString(R.string.day_saturday)
                7 -> res.getString(R.string.day_sunday)
                else -> day.toString()
            }
        }

        private fun formatTime(hour: Int, minute: Int): String =
            String.format("%02d:%02d", hour, minute)
    }

    private object DiffCallback : DiffUtil.ItemCallback<CourseEntity>() {
        override fun areItemsTheSame(oldItem: CourseEntity, newItem: CourseEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CourseEntity, newItem: CourseEntity): Boolean =
            oldItem == newItem
    }
}

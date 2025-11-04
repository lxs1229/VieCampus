package com.example.viecampus.ui.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.viecampus.R
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasksAdapter(
    private val onTaskClick: (TaskEntity) -> Unit,
    private val onTaskLongClick: (TaskEntity) -> Unit,
    private val onStatusAction: (TaskEntity, TaskStatus) -> Unit
) : ListAdapter<TaskEntity, TasksAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, onTaskClick, onTaskLongClick, onStatusAction)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        itemView: View,
        private val onTaskClick: (TaskEntity) -> Unit,
        private val onTaskLongClick: (TaskEntity) -> Unit,
        private val onStatusAction: (TaskEntity, TaskStatus) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.taskTitle)
        private val meta: TextView = itemView.findViewById(R.id.taskMeta)
        private val description: TextView = itemView.findViewById(R.id.taskDescription)
        private val status: TextView = itemView.findViewById(R.id.taskStatus)
        private val statusAction: MaterialButton = itemView.findViewById(R.id.taskStatusAction)

        private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(task: TaskEntity) {
            title.text = task.title
            meta.text = buildMeta(task)
            description.text = task.description
            description.visibility = if (task.description.isNullOrBlank()) View.GONE else View.VISIBLE
            status.text = itemView.resources.getString(
                R.string.task_status_display,
                statusLabel(task.status)
            )
            val nextStatus = nextStatus(task.status)
            statusAction.text = itemView.resources.getString(nextStatusLabel(nextStatus))
            statusAction.setOnClickListener { onStatusAction(task, nextStatus) }

            itemView.setOnClickListener { onTaskClick(task) }
            itemView.setOnLongClickListener {
                onTaskLongClick(task)
                true
            }
        }

        private fun buildMeta(task: TaskEntity): String {
            val typeLabel = when (task.type) {
                TaskType.TODO -> itemView.context.getString(R.string.task_type_todo)
                TaskType.ASSIGNMENT -> itemView.context.getString(R.string.task_type_assignment)
                TaskType.EXAM -> itemView.context.getString(R.string.task_type_exam)
            }
            val dueText = task.dueAt?.let {
                val date = Date(it)
                itemView.context.getString(
                    R.string.task_due_format,
                    formatter.format(date)
                )
            }
            return if (dueText != null) {
                "$typeLabel · $dueText"
            } else {
                typeLabel
            }
        }

        private fun statusLabel(status: TaskStatus): String = when (status) {
            TaskStatus.PENDING -> itemView.context.getString(R.string.task_status_pending)
            TaskStatus.IN_PROGRESS -> itemView.context.getString(R.string.task_status_in_progress)
            TaskStatus.DONE -> itemView.context.getString(R.string.task_status_done)
        }

        private fun nextStatus(status: TaskStatus): TaskStatus = when (status) {
            TaskStatus.PENDING -> TaskStatus.IN_PROGRESS
            TaskStatus.IN_PROGRESS -> TaskStatus.DONE
            TaskStatus.DONE -> TaskStatus.PENDING
        }

        private fun nextStatusLabel(status: TaskStatus): Int = when (status) {
            TaskStatus.PENDING -> R.string.task_mark_pending
            TaskStatus.IN_PROGRESS -> R.string.task_mark_in_progress
            TaskStatus.DONE -> R.string.task_mark_done
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean =
            oldItem == newItem
    }
}

package com.example.viecampus.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.viecampus.R
import com.example.viecampus.VieCampusApp
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.databinding.DialogTaskBinding
import com.example.viecampus.databinding.FragmentTasksBinding
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import com.example.viecampus.reminders.ReminderScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasksViewModel by viewModels {
        val app = requireActivity().application as VieCampusApp
        TasksViewModelFactory(app.repository, ReminderScheduler(requireContext().applicationContext))
    }

    private lateinit var adapter: TasksAdapter

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TasksAdapter(
            onTaskClick = { task -> showTaskDialog(task) },
            onTaskLongClick = { task -> confirmDeleteTask(task) },
            onStatusAction = { task, nextStatus ->
                viewModel.updateStatus(task.id, nextStatus)
            }
        )

        binding.taskList.layoutManager = LinearLayoutManager(requireContext())
        binding.taskList.adapter = adapter

        binding.addTaskFab.setOnClickListener { showTaskDialog(null) }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.emptyView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun confirmDeleteTask(task: TaskEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(task.title)
            .setMessage(R.string.confirm_delete_task)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteTask(task)
            }
            .show()
    }

    private fun showTaskDialog(existing: TaskEntity?) {
        val dialogBinding = DialogTaskBinding.inflate(layoutInflater)
        val context = dialogBinding.root.context

        val typeAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.task_type_options,
            android.R.layout.simple_list_item_1
        )
        dialogBinding.taskTypeInput.setAdapter(typeAdapter)

        existing?.let { task ->
            dialogBinding.taskTitleInput.setText(task.title)
            dialogBinding.taskDescriptionInput.setText(task.description.orEmpty())
            dialogBinding.taskTypeInput.setText(mapTaskTypeToLabel(task.type), false)
            task.dueAt?.let {
                val dateTime = java.time.Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                dialogBinding.taskDueDateInput.setText(dateTime.toLocalDate().format(dateFormatter))
                dialogBinding.taskDueTimeInput.setText(dateTime.toLocalTime().format(timeFormatter))
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.add_task else R.string.edit_task)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .apply {
                if (existing != null) {
                    setNeutralButton(R.string.action_delete) { _, _ ->
                        viewModel.deleteTask(existing)
                    }
                }
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val task = parseTaskInput(existing?.id ?: 0L, existing?.status ?: TaskStatus.PENDING, dialogBinding)
                if (task != null) {
                    viewModel.saveTask(task)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun parseTaskInput(
        id: Long,
        currentStatus: TaskStatus,
        binding: DialogTaskBinding
    ): TaskEntity? {
        val title = binding.taskTitleInput.text?.toString()?.trim().orEmpty()
        val description = binding.taskDescriptionInput.text?.toString()?.trim()
        val typeLabel = binding.taskTypeInput.text?.toString()?.trim().orEmpty()
        val dueDate = binding.taskDueDateInput.text?.toString()?.trim().orEmpty()
        val dueTime = binding.taskDueTimeInput.text?.toString()?.trim().orEmpty()

        if (title.isBlank() || typeLabel.isBlank()) {
            Toast.makeText(requireContext(), R.string.dialog_missing_required, Toast.LENGTH_SHORT).show()
            return null
        }

        val type = mapLabelToTaskType(typeLabel) ?: TaskType.TODO
        val dueMillis = when {
            dueDate.isBlank() && dueTime.isBlank() -> null
            dueDate.isNotBlank() && dueTime.isNotBlank() -> parseDueDateTime(dueDate, dueTime)
            else -> {
                Toast.makeText(requireContext(), R.string.dialog_invalid_date, Toast.LENGTH_SHORT).show()
                return null
            }
        }
        if (dueDate.isNotBlank() && dueTime.isNotBlank() && dueMillis == null) {
            return null
        }

        return TaskEntity(
            id = id,
            title = title,
            description = description?.takeIf { it.isNotBlank() },
            type = type,
            status = if (id == 0L) TaskStatus.PENDING else currentStatus,
            dueAt = dueMillis,
            courseId = null
        )
    }

    private fun parseDueDateTime(date: String, time: String): Long? {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            val localTime = LocalTime.parse(time, timeFormatter)
            val dateTime = LocalDateTime.of(localDate, localTime)
            dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (ex: DateTimeParseException) {
            Toast.makeText(requireContext(), R.string.dialog_invalid_date, Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun mapLabelToTaskType(label: String): TaskType? {
        val res = resources
        return when (label) {
            res.getString(R.string.task_type_todo) -> TaskType.TODO
            res.getString(R.string.task_type_assignment) -> TaskType.ASSIGNMENT
            res.getString(R.string.task_type_exam) -> TaskType.EXAM
            else -> null
        }
    }

    private fun mapTaskTypeToLabel(type: TaskType): String {
        val res = resources
        return when (type) {
            TaskType.TODO -> res.getString(R.string.task_type_todo)
            TaskType.ASSIGNMENT -> res.getString(R.string.task_type_assignment)
            TaskType.EXAM -> res.getString(R.string.task_type_exam)
        }
    }
}

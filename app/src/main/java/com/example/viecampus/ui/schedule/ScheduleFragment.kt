package com.example.viecampus.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viecampus.R
import com.example.viecampus.VieCampusApp
import com.example.viecampus.data.entity.CourseEntity
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.databinding.DialogAddCourseBinding
import com.example.viecampus.databinding.FragmentScheduleBinding
import com.example.viecampus.databinding.ViewHomeTaskCardBinding
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import com.example.viecampus.ui.gpa.GpaViewModel
import com.example.viecampus.ui.gpa.GpaViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM", Locale.FRENCH)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val viewModel: ScheduleHomeViewModel by viewModels {
        val repo = (requireActivity().application as VieCampusApp).repository
        ScheduleHomeViewModelFactory(repo)
    }
    private val gpaViewModel: GpaViewModel by activityViewModels {
        val repo = (requireActivity().application as VieCampusApp).repository
        GpaViewModelFactory(repo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderHeader()

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            renderTasks(tasks)
        }

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            renderCourses(courses)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                gpaViewModel.gpa.collect { value ->
                    renderGpa(value)
                }
            }
        }

        binding.addCourseFab.setOnClickListener { showAddCourseDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderHeader() {
        val today = LocalDate.now().format(dateFormatter)
        binding.dateChip.text = today.replaceFirstChar { it.titlecase(Locale.FRENCH) }
    }

    private fun renderTasks(tasks: List<TaskEntity>) {
        val sorted = tasks.sortedWith(
            compareBy<TaskEntity> { it.dueAt ?: Long.MAX_VALUE }.thenBy { it.id }
        )
        val cards = listOf(binding.taskCard1, binding.taskCard2, binding.taskCard3)
        cards.forEachIndexed { index, cardBinding ->
            val task = sorted.getOrNull(index)
            if (task == null) {
                cardBinding.root.visibility = View.GONE
            } else {
                cardBinding.root.visibility = View.VISIBLE
                bindTaskCard(cardBinding, task)
            }
        }
    }

    private fun renderCourses(courses: List<CourseEntity>) {
        binding.courseChipGroup.removeAllViews()
        if (courses.isEmpty()) {
            val chip = Chip(requireContext()).apply {
                text = getString(R.string.schedule_empty_state)
                isCheckable = false
            }
            binding.courseChipGroup.addView(chip)
            return
        }
        courses.take(8).forEach { course ->
            val chip = Chip(requireContext()).apply {
                text = formatCourseLabel(course)
                isCheckable = false
            }
            binding.courseChipGroup.addView(chip)
        }
    }

    private fun renderGpa(value: Double) {
        val clamped = value.coerceIn(0.0, 20.0)
        val progress = ((clamped / 20.0) * 100).toInt()
        binding.gpaTitle.text = getString(R.string.gpa_current_value, clamped)
        binding.gpaProgress.setProgressCompat(progress, false)
    }

    private fun bindTaskCard(cardBinding: ViewHomeTaskCardBinding, task: TaskEntity) {
        cardBinding.taskCheck.isChecked = task.status == TaskStatus.DONE
        cardBinding.taskTitle.text = task.title
        cardBinding.taskSubtitle.text = buildSubtitle(task)
    }

    private fun buildSubtitle(task: TaskEntity): String {
        val typeLabel = mapTaskTypeToLabel(task.type)
        val dueAt = task.dueAt ?: return typeLabel
        val zoned = Instant.ofEpochMilli(dueAt).atZone(ZoneId.systemDefault())
        val date = zoned.toLocalDate()
        val time = zoned.toLocalTime().format(timeFormatter)
        val today = LocalDate.now()
        val dateLabel = when (date) {
            today -> getString(R.string.task_due_format, getString(R.string.day_today))
            today.plusDays(1) -> getString(R.string.task_due_format, getString(R.string.day_tomorrow))
            else -> getString(R.string.task_due_format, date.format(dateFormatter))
        }
        return "$dateLabel · $time • $typeLabel"
    }

    private fun formatCourseLabel(course: CourseEntity): String {
        val dayLabel = mapDayLabelReverse(course.dayOfWeek)
        val start = String.format("%02d:%02d", course.startHour, course.startMinute)
        val end = String.format("%02d:%02d", course.endHour, course.endMinute)
        val place = course.location
        return "${course.name} · $dayLabel $start-$end • $place"
    }

    private fun mapTaskTypeToLabel(type: TaskType): String {
        val res = resources
        return when (type) {
            TaskType.TODO -> res.getString(R.string.task_type_todo)
            TaskType.ASSIGNMENT -> res.getString(R.string.task_type_assignment)
            TaskType.EXAM -> res.getString(R.string.task_type_exam)
        }
    }

    private fun showAddCourseDialog() {
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)
        val dayAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.course_days,
            android.R.layout.simple_list_item_1
        )
        dialogBinding.courseDayInput.setAdapter(dayAdapter)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val course = parseCourseInput(dialogBinding) ?: return@setOnClickListener
                viewModel.saveCourse(course)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun parseCourseInput(binding: DialogAddCourseBinding): CourseEntity? {
        val name = binding.courseNameInput.text?.toString()?.trim().orEmpty()
        val location = binding.courseLocationInput.text?.toString()?.trim().orEmpty()
        val instructor = binding.courseInstructorInput.text?.toString()?.trim()
        val notes = binding.courseNotesInput.text?.toString()?.trim()
        val dayLabel = binding.courseDayInput.text?.toString()?.trim().orEmpty()
        val start = binding.courseStartInput.text?.toString()?.trim().orEmpty()
        val end = binding.courseEndInput.text?.toString()?.trim().orEmpty()

        if (name.isBlank() || location.isBlank() || dayLabel.isBlank() || start.isBlank() || end.isBlank()) {
            Toast.makeText(requireContext(), R.string.dialog_missing_required, Toast.LENGTH_SHORT).show()
            return null
        }

        val day = mapDayLabel(dayLabel) ?: run {
            Toast.makeText(requireContext(), R.string.dialog_missing_required, Toast.LENGTH_SHORT).show()
            return null
        }
        val startPair = parseTime(start) ?: return null
        val endPair = parseTime(end) ?: return null

        return CourseEntity(
            id = 0L,
            name = name,
            location = location,
            instructor = instructor?.takeIf { it.isNotBlank() },
            dayOfWeek = day,
            startHour = startPair.first,
            startMinute = startPair.second,
            endHour = endPair.first,
            endMinute = endPair.second,
            notes = notes?.takeIf { it.isNotBlank() }
        )
    }

    private fun mapDayLabel(label: String): Int? {
        val res = resources
        return when (label) {
            res.getString(R.string.day_monday) -> 1
            res.getString(R.string.day_tuesday) -> 2
            res.getString(R.string.day_wednesday) -> 3
            res.getString(R.string.day_thursday) -> 4
            res.getString(R.string.day_friday) -> 5
            res.getString(R.string.day_saturday) -> 6
            res.getString(R.string.day_sunday) -> 7
            else -> null
        }
    }

    private fun mapDayLabelReverse(day: Int): String {
        val res = resources
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

    private fun parseTime(value: String): Pair<Int, Int>? {
        val parts = value.split(":")
        if (parts.size != 2) {
            Toast.makeText(requireContext(), R.string.dialog_invalid_time, Toast.LENGTH_SHORT).show()
            return null
        }
        return try {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            if (hour in 0..23 && minute in 0..59) hour to minute else null
        } catch (_: NumberFormatException) {
            null
        } ?: run {
            Toast.makeText(requireContext(), R.string.dialog_invalid_time, Toast.LENGTH_SHORT).show()
            null
        }
    }
}

package com.example.viecampus.ui.schedule

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
import com.example.viecampus.data.entity.CourseEntity
import com.example.viecampus.databinding.DialogCourseBinding
import com.example.viecampus.databinding.FragmentScheduleBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels {
        ScheduleViewModelFactory((requireActivity().application as VieCampusApp).repository)
    }

    private lateinit var adapter: ScheduleAdapter

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
        adapter = ScheduleAdapter(
            onCourseClick = { course -> showCourseDialog(course) },
            onCourseLongClick = { course -> confirmDeleteCourse(course) }
        )

        binding.courseList.layoutManager = LinearLayoutManager(requireContext())
        binding.courseList.adapter = adapter

        binding.addCourseFab.setOnClickListener { showCourseDialog(null) }

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            adapter.submitList(courses)
            binding.emptyView.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun confirmDeleteCourse(course: CourseEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(course.name)
            .setMessage(R.string.confirm_delete_course)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteCourse(course)
            }
            .show()
    }

    private fun showCourseDialog(existing: CourseEntity?) {
        val dialogBinding = DialogCourseBinding.inflate(layoutInflater)
        val context = dialogBinding.root.context

        val dayAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.course_days,
            android.R.layout.simple_list_item_1
        )
        dialogBinding.courseDayInput.setAdapter(dayAdapter)

        existing?.let { course ->
            dialogBinding.courseNameInput.setText(course.name)
            dialogBinding.courseLocationInput.setText(course.location)
            dialogBinding.courseInstructorInput.setText(course.instructor.orEmpty())
            dialogBinding.courseNotesInput.setText(course.notes.orEmpty())
            dialogBinding.courseDayInput.setText(dayAdapter.getItem(course.dayOfWeek - 1), false)
            dialogBinding.courseStartInput.setText(formatTime(course.startHour, course.startMinute))
            dialogBinding.courseEndInput.setText(formatTime(course.endHour, course.endMinute))
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.add_course else R.string.edit_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .apply {
                if (existing != null) {
                    setNeutralButton(R.string.action_delete) { _, _ ->
                        viewModel.deleteCourse(existing)
                    }
                }
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val course = parseCourseInput(existing?.id ?: 0L, dialogBinding)
                if (course != null) {
                    viewModel.saveCourse(course)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun parseCourseInput(id: Long, dialogBinding: DialogCourseBinding): CourseEntity? {
        val name = dialogBinding.courseNameInput.text?.toString()?.trim().orEmpty()
        val location = dialogBinding.courseLocationInput.text?.toString()?.trim().orEmpty()
        val instructor = dialogBinding.courseInstructorInput.text?.toString()?.trim()
        val notes = dialogBinding.courseNotesInput.text?.toString()?.trim()
        val dayLabel = dialogBinding.courseDayInput.text?.toString()?.trim().orEmpty()
        val start = dialogBinding.courseStartInput.text?.toString()?.trim().orEmpty()
        val end = dialogBinding.courseEndInput.text?.toString()?.trim().orEmpty()

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
            id = id,
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
        } catch (ex: NumberFormatException) {
            null
        } ?: run {
            Toast.makeText(requireContext(), R.string.dialog_invalid_time, Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun mapDayLabel(label: String): Int? {
        val resources = resources
        return when (label) {
            resources.getString(R.string.day_monday) -> 1
            resources.getString(R.string.day_tuesday) -> 2
            resources.getString(R.string.day_wednesday) -> 3
            resources.getString(R.string.day_thursday) -> 4
            resources.getString(R.string.day_friday) -> 5
            resources.getString(R.string.day_saturday) -> 6
            resources.getString(R.string.day_sunday) -> 7
            else -> null
        }
    }

    private fun formatTime(hour: Int, minute: Int): String =
        String.format("%02d:%02d", hour, minute)
}

package com.example.viecampus.ui.gpa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.viecampus.R
import com.example.viecampus.VieCampusApp
import com.example.viecampus.databinding.DialogGpaCourseBinding
import com.example.viecampus.databinding.FragmentGpaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class GpaFragment : Fragment() {

    private var _binding: FragmentGpaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GpaViewModel by activityViewModels {
        val app = requireActivity().application as VieCampusApp
        GpaViewModelFactory(app.repository)
    }

    private lateinit var adapter: GpaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGpaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GpaAdapter(
            onCourseClick = { showCourseDialog(it) },
            onCourseLongClick = { confirmDeleteCourse(it) }
        )

        binding.gpaList.layoutManager = LinearLayoutManager(requireContext())
        binding.gpaList.adapter = adapter

        binding.addGpaFab.setOnClickListener { showCourseDialog(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.courses.collect { courses ->
                        adapter.submitList(courses)
                        binding.emptyView.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.gpa.collect { value ->
                        binding.gpaValue.text = getString(R.string.gpa_current_value, value)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun confirmDeleteCourse(course: GpaCourse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(course.name)
            .setMessage(R.string.confirm_delete_gpa_course)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteCourse(course.id)
            }
            .show()
    }

    private fun showCourseDialog(existing: GpaCourse?) {
        val dialogBinding = DialogGpaCourseBinding.inflate(layoutInflater)
        existing?.let { course ->
            dialogBinding.gpaCourseNameInput.setText(course.name)
            dialogBinding.gpaScoreInput.setText(course.score.toString())
            dialogBinding.gpaCreditsInput.setText(course.credits.toString())
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.gpa_add_course else R.string.gpa_edit_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .apply {
                if (existing != null) {
                    setNeutralButton(R.string.action_delete) { _, _ ->
                        viewModel.deleteCourse(existing.id)
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

    private fun parseCourseInput(id: Long, binding: DialogGpaCourseBinding): GpaCourse? {
        val name = binding.gpaCourseNameInput.text?.toString()?.trim().orEmpty()
        val scoreText = binding.gpaScoreInput.text?.toString()?.trim().orEmpty()
        val creditText = binding.gpaCreditsInput.text?.toString()?.trim().orEmpty()

        if (name.isBlank() || scoreText.isBlank() || creditText.isBlank()) {
            Toast.makeText(requireContext(), R.string.dialog_missing_required, Toast.LENGTH_SHORT).show()
            return null
        }

        val score = scoreText.toDoubleOrNull()
        val credits = creditText.toDoubleOrNull()
        if (score == null || credits == null || score !in 0.0..100.0 || credits <= 0.0) {
            Toast.makeText(requireContext(), R.string.dialog_invalid_score_or_credit, Toast.LENGTH_SHORT).show()
            return null
        }

        return GpaCourse(
            id = id,
            name = name,
            score = score,
            credits = credits
        )
    }
}

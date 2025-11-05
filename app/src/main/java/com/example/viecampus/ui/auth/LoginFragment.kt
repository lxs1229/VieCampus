package com.example.viecampus.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.viecampus.R
import com.example.viecampus.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                findNavController().navigate(R.id.action_loginFragment_to_scheduleFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateInputs(): Boolean {
        val email = binding.loginEmailInput.text?.toString()?.trim().orEmpty()
        val password = binding.loginPasswordInput.text?.toString()?.trim().orEmpty()

        var valid = true

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginEmailLayout.error = getString(R.string.login_error_email)
            valid = false
        } else {
            binding.loginEmailLayout.error = null
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.loginPasswordLayout.error = getString(R.string.login_error_password, MIN_PASSWORD_LENGTH)
            valid = false
        } else {
            binding.loginPasswordLayout.error = null
        }

        return valid
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}

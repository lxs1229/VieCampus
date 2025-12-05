package com.example.viecampus.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.viecampus.R
import com.example.viecampus.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var hasNavigatedAfterCas = false
    private var pendingUsername: String = ""
    private var pendingPassword: String = ""

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

        configureCasWebView()

        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                binding.loginHint.text = getString(R.string.login_cas_loading)
                startCasLogin()
            }
        }
    }

    override fun onDestroyView() {
        binding.casWebView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroyView()
        _binding = null
    }

    private fun validateInputs(): Boolean {
        val username = binding.loginUsernameInput.text?.toString()?.trim().orEmpty()
        val password = binding.loginPasswordInput.text?.toString()?.trim().orEmpty()

        var valid = true

        if (username.isBlank()) {
            binding.loginUsernameLayout.error = getString(R.string.login_error_username)
            valid = false
        } else {
            binding.loginUsernameLayout.error = null
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.loginPasswordLayout.error = getString(R.string.login_error_password, MIN_PASSWORD_LENGTH)
            valid = false
        } else {
            binding.loginPasswordLayout.error = null
        }

        return valid
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureCasWebView() {
        binding.casWebView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val currentUrl = url.orEmpty()
                    if (currentUrl.contains("cas/login")) {
                        binding.loginButton.isEnabled = true
                        autoFillCasForm()
                    }
                    if (!hasNavigatedAfterCas && currentUrl.startsWith(CAS_SERVICE_SUCCESS)) {
                        hasNavigatedAfterCas = true
                        findNavController().navigate(R.id.action_loginFragment_to_scheduleFragment)
                    }
                }
            }
        }
    }

    private fun startCasLogin() {
        hasNavigatedAfterCas = false
        pendingUsername = binding.loginUsernameInput.text?.toString()?.trim().orEmpty()
        pendingPassword = binding.loginPasswordInput.text?.toString()?.trim().orEmpty()
        binding.loginButton.isEnabled = false
        binding.casWebView.visibility = View.VISIBLE
        binding.casWebView.loadUrl(CAS_LOGIN_URL)
    }

    private fun autoFillCasForm() {
        if (pendingUsername.isBlank() || pendingPassword.isBlank()) return

        // Autofill and submit the CAS form to avoid retyping credentials.
        val jsUsername = pendingUsername.escapeForJavascript()
        val jsPassword = pendingPassword.escapeForJavascript()
        val js = """
            (function() {
                var userField = document.getElementById('username');
                var passField = document.getElementById('password');
                if (!userField || !passField) { return; }
                userField.value = '$jsUsername';
                passField.value = '$jsPassword';
                var form = userField.form || document.querySelector('form');
                if (form) { HTMLFormElement.prototype.submit.call(form); }
            })();
        """.trimIndent()
        binding.casWebView.evaluateJavascript(js, null)
    }

    private fun String.escapeForJavascript(): String =
        this.replace("\\", "\\\\").replace("'", "\\'")

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val CAS_LOGIN_URL =
            "https://cas.univ-paris8.fr/cas/login?service=https://e-p8.univ-paris8.fr/uPortal/Login"
        private const val CAS_SERVICE_SUCCESS = "https://e-p8.univ-paris8.fr/uPortal/Login"
    }
}

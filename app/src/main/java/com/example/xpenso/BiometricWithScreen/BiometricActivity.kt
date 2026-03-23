package com.example.xpenso.BiometricWithScreen
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/* ---------------------------------------------------------
   BIOMETRIC ACTIVITY
   A dedicated FragmentActivity to handle biometric prompts.
   This allows MainActivity to remain a ComponentActivity.
---------------------------------------------------------- */

class BiometricActivity : FragmentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get custom strings from intent if available
        val title = intent.getStringExtra("title") ?: "Biometric Authentication"
        val subtitle = intent.getStringExtra("subtitle") ?: "Confirm your identity"

        executor = ContextCompat.getMainExecutor(this)

        val biometricManager = BiometricManager.from(this)

        // Define authenticators: Strong Biometrics or Device Credentials (PIN/Pattern)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { /* Proceed */ }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "No biometric hardware found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware is currently unavailable", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No fingerprint or lock screen PIN enrolled", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            else -> {
                Toast.makeText(this, "Biometric security is not supported on this device", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }

        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    setResult(RESULT_OK)
                    Toast.makeText(this@BiometricActivity, "Authenticated", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Result canceled or error
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@BiometricActivity, errString, Toast.LENGTH_SHORT).show()

                    }
                    finish()
                }

                override fun onAuthenticationFailed() {
                    // This is called for unrecognized prints, the system handles the UI.
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators) // Allows fallback to PIN
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

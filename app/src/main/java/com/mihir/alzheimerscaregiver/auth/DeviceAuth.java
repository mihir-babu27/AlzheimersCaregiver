package com.mihir.alzheimerscaregiver.auth;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class DeviceAuth {

    public interface Callback {
        void onSuccess();
        void onFailure();
    }

    public static void prompt(@NonNull AppCompatActivity activity, @NonNull Callback callback) {
        BiometricManager manager = BiometricManager.from(activity);
        int allowed = manager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK);
        if (allowed == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE || allowed == BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
            // No auth available; proceed (optional: fail instead)
            callback.onSuccess();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock App")
                .setSubtitle("Use device PIN/Pattern/Password or biometric")
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build();

        BiometricPrompt prompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onFailure();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        prompt.authenticate(promptInfo);
    }
}



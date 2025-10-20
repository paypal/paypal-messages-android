package com.paypal.messages.manifesttest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal test activity for manifest merger verification.
 * This activity is never actually run - it exists only to ensure
 * the manifest merger succeeds during build.
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

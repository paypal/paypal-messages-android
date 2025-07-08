package com.paypal.messagesdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.paypal.messagesdemo.databinding.ActivityBasicMessageBinding

class BasicXmlActivity : AppCompatActivity() {
	private lateinit var binding: ActivityBasicMessageBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBasicMessageBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}
}

package com.paypal.messagesdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.paypal.messagesdemo.databinding.ActivityDemoBinding
import com.paypal.messagesdemo.ui.main.SectionsPagerAdapter

class DemoActivity : AppCompatActivity() {
	private lateinit var binding: ActivityDemoBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityDemoBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
		val viewPager: ViewPager = binding.viewPager
		viewPager.adapter = sectionsPagerAdapter
		val tabs: TabLayout = binding.tabs
		tabs.setupWithViewPager(viewPager)
	}
}

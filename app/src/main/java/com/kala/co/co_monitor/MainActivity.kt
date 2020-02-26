package com.kala.co.co_monitor

import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.kala.co.co_monitor.ui.main.ParametersFragment
import com.kala.co.co_monitor.ui.main.SectionsPagerAdapter
import com.kala.co.co_monitor.ui.main.ValuesFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        setupViewPager(viewPager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        startService(Intent(this, CoNotificationService::class.java))
    }

    private fun setupViewPager(viewPager: ViewPager) {
        var sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        sectionsPagerAdapter.addFragment(ValuesFragment.newInstance(), "Wartości")
        sectionsPagerAdapter.addFragment(ParametersFragment.newInstance(), "Parametry")
        viewPager.adapter = sectionsPagerAdapter
    }
}
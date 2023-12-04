package com.example.isable_capstone.ui.onBoarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnBoardingPagerAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle):FragmentStateAdapter(fragmentManager,lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0->OnBoarding_Fragment_1()
            1->OnBoarding_Fragment_2()
            2->OnBoarding_Fragment_3()
            else -> throw IllegalArgumentException("Invalid Position")
        }
    }
}
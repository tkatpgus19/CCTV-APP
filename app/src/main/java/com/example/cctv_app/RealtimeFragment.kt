package com.example.cctv_app

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.cctv_app.databinding.FragmentRealtimeBinding

class RealtimeFragment : Fragment() {
    private lateinit var binding: FragmentRealtimeBinding
    private var saveList = ArrayList<Int>()
    private lateinit var drawMobile: DrawMobile
    private lateinit var drawTablet: DrawTablet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_realtime, container, false)

        val view = binding.root
        val isTablet = arguments?.getBoolean("isTablet")
        val activatedCamList = arguments?.getIntegerArrayList("activatedCamList")

        if(isTablet == null) {
            if(activatedCamList != null){
                for(n in activatedCamList)
                    saveList.add(n)
                drawMobile = DrawMobile(binding, requireActivity(), requireContext())
                drawMobile.setLayoutParams()
                drawMobile.setClickEvent(saveList)
            }
            if(isTabletDevice(requireContext())) {
                drawTablet = DrawTablet(binding, requireActivity(), requireContext())
                drawTablet.setLayoutParams()
                drawTablet.setClickEvent()
            }
            drawMobile = DrawMobile(binding, requireActivity(), requireContext())
            drawMobile.setLayoutParams()
            drawMobile.setClickEvent(saveList)
        }
        else{
            if(isTablet){
                drawTablet = DrawTablet(binding, requireActivity(), requireContext())
                drawTablet.setLayoutParams()
                drawTablet.setClickEvent()
            }
            else{
                if (activatedCamList != null) {
                    for (n in activatedCamList)
                        saveList.add(n)
                }
                drawMobile = DrawMobile(binding, requireActivity(), requireContext())
                drawMobile.setLayoutParams()
                drawMobile.setClickEvent(saveList)
            }
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        return view
    }
    // 태블릿 확인 함수
    private fun isTabletDevice(context: Context): Boolean{
        val xlarge = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == 4
        val large = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge or large
    }
}
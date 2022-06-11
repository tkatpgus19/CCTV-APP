package com.example.cctv_app

import android.animation.LayoutTransition
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.example.cctv_app.databinding.FragmentFullscreenBinding

class FullscreenFragment : Fragment() {
    private var frameList: List<CctvLayout> = arrayListOf()
    private lateinit var binding: FragmentFullscreenBinding
    private val layoutTransition = LayoutTransition()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fullscreen, container, false)

        val view = binding.root

        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.setDuration(200)

        frameList = (0..15).map { i ->
            val frame = CctvLayout(requireActivity())
            frame.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            frame
        }

        val cnt = arguments?.getInt("cnt")
        val activatedCamList = arguments?.getIntegerArrayList("activatedCamList")
        val warning = arguments?.getBoolean("isWarning")

        if(activatedCamList != null) {
            if (activatedCamList.any { it == cnt }) {
                frameList[cnt!!].setup(1)
                frameList[cnt].setLabel("${cnt}")
                if(warning!!)
                    frameList[cnt].warning(true)
            }
        }

        frameList[cnt!!].layoutTransition = layoutTransition

        binding.fullscreen.addView(frameList[cnt])

        frameList[cnt].setOnClickListener {
            val fragment = RealtimeFragment()
            val bundle = Bundle()
            bundle.putIntegerArrayList("activatedCamList", activatedCamList)
            fragment.arguments = bundle

            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.nav_fragment, fragment)
                .commit()
        }
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        return view
    }

}
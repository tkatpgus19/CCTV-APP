package com.example.cctv_app

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.cctv_app.databinding.FragmentRealtimeBinding

class RealtimeFragment : Fragment() {
    private lateinit var binding: FragmentRealtimeBinding
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

        /* FullscreenFragment 에서 왔는지 체크, 디폴트 값은 false,
           FullscreenFragment 에서 값을 지정해서 전달한걸 여기서 받아옴*/
        var fromFullscreenFragment = false

        /* 체크하는 조건변수가 null 이 아닌 경우에만 boolean 값 저장*/
        if(arguments?.getBoolean("fromFullscreenFragment") != null)
            fromFullscreenFragment = arguments?.getBoolean("fromFullscreenFragment")!!

        /* 처음 앱 실행 및 FullscreenFragment 에서 왔을 때 */
        if(isTablet == null) {
            /* FullscreenFragment 에서 왔을 때 */
            if(fromFullscreenFragment)
                setMobileLayout()

            /* 처음 앱을 실행했을 때 */
            else{
                /* 기기가 태블릿인지 아닌지 체크하고 각 기기에 맞는 레이아웃 생성 */
                if(isTabletDevice(requireContext()))
                    setTabletLayout()
                else
                    setMobileLayout()
            }
        }
        /* 앱 실행 도중 레이아웃을 변경했을 시 (from MainActivity) */
        else{
            /* '4x4' 메뉴 선택시 */
            if(isTablet)
                setTabletLayout()

            /* '2x2' 메뉴 선택시 */
            else
                setMobileLayout()
        }

        /* 가로모드 고정 해제 */
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        return view
    }

    /* 해당 기기가 태블릿인지 체크하는 함수 */
    private fun isTabletDevice(context: Context): Boolean{
        val xlarge = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == 4
        val large = context.resources
            .configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge or large
    }

    /* 2x2 레이아웃 생성 */
    private fun setMobileLayout(){
        drawMobile = DrawMobile(binding, requireActivity(), requireContext())
        drawMobile.setLayoutParams()
        drawMobile.setLongClickEvent()
        drawMobile.setClickEvent()
    }

    /* 4x4 레이아웃 생성 */
    private fun setTabletLayout(){
        drawTablet = DrawTablet(binding, requireActivity(), requireContext())
        drawTablet.setLayoutParams()
        drawTablet.setLongClickEvent()
        drawTablet.setClickEvent()
    }
}
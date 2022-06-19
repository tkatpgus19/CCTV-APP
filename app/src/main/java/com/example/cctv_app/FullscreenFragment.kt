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

        /* frame 생성 */
        frameList = (0..15).map { i ->
            val frame = CctvLayout(requireActivity())
            frame.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            frame
        }

        /* 이전 프래그먼트에서 몇 번 frame 이 확장된건지,
           활성화된 카메라 리스트 정보를 받아와 저장 */
        val extendedFrameNum = arguments?.getInt("extendedFrameNum")
        val activatedCamList = MyApplication.prefs.getCamInstance()

        /* 활성화된 카메라 정보 리스트가 있으면 해당 프레임 설정 */
        if(MyApplication.prefs.isCamListExist() != "no") {
            if (activatedCamList.any { it == extendedFrameNum }) {
                frameList[extendedFrameNum!!].setup(1)
                frameList[extendedFrameNum].setLabel("${extendedFrameNum}")
                frameList[extendedFrameNum].showVoiceBtn(true)
            }
        }

        frameList[extendedFrameNum!!].layoutTransition = layoutTransition
        binding.fullscreen.addView(frameList[extendedFrameNum])

        /* 전체화면 클릭리스너 등록 (RealtimeFragment 로 이동) */
        frameList[extendedFrameNum].setOnClickListener {
            val fragment = RealtimeFragment()
            val bundle = Bundle()

            // 이 프래그먼트에서 이동했다는 것을 증명하기 위해 변수 선언 및 전달
            val fromFsF = true
            bundle.putBoolean("fromFullscreenFragment", fromFsF)
            fragment.arguments = bundle

            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.nav_fragment, fragment)
                .commit()
        }

        // 가로모드로 고정
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        return view
    }

}
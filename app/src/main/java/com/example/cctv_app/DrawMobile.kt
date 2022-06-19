package com.example.cctv_app

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.example.cctv_app.databinding.FragmentRealtimeBinding

class DrawMobile(var binding: FragmentRealtimeBinding, var activity: FragmentActivity, var context: Context){

    private var frameList: List<CctvLayout> = arrayListOf()
    private var layoutParamsList: MutableList<GridLayout.LayoutParams?> = MutableList(16){null}
    private val layoutTransition = LayoutTransition()
    private var activatedCamList = ArrayList<Int>()

    init {
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.setDuration(200)
    }

    /* frame 생성, 초기화, addView */
    fun setLayoutParams(){
        /* 가로, 세로모드 각각 화면크기 체크 후 addView (비율 체크) */
        val sizeX = getSize().x
        var sizeY = getSize().y

        if(sizeY < sizeX)
            sizeY = sizeX

        frameList = (0..15).map { i ->
            val frame = CctvLayout(activity)
            val layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(i / 2 * 2, 2, 1.0f),
                GridLayout.spec(i % 2 * 2, 2, 1.0f)
            )
            layoutParams.width = 0
            layoutParams.height = 0
            frame.layoutParams = layoutParams

            frame.setPos(i % 2 * 2, i / 2 * 2)
            frame
        }

        val gridLayout = GridLayout(context)
        gridLayout.rowCount = 32
        gridLayout.columnCount = 4
        gridLayout.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        (0..15).map { i ->
            gridLayout.addView(frameList[i])
        }
        gridLayout.layoutTransition = layoutTransition

        val linearLayout = LinearLayout(context)

        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            sizeY * 2
        )
        linearLayout.addView(gridLayout)

        val subLayout = LinearLayout(context)
        subLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        subLayout.addView(linearLayout)

        val scrollView = ScrollView(context)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.addView(subLayout)
        binding.mainLayout.addView(scrollView)
    }

    /* 팝업 메뉴 띄우는 함수 */
    fun setLongClickEvent() {
        for (cnt in 0..15) {
            frameList[cnt].setOnLongClickListener {
                val popupMenu = PopupMenu(context, it)
                activity.menuInflater.inflate(R.menu.camera_menu, popupMenu.menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.addCam -> {
                            if (frameList[cnt].isSetup)
                                Toast.makeText(context, "이미 추가되어있습니다.", Toast.LENGTH_SHORT).show()
                            else {
                                frameList[cnt].setLabel("${cnt}")
                                frameList[cnt].setup(0)
                                
                                // 활성화 시 활성화된 카메라 정보 리스트에 추가
                                activatedCamList.add(cnt)
                                MyApplication.prefs.saveCamInstance(activatedCamList)
                            }
                            true
                        }
                        else -> {
                            if (frameList[cnt].isSetup) {
                                frameList[cnt].unset()
                                frameList[cnt].isSetup = false
                                
                                // 비활성화 시 활성화된 카메라 정보 리스트에서 제거
                                activatedCamList.remove(cnt)
                                MyApplication.prefs.saveCamInstance(activatedCamList)
                            } else
                                Toast.makeText(context, "이미 제거했습니다.", Toast.LENGTH_SHORT).show()
                            true
                        }
                    }
                }
                true
            }
        }

    }

    /* frame 의 확대 및 이동 함수 */
    fun setClickEvent(){
        /* 활성화된 카메라 정보 리스트가 있을 경우 활성화 */
        if(MyApplication.prefs.isCamListExist() != "no"){
            for(n in MyApplication.prefs.getCamInstance()) {
                frameList[n].setLabel("${n}")
                frameList[n].setup(0)
                activatedCamList.add(n)
            }
        }

        for(cnt in 0..15){
            frameList[cnt].setOnClickListener {

                /* 해당 frame 의 z 값이 0.0f가 아닐때만 클릭 리스너를 등록함.
                   즉, 특정 frame 확대 시에는 다른 frame 이 터치되지 않도록 클릭리스너를 등록하지 않음 */
                if (frameList[cnt].z != 0.0f) {

                    /**** 해당 frame 이 처음 터치될 때 (1차 확대) ****/
                    if (frameList[cnt].touchCnt == 0) {
                        layoutParamsList[cnt] = frameList[cnt].layoutParams as GridLayout.LayoutParams

                        var layoutParams = GridLayout.LayoutParams(
                            GridLayout.spec(cnt / 2 * 2, 4, 2.0f),
                            GridLayout.spec(0, 4, 2.0f)
                        )
                        layoutParams.width = 0
                        layoutParams.height = 0

                        frameList[cnt].z = 10.0f
                        frameList[cnt].layoutParams = layoutParams

                        /* 확대된 frame 을 제외한 나머지 frame 이동 */
                        /* 짝수번 frame 이동 */
                        if (cnt % 2 == 0) {
                            for (other in (0..15).filter { it != cnt }) {
                                if (other < cnt) {
                                    layoutParamsList[other] = frameList[other].layoutParams as GridLayout.LayoutParams
                                    continue
                                }
                                layoutParamsList[other] = frameList[other].layoutParams as GridLayout.LayoutParams

                                layoutParams = GridLayout.LayoutParams(
                                    GridLayout.spec(frameList[other].startY + 2 * (other % 2 + 1), 2, 1.0f),
                                    GridLayout.spec(frameList[other].startX + 2 - (other % 2 * 4), 2, 1.0f)
                                )
                                layoutParams.width = 0
                                layoutParams.height = 0
                                frameList[other].layoutParams = layoutParams
                            }
                        }
                        /* 홀수번 frame 이동 */
                        else {
                            for (other in (0..15).filter { it != cnt }) {
                                if (other < cnt - 1) {
                                    layoutParamsList[other] = frameList[other].layoutParams as GridLayout.LayoutParams
                                    continue
                                }
                                layoutParamsList[other] = frameList[other].layoutParams as GridLayout.LayoutParams

                                layoutParams = if (other == cnt - 1) {
                                    GridLayout.LayoutParams(
                                        GridLayout.spec(frameList[other].startY + 4, 2, 1.0f),
                                        GridLayout.spec(frameList[other].startX, 2, 1.0f)
                                    )
                                } else {
                                    GridLayout.LayoutParams(
                                        GridLayout.spec(frameList[other].startY + 2 * (other % 2 + 1), 2, 1.0f),
                                        GridLayout.spec(frameList[other].startX + 2 - (other % 2 * 4), 2, 1.0f)
                                    )
                                }
                                layoutParams.width = 0
                                layoutParams.height = 0
                                frameList[other].layoutParams = layoutParams
                            }
                        }
                        /* 확대된 frame 을 제외한 모든 frame z 값 0.0f로 설정 */
                        for (other in (0..15).filter { it != cnt }) {
                            frameList[other].z = 0.0f
                        }
                        frameList[cnt].touchCnt++
                    }

                    /**** 해당 frame 이 두번 터치될 때 (전체 화면으로 확대 -> FullscreenFragment 로 이동) ****/
                    else if(frameList[cnt].touchCnt == 1){
                        val bundle = Bundle()
                        bundle.putInt("extendedFrameNum", cnt)
                        val fragment = FullscreenFragment()
                        fragment.arguments = bundle
                        activity.supportFragmentManager
                            .beginTransaction()
                            .add(R.id.nav_fragment, fragment)
                            .commit()
                    }

                    /**** 해당 frame 이 세번 터치될 때 (다시 원상복귀) ****/
                    else {
                        frameList[cnt].layoutParams = layoutParamsList[cnt]
                        layoutParamsList[cnt] = null

                        for (other in (0..15).filter { it != cnt }) {
                            frameList[other].z = 5.0f
                            frameList[other].layoutParams = layoutParamsList[other]
                            layoutParamsList[other] = null
                        }
                        frameList[cnt].touchCnt = 0
                    }
                }
            }
        }
    }

    /* 화면크기 계산 함수 */
    private fun getSize(): Point {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size
    }
}
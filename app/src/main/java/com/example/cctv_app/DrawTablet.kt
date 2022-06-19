package com.example.cctv_app

import android.animation.LayoutTransition
import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.cctv_app.databinding.FragmentRealtimeBinding

class DrawTablet(var binding: FragmentRealtimeBinding, var activity: FragmentActivity, var context: Context){

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
        frameList = (0..15).map { i ->
            val goto = 0
            val frame = CctvLayout(activity)
            val layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(i / 4 * 2, 2, 1.0f),
                GridLayout.spec(i % 4 * 2, 2, 1.0f)
            )
            layoutParams.width = 0
            layoutParams.height = 0
            frame.layoutParams = layoutParams

            /* 안습적인 4개의 frame 그룹 지정....
               사실상 하드코딩, frame 확대시 이동할 위치 정보 설정 */
            if (i % 4 < 2) {
                /* 좌측 상단 그룹(0,1,4,5) */
                if (i / 4 < 2) {
                    frame.setPos(0,0)
                    frame.setMovedPos(4,2)
                    frame.goto = goto + 6
                }
                /* 좌측 하단 그룹(8,9,12,13) */
                else {
                    frame.setPos(0,4)
                    frame.setMovedPos(4,4)
                    frame.goto = goto + 10
                }
            }
            else {
                /* 우측 상단 그룹(2,3,6,7) */
                if (i / 4 < 2) {
                    frame.setPos(4,0)
                    frame.setMovedPos(2,2)
                    frame.goto = goto + 5
                }
                /* 우측 하단 그룹(10,11,14,15) */
                else {
                    frame.setPos(4,4)
                    frame.setMovedPos(2,4)
                    frame.goto = goto + 9
                }
            }
            frame
        }

        val gridLayout = GridLayout(context)
        gridLayout.rowCount = 8
        gridLayout.columnCount = 8
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
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayout.addView(gridLayout)
        binding.mainLayout.addView(linearLayout)
    }

    /* 팝업 메뉴 띄우는 함수 */
    fun setLongClickEvent(){
        for(cnt in 0..15){
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

        for(cnt in 0..15) {
            frameList[cnt].setOnClickListener {

                /* 해당 frame 의 z 값이 0.0f가 아닐때만 클릭 리스너 등록함.
                   즉, 특정 frame 확대 시에는 다른 frame 이 터치되지 않도록 클릭리스너를 등록하지 않음 */
                if (frameList[cnt].z != 0.0f){

                    /**** 해당 frame 이 처음 터치될 때 (1차 확대) ****/
                    if (frameList[cnt].touchCnt == 0) {

                        /* 확대될 주인공 frame 확대
                           z 값 10.0f, 맨 앞으로 변경하고 touchCount 증가 */
                        layoutParamsList[cnt] = frameList[cnt].layoutParams as GridLayout.LayoutParams

                        var layoutParams = GridLayout.LayoutParams(
                            GridLayout.spec(frameList[cnt].startY, 4, 2.0f),
                            GridLayout.spec(frameList[cnt].startX, 4, 2.0f)
                        )
                        layoutParams.width = 0
                        layoutParams.height = 0

                        frameList[cnt].z = 10.0f
                        frameList[cnt].layoutParams = layoutParams

                        frameList[cnt].touchCnt++


                        /* 눈물의 똥꼬쇼 시작ㅠㅠ
                           이동당하는 3개의 frame 과 4분할 될 frame 이동 및 z 값 0.0f 로 설정 */
                        var countGroupNum = 0
                        var plusX = 0
                        var plusY = 0

                        /* 확대되는 frame 과 같은 그룹에 속하는 3개의 frame 위치 이동 */
                        for (i in (0..15).filter { (frameList[it].goto == frameList[cnt].goto) && (it != cnt) }) {
                            layoutParamsList[i] = frameList[i].layoutParams as GridLayout.LayoutParams

                            if (countGroupNum == 1)
                                plusX++
                            if (countGroupNum == 2) {
                                plusY++
                                plusX--
                            }

                            layoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(frameList[i].movedY + plusY, 1, 0.5f),
                                GridLayout.spec(frameList[i].movedX + plusX, 1, 0.5f)
                            )
                            layoutParams.width = 0
                            layoutParams.height = 0
                            frameList[i].z = 0.0f

                            frameList[i].layoutParams = layoutParams

                            countGroupNum++
                        }
                        
                        /* 4분할 당할 frame 위치 이동 */
                        layoutParamsList[frameList[cnt].goto] = frameList[frameList[cnt].goto].layoutParams as GridLayout.LayoutParams
                        
                        layoutParams = GridLayout.LayoutParams(
                            GridLayout.spec(frameList[cnt].movedY + plusY, 1, 0.5f),
                            GridLayout.spec(frameList[cnt].movedX + ++plusX, 1, 0.5f)
                        )
                        layoutParams.width = 0
                        layoutParams.height = 0
                        frameList[frameList[cnt].goto].z = 0.0f

                        frameList[frameList[cnt].goto].layoutParams = layoutParams


                        /* 그 외의 다른 frame 들 모두 z 값 0.0f 로 변경*/
                        for (other in (0..15).filter { (it != cnt) && (frameList[it].goto != frameList[cnt].goto) && it!=frameList[cnt].goto }) {
                            layoutParamsList[other] =
                                frameList[other].layoutParams as GridLayout.LayoutParams
                            frameList[other].z = 0.0f
                        }
                    }

                    /**** 해당 frame 이 두번 터치될 때 (전체 화면으로 확대) ****/
                    else if (frameList[cnt].touchCnt == 1) {
                        /* 가로모드 고정 */
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                        val layoutParams = GridLayout.LayoutParams(
                            GridLayout.spec(0, 8, 1.0f),
                            GridLayout.spec(0, 8, 1.0f)
                        )
                        frameList[cnt].layoutParams = layoutParams

                        /* 활성화 된 카메라일 때 마이크 아이콘 활성화 */
                        if(frameList[cnt].isSetup)
                            frameList[cnt].showVoiceBtn(true)

                        frameList[cnt].touchCnt++
                    }

                    /**** 해당 frame 이 세번 터치될 때 (다시 원상복귀) ****/
                    else {
                        /* 가로모드 해제 */
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                        /* 모든 frame 을 저장해두었던 기존 좌표로 복귀, z값도 원상복귀, 마이크 아이콘 비활성화 */
                        frameList[cnt].layoutParams = layoutParamsList[cnt]
                        layoutParamsList[cnt] = null
                        frameList[cnt].z = 5.0f
                        frameList[cnt].touchCnt = 0
                        frameList[cnt].showVoiceBtn(false)
                        
                        for (other in (0..15).filter { it != cnt }) {
                            frameList[other].layoutParams = layoutParamsList[other]
                            layoutParamsList[other] = null
                            frameList[other].z = 5.0f
                        }
                    }
                }
            }
        }
    }
}
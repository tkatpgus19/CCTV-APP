package com.example.cctv_app

import android.animation.LayoutTransition
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
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

    init {
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.setDuration(200)
    }

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

            if (i % 4 < 2) {
                if (i / 4 < 2) {
                    frame.setPos(0,0)
                    frame.setMovedPos(4,2)
                    frame.goto = goto + 6
                }
                else {
                    frame.setPos(0,4)
                    frame.setMovedPos(4,4)
                    frame.goto = goto + 10
                }
            }
            else {
                if (i / 4 < 2) {
                    frame.setPos(4,0)
                    frame.setMovedPos(2,2)
                    frame.goto = goto + 5
                }
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

    fun setClickEvent(){

        for(cnt in 0..15){
            frameList[cnt].setOnLongClickListener {
                val popupMenu = PopupMenu(context, it)
                activity.menuInflater.inflate(R.menu.camera_menu, popupMenu.menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.addCam -> {
                            if (frameList[cnt].isSetup)
                                Toast.makeText(context, "이미 추가되어있습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            else {
                                frameList[cnt].setLabel("${cnt}")
                                frameList[cnt].setup(0)
                            }
                            true
                        }
                        else -> {
                            if (frameList[cnt].isSetup) {
                                frameList[cnt].unset()
                                frameList[cnt].isSetup = false
                            } else
                                Toast.makeText(context, "이미 제거했습니다.", Toast.LENGTH_SHORT).show()
                            true
                        }
                    }
                }
                true
            }
        }

        for(cnt in 0..15) {
                frameList[cnt].setOnClickListener {
                    if (frameList[cnt].z != 0.0f){
                        if (frameList[cnt].touchCnt == 0) {
                            layoutParamsList[cnt] =
                                frameList[cnt].layoutParams as GridLayout.LayoutParams

                            var countGroupNum = 0
                            var plusX = 0
                            var plusY = 0

                            for (i in (0..15).filter { (frameList[it].goto == frameList[cnt].goto) && (it != cnt) }) {

                                Log.d("CIVAL", i.toString())
                                layoutParamsList[i] =
                                    frameList[i].layoutParams as GridLayout.LayoutParams

                                if (countGroupNum == 1)
                                    plusX++
                                if (countGroupNum == 2) {
                                    plusY++
                                    plusX--
                                }

                                var layoutParams = GridLayout.LayoutParams(
                                    GridLayout.spec(frameList[i].movedY + plusY, 1, 0.5f),
                                    GridLayout.spec(frameList[i].movedX + plusX, 1, 0.5f)
                                )
                                layoutParams.width = 0
                                layoutParams.height = 0
                                frameList[i].z = 0.0f

                                frameList[i].layoutParams = layoutParams

                                countGroupNum++
                            }
                            layoutParamsList[frameList[cnt].goto] =
                                frameList[frameList[cnt].goto].layoutParams as GridLayout.LayoutParams

                            Log.d("CIVAL", frameList[cnt].goto.toString())

                            var layoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(frameList[cnt].movedY + plusY, 1, 0.5f),
                                GridLayout.spec(frameList[cnt].movedX + ++plusX, 1, 0.5f)
                            )
                            layoutParams.width = 0
                            layoutParams.height = 0
                            frameList[frameList[cnt].goto].z = 0.0f

                            frameList[frameList[cnt].goto].layoutParams = layoutParams

                            layoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(frameList[cnt].startY, 4, 2.0f),
                                GridLayout.spec(frameList[cnt].startX, 4, 2.0f)
                            )
                            layoutParams.width = 0
                            layoutParams.height = 0

                            frameList[cnt].z = 10.0f
                            frameList[cnt].layoutParams = layoutParams

                            frameList[cnt].touchCnt++

                            for (other in (0..15).filter { (it != cnt) && (frameList[it].goto != frameList[cnt].goto) && it!=frameList[cnt].goto }) {
                                layoutParamsList[other] =
                                    frameList[other].layoutParams as GridLayout.LayoutParams
                                frameList[other].z = 0.0f
                            }
                        } else if (frameList[cnt].touchCnt == 1) {
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                            val layoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(0, 8, 1.0f),
                                GridLayout.spec(0, 8, 1.0f)
                            )
                            frameList[cnt].layoutParams = layoutParams

                            if(frameList[cnt].isSetup)
                                frameList[cnt].showVoiceBtn(true)

                            frameList[cnt].touchCnt++
                        } else {
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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



        /* 개선해야 하는 부분
        for(cnt in 0..3){
            for(i in groupList[cnt]) {

                // 그리드 한칸당 설정
                frameList[i].setOnClickListener {
                    val scf = frameList[i].scf

                    if(frameList[i].z != 0.0f) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                        for (n in groupList[cnt].filter { it != i }) {
                            if (layoutParamsList[n] != null) {
                                if (frameList[i].touchCnt == 2) {
                                    frameList[n].layoutParams = layoutParamsList[n]
                                    layoutParamsList[n] = null
                                    frameList[n].z = 5.0f
                                    t = 0
                                }
                            } else {
                                layoutParamsList[n] =
                                    frameList[n].layoutParams as GridLayout.LayoutParams
                                val layoutParams = GridLayout.LayoutParams(
                                    GridLayout.spec(testList[cnt][t * 2], 1, 0.5f),
                                    GridLayout.spec(testList[cnt][t * 2 + 1], 1, 0.5f)
                                )
                                layoutParams.width = 0
                                layoutParams.height = 0
                                frameList[n].z = 5.0f
                                frameList[n].layoutParams = layoutParams
                                t++
                            }
                        }

                        if (layoutParamsList[i] != null) {

                            // 터치가 두번째(전체화면), 다시 원래 4x4로 복귀
                            if (frameList[i].touchCnt == 2) {
                                frameList[i].layoutParams = layoutParamsList[i]
                                layoutParamsList[i] = null
                                frameList[i].z = 5.0f

                                frameList[scf].layoutParams = layoutParamsList[scf]
                                layoutParamsList[scf] = null
                                frameList[scf].z = 5.0f

                                frameList[i].touchCnt = 0

                                for (a in (0..15).filter { it != i }) {
                                    frameList[a].z = 5.0f
                                }
                            }
                            // 터치가 첫번째(1차확대 상태), 전체화면으로 확대
                            else {
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                                val layoutParams = GridLayout.LayoutParams(
                                    GridLayout.spec(0, 8, 1.0f),
                                    GridLayout.spec(0, 8, 1.0f)
                                )
                                frameList[i].z = 10.0f
                                frameList[i].layoutParams = layoutParams

                                frameList[i].touchCnt++
                            }
                        } else {
                            layoutParamsList[i] =
                                frameList[i].layoutParams as GridLayout.LayoutParams
                            layoutParamsList[scf] =
                                frameList[scf].layoutParams as GridLayout.LayoutParams

                            val layoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(frameList[i].startY, 4, 1.0f),
                                GridLayout.spec(frameList[i].startX, 4, 1.0f)
                            )
                            val divLayoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(testList2[cnt * 2], 1, 0.5f),
                                GridLayout.spec(testList2[cnt * 2 + 1], 1, 0.5f)
                            )
                            frameList[i].z = 5.0f
                            frameList[i].layoutParams = layoutParams

                            divLayoutParams.width = 0
                            divLayoutParams.height = 0

                            frameList[scf].z = 5.0f
                            frameList[scf].layoutParams = divLayoutParams

                            frameList[i].touchCnt++

                            for (a in (0..15).filter { it != i }) {
                                frameList[a].z = 0.0f
                            }
                        }
                    }
                }
            }
        }
        개선해야 하는 부분 */
    }
}
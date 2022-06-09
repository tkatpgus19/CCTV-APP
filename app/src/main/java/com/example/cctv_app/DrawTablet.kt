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

    init {
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layoutTransition.setDuration(200)
    }

    /* 개선해야 하는 부분 */
    private val groupList = listOf(
        listOf(0,1,4,5),
        listOf(2,3,6,7),
        listOf(8,9,12,13),
        listOf(10,11,14,15))
    /* 개선해야 하는 부분 */

    fun setLayoutParams(){
        frameList = (0..15).map { i ->
            val frame = CctvLayout(activity)
            val layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(i / 4 * 2, 2, 1.0f),
                GridLayout.spec(i % 4 * 2, 2, 1.0f)
            )
            layoutParams.width = 0
            layoutParams.height = 0
            frame.layoutParams = layoutParams

            when(i){
                0,1,4,5 -> {
                    frame.setPos(0,0)
                    frame.scf = 6}
                2,3,6,7 -> {
                    frame.setPos(4,0)
                    frame.scf = 5}
                8,9,12,13 -> {
                    frame.setPos(0,4)
                    frame.scf = 10}
                else -> {
                    frame.setPos(4,4)
                    frame.scf = 9}
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
        /* 개선해야 하는 부분 */
        val testList = listOf(
            listOf(2,4, 2,5, 3,4),
            listOf(2,3, 3,2, 3,3),
            listOf(4,4, 5,4, 5,5),
            listOf(4,2, 4,3, 5,2))

        val testList2 = listOf(
            3,5, 2,2, 4,5, 5,3
        )

        var t = 0
        /* 개선해야 하는 부분 */

        for(cnt in 0..15){
            frameList[cnt].setOnLongClickListener {
                val popupMenu = PopupMenu(context, it)
                activity.menuInflater.inflate(R.menu.camera_menu, popupMenu.menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.addCam -> {
                            if(frameList[cnt].isSetup)
                                Toast.makeText(context, "이미 추가되어있습니다.", Toast.LENGTH_SHORT).show()
                            else{
                                frameList[cnt].setLabel("${cnt}")
                                frameList[cnt].setup(0)
                            }
                            true
                        }
                        else -> {
                            if(frameList[cnt].isSetup){
                                frameList[cnt].unset()
                                frameList[cnt].isSetup = false
                            }
                            else
                                Toast.makeText(context, "이미 제거했습니다.", Toast.LENGTH_SHORT).show()
                            true
                        }
                    }
                }
                true
            }
        }

        /* 개선해야 하는 부분 */
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
        /* 개선해야 하는 부분 */
    }
}
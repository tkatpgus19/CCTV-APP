package com.example.cctv_app

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
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

    fun setLayoutParams(){
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

    private fun getSize(): Point {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size
    }

    fun setClickEvent(save: ArrayList<Int>){
        var isWarning = false

        for(n in save){
            frameList[n].setLabel("${n}")
            frameList[n].setup(0)
            activatedCamList.add(n)
        }
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
                                activatedCamList.add(cnt)
                            }
                            true
                        }
                        else -> {
                            if (frameList[cnt].isSetup) {
                                frameList[cnt].unset()
                                frameList[cnt].isSetup = false
                                activatedCamList.remove(cnt)
                            } else
                                Toast.makeText(context, "이미 제거했습니다.", Toast.LENGTH_SHORT).show()
                            true
                        }
                    }
                }
                frameList[cnt].warning(true)
                isWarning = true
                true
            }

            frameList[cnt].setOnClickListener {

                if (frameList[cnt].z != 0.0f) {
                    if (layoutParamsList[cnt] != null && frameList[cnt].touchCnt == 2) {

                        frameList[cnt].layoutParams = layoutParamsList[cnt]
                        layoutParamsList[cnt] = null

                        for (other in (0..15).filter { it != cnt }) {
                            frameList[other].z = 5.0f
                            frameList[other].layoutParams = layoutParamsList[other]
                            layoutParamsList[other] = null
                        }
                        frameList[cnt].touchCnt = 0
                    }

                    else if (frameList[cnt].touchCnt == 0) {
                        layoutParamsList[cnt] = frameList[cnt].layoutParams as GridLayout.LayoutParams

                        var layoutParams = GridLayout.LayoutParams(
                            GridLayout.spec(cnt / 2 * 2, 4, 2.0f),
                            GridLayout.spec(0, 4, 2.0f)
                        )
                        layoutParams.width = 0
                        layoutParams.height = 0

                        frameList[cnt].z = 10.0f
                        frameList[cnt].layoutParams = layoutParams

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
                        } else {
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
                        for (other in (0..15).filter { it != cnt }) {
                            frameList[other].z = 0.0f
                        }
                        frameList[cnt].touchCnt++
                    }
                    else{
                        val bundle = Bundle()
                        bundle.putInt("cnt", cnt)
                        bundle.putBoolean("isWarning", isWarning)
                        bundle.putIntegerArrayList("activatedCamList", activatedCamList)
                        val fragment = FullscreenFragment()
                        fragment.arguments = bundle
                        activity.supportFragmentManager
                            .beginTransaction()
                            .add(R.id.nav_fragment, fragment)
                            .commit()
                    }
                }
            }
        }
    }
}
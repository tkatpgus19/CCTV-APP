package com.example.cctv_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView

class ExpandableListAdapter(
    private val context: Context,
    private val parents: MutableList<String>,
    private val childList: MutableList<MutableList<String>>
    ): BaseExpandableListAdapter(){

    override fun getGroupCount() = parents.size

    override fun getChildrenCount(p0: Int): Int = childList[p0].size

    override fun getGroup(p0: Int): Any = parents[p0]

    override fun getChild(p0: Int, p1: Int): String = childList[p0][p1]

    override fun getGroupId(p0: Int): Long = p0.toLong()

    override fun getChildId(p0: Int, p1: Int): Long = p1.toLong()

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(p0: Int, p1: Int): Boolean = true


    /* 부모 계층 레이아웃 설정 */
    override fun getGroupView(p0: Int, p1: Boolean, p2: View?, p3: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val parentView = inflater.inflate(R.layout.menu_parent, p3, false)

        val tvParent = parentView.findViewById<TextView>(R.id.tv_list_title)
        tvParent.text = parents[p0]

        setIcon(p0, parentView)
        setArrow(p0, parentView, p1)

        return parentView
    }

    /* 자식 계층 레이아웃 설정 */
    override fun getChildView(p0: Int, p1: Int, p2: Boolean, p3: View?, p4: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val childView = inflater.inflate(R.layout.menu_child, p4, false)
        val tvChild = childView.findViewById<TextView>(R.id.tv_child_title)
        tvChild.text = getChild(p0, p1)
        return childView
    }

    /* drawer 아이콘 설정 */
    private fun setIcon(parentPosition: Int, parentView: View) {
        val iv = parentView.findViewById<ImageView>(R.id.iv_img)
        when (parentPosition) {
            0 -> iv.setImageResource(R.drawable.ic_baseline_videocam_24)
            1 -> iv.setImageResource(R.drawable.ic_baseline_video_library_24)
            2 -> iv.setImageResource(R.drawable.ic_baseline_waves_24)
        }
    }

    /* 닫힘, 열림 표시해주는 화살표 설정 */
    private fun setArrow(parentPosition: Int, parentView: View, isExpanded: Boolean) {
        val arrow = parentView.findViewById<ImageView>(R.id.iv_arrow_drop)
        /* 0번째 부모는 자식이 없으므로 화살표 설정해주지 않음 */
        if (parentPosition == 0) {
            if (isExpanded) arrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
            else arrow.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
        }
    }
}
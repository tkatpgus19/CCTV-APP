package com.example.cctv_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.example.cctv_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 태블릿 확인 변수
    private var isTablet = false
    private var backKeyPressedTime = 0L

    // drawer layout 메뉴에서 어떤 메뉴가 선택되었는지 체크하는 변수
    private var selectedMenu = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setExpandableList()
    }

    /* DrawerLayout 메뉴, 클릭시 프래그먼트 이동 구현 */
    private fun setExpandableList(){
        val parentList = mutableListOf("실시간 CCTV", "저장된 CCTV 영상", "추가 예정")
        val childList = mutableListOf(
            mutableListOf("2x2", "4x4"),
            mutableListOf(),
            mutableListOf()
        )

        val expandableAdapter = ExpandableListAdapter(this, parentList, childList)
        binding.elMenu.setAdapter(expandableAdapter)

        /* 부모메뉴 클릭 리스너('실시간 CCTV', '저장된 CCTV 영상', '추가 예정') */
        binding.elMenu.setOnGroupClickListener { expandableListView, view, i, id ->
            when(id){
                1L ->{
                    /* '저장된 CCTV 영상' 클릭 */
                    val fragment = ArchiveFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.nav_fragment, fragment)
                        .commit()
                    selectedMenu = Companion.OTHER_MENU
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                2L -> {
                    /* '추가 예정' 클릭 */
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    selectedMenu = Companion.OTHER_MENU
                }
            }
            false
        }
        /* 자식메뉴 클릭 리스너('2x2', '4x4') */
        binding.elMenu.setOnChildClickListener { expandableListView, view, i, i2, id ->
            when(id){
                0L -> {
                    /* '2x2' 클릭 */
                    if(selectedMenu != 1) {
                        isTablet = false

                        expandableListView.getChildAt(1).isEnabled = false
                        val bundle = Bundle()
                        bundle.putBoolean("isTablet", isTablet)

                        val fragment = RealtimeFragment()
                        fragment.arguments = bundle
                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.nav_fragment, fragment)
                            .commit()
                        selectedMenu = 1
                    }
                    /* 이미 2x2 레이아웃 일때 알림메시지*/
                    else{
                        Toast.makeText(this, "이미 2x2 레이아웃입니다.", Toast.LENGTH_SHORT).show()
                        expandableListView.getChildAt(1).isEnabled = true
                    }
                }
                1L -> {
                    /* '4x4' 클릭 */
                    if (selectedMenu != 2) {
                        isTablet = true

                        expandableListView.getChildAt(2).isEnabled = false
                        val bundle = Bundle()
                        bundle.putBoolean("isTablet", isTablet)

                        val fragment = RealtimeFragment()
                        fragment.arguments = bundle
                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.nav_fragment, fragment)
                            .commit()
                        selectedMenu = 2
                    }
                    /* 이미 4x4 레이아웃 일때 알림메시지*/
                    else {
                        Toast.makeText(this, "이미 4x4 레이아웃입니다.", Toast.LENGTH_SHORT).show()
                        expandableListView.getChildAt(2).isEnabled = true
                    }
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            false
        }
    }

    /* 뒤로가기시 DrawerLayout 닫기 구현 */
    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)

        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this,"종료하려면 한번 더 누르세요", Toast.LENGTH_SHORT).show()
        }
        else if(System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish()
        }
            //super.onBackPressed()
    }

    companion object {
        private const val OTHER_MENU = 0
    }
}
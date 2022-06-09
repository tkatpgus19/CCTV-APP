package com.example.cctv_app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.example.cctv_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isTablet = false
    private var backKeyPressedTime = 0L
    private var saveInstance = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /*
        val save = intent.getIntegerArrayListExtra("save")
        if(save != null){
            val bundle = Bundle()
            bundle.putIntegerArrayList("saveList", save)
            bundle.putBoolean("isTablet", false)
            val fragment = RealtimeFragment()
            fragment.arguments = bundle
            supportFragmentManager
                .beginTransaction()
                .add(R.id.nav_fragment, fragment)
                .commit()
        }*/
        setExpandableList()
    }

    private fun setExpandableList(){
        val parentList = mutableListOf("실시간 CCTV", "저장된 CCTV 영상", "추가 예정")
        val childList = mutableListOf(
            mutableListOf("2x2", "4x4"),
            mutableListOf(),
            mutableListOf()
        )

        val expandableAdapter = ExpandableListAdapter(this, parentList, childList)

        binding.elMenu.setAdapter(expandableAdapter)

        binding.elMenu.setOnGroupClickListener { expandableListView, view, i, id ->
            when(id){
                1L ->{
                    val fragment = ArchiveFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.nav_fragment, fragment)
                        .commit()
                    saveInstance = 3
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                2L -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    saveInstance = 3
                }
            }
            false
        }
        binding.elMenu.setOnChildClickListener { expandableListView, view, i, i2, id ->
            when(id){
                0L -> {
                    if(saveInstance != 1) {
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
                        saveInstance = 1
                    }
                    else{
                        Toast.makeText(this, "이미 2x2 레이아웃입니다.", Toast.LENGTH_SHORT).show()
                        expandableListView.getChildAt(1).isEnabled = true
                    }
                }
                1L -> {
                    if (saveInstance != 2) {
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
                        saveInstance = 2
                    }
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
}
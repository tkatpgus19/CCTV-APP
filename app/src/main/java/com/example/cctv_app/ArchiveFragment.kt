package com.example.cctv_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.cctv_app.databinding.FragmentArchiveBinding

class ArchiveFragment : Fragment() {
    private lateinit var binding: FragmentArchiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* 저장된 CCTV 영상 조회하는 프래그먼트 */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_archive, container, false)
        val view = binding.root

        return view
    }
}
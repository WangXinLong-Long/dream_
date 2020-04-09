package com.alading.dream.ui.sofa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.alading.dream.R
import com.alading.libcommon.utils.MyLog
import com.example.libnavannotation.FragmentDestination

@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false)
class SofaFragment : Fragment() {

    private lateinit var sofaViewModel: SofaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sofaViewModel =
                ViewModelProviders.of(this).get(SofaViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sofa, container, false)
        val textView: TextView = root.findViewById(R.id.text_sofa)
        sofaViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        MyLog.logD("SofaFragment::onCreateView: sofaViewModel:${sofaViewModel}  ")
        return root
    }
}

package com.alading.dream.ui.sofa

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alading.dream.databinding.FragmentSofaBinding
import com.alading.dream.model.SofaTab
import com.alading.dream.ui.home.HomeFragment
import com.alading.dream.utils.AppConfig
import com.example.libnavannotation.FragmentDestination
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_sofa.*

@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false)
open class SofaFragment : Fragment() {

    private lateinit var sofaViewModel: SofaViewModel
    var tabs = arrayListOf<SofaTab.Tabs>()
    var binding: FragmentSofaBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sofaViewModel =
            ViewModelProviders.of(this).get(SofaViewModel::class.java)

        binding = FragmentSofaBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    private val mediator: TabLayoutMediator
        get() {
            return TabLayoutMediator(
                tab_layout!!, view_pager!!, false,
                TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                    tab.customView = makeTabView(position)
                })
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var tabConfig = getTabConfig()

        for (tab in tabConfig.tabs) {
            if (tab.enable) {
                tabs.add(tab)
            }
        }

        view_pager?.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        view_pager?.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return tabs.size
            }

            override fun createFragment(position: Int): Fragment {

                return getTabFragment(position)
            }

        }

        tab_layout.tabGravity = tabConfig.tabGravity

        mediator.attach()

        view_pager?.registerOnPageChangeCallback(mPageChangeCallback)

        view_pager?.post {
            view_pager?.currentItem = tabConfig.select
        }
    }

    private var mPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            var tabCount = tab_layout!!.tabCount
            for (i in 0 until tabCount) {
                var tabAt = tab_layout!!.getTabAt(i)!!
                var customView = tabAt.customView as TextView
                if (tabAt.position == position) {
                    customView.textSize = getTabConfig().activeSize.toFloat()
                    customView.typeface = Typeface.DEFAULT_BOLD
                } else {
                    customView.textSize = getTabConfig().normalSize.toFloat()
                    customView.typeface = Typeface.DEFAULT
                }
            }

        }
    }

    private fun makeTabView(position: Int): View {
        var tabView = TextView(context)
        var states = arrayOf(IntArray(1), IntArray(0))
        states[0] = IntArray(android.R.attr.state_selected)
        states[1] = IntArray(0)
        var colors = intArrayOf(
            Color.parseColor(getTabConfig().activeColor),
            Color.parseColor(getTabConfig().normalColor)
        )

        var colorStateList = ColorStateList(states, colors)
        tabView.setTextColor(colorStateList)
        tabView.text = tabs[position].title
        tabView.textSize = getTabConfig().normalSize.toFloat()
        return tabView
    }

    open fun getTabFragment(position: Int): Fragment {
        return HomeFragment.newInstance(tabs[position].tag)
    }

    open fun getTabConfig(): SofaTab {

        return AppConfig.getSofaTabConfig()
    }

    override fun onDestroy() {
        mediator.detach()
        view_pager?.unregisterOnPageChangeCallback(mPageChangeCallback)
        super.onDestroy()
    }
}

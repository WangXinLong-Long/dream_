package com.alading.dream.ui.find

import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.alading.dream.model.SofaTab
import com.alading.dream.ui.sofa.SofaFragment
import com.alading.dream.utils.AppConfig
import com.example.libnavannotation.FragmentDestination
import kotlinx.android.synthetic.main.fragment_sofa.*

@FragmentDestination(pageUrl = "main/tabs/find")
class FindFragment : SofaFragment() {

    override fun getTabFragment(position: Int): Fragment {
        val tab = getTabConfig().tabs.get(position)
        return TagListFragment.newInstance(tab.tag)
    }

    override fun getTabConfig(): SofaTab {
        return AppConfig.getFindTabConfig()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        var tagType = childFragment.arguments?.getString(TagListFragment.KEY_TAG_TYPE)
        if (TextUtils.equals(tagType,"onlyFollow")){
            ViewModelProviders.of(childFragment).get(TagListViewModel::class.java).switchTabLiveData.observe(this,
                Observer<Object> {
                    view_pager.currentItem = 1
                })
        }
    }
}

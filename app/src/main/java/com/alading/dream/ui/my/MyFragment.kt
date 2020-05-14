package com.alading.dream.ui.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.alading.dream.R
import com.alading.dream.databinding.FragmentMyBinding
import com.alading.dream.ui.login.UserManager
import com.alading.libcommon.utils.StatusBar
import com.example.libnavannotation.FragmentDestination

@FragmentDestination(pageUrl = "main/tabs/my", needLogin = true)
class MyFragment : Fragment() {

    private val myViewModel by viewModels<MyViewModel>()
    private val mBinding by lazy {
        DataBindingUtil.setContentView<FragmentMyBinding>(activity!!, R.layout.fragment_my)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var user = UserManager.get().user
        mBinding.user = user
        UserManager.get().refresh().observe(this, Observer {
            if (it!=null){
                mBinding.user = it
            }
        })

        mBinding.actionLogout.setOnClickListener {
            AlertDialog.Builder(context!!).setMessage(getString(R.string.fragment_my_logout))
                .setPositiveButton(getString(R.string.fragment_my_logout_ok)) { dialog, which ->
                    dialog.dismiss()
                    UserManager.get().logout()
                    activity?.onBackPressed()
                }.setNegativeButton(getString(R.string.fragment_my_logout_cancel), null)
                .create().show()
        }


        mBinding.goDetail.setOnClickListener { v ->
            ProfileActivity.startProfileActivity(
                context,
                ProfileActivity.TAB_TYPE_ALL
            )
        }
        mBinding.userFeed.setOnClickListener { v ->
            ProfileActivity.startProfileActivity(
                context,
                ProfileActivity.TAB_TYPE_FEED
            )
        }
        mBinding.userComment.setOnClickListener { v ->
            ProfileActivity.startProfileActivity(
                context,
                ProfileActivity.TAB_TYPE_COMMENT
            )
        }
        mBinding.userFavorite.setOnClickListener { v ->
            UserBehaviorListActivity.startBehaviorListActivity(
                context,
                UserBehaviorListActivity.BEHAVIOR_FAVORITE
            )
        }
        mBinding.userHistory.setOnClickListener { v ->
            UserBehaviorListActivity.startBehaviorListActivity(
                context,
                UserBehaviorListActivity.BEHAVIOR_HISTORY
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBar.lightStatusBar(activity, false)
        super.onCreate(savedInstanceState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        StatusBar.lightStatusBar(activity, hidden)
    }
}

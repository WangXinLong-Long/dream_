package com.alading.dream.ui.main

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.alading.dream.R
import com.alading.dream.data.model.Destination
import com.alading.dream.ui.login.UserManager
import com.alading.dream.utils.AppConfig
import com.alading.dream.utils.NavGraphBuilder
import com.alading.libcommon.utils.StatusBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        //由于 启动时设置了 R.style.launcher 的windowBackground属性
        //势必要在进入主页后,把窗口背景清理掉

        //由于 启动时设置了 R.style.launcher 的windowBackground属性
        //势必要在进入主页后,把窗口背景清理掉
        setTheme(R.style.AppTheme)

        //启用沉浸式布局，白底黑字

        //启用沉浸式布局，白底黑字
        StatusBar.fitSystemBar(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val navView: BottomNavigationView = findViewById(R.id.nav_view)
//        var navHostFragment = R.id.nav_host_fragment
//        navController = findNavController(navHostFragment)
//        nav_view.setupWithNavController(navController!!)
////        navController?.handleDeepLink(intent)
//        MyLog.logD(
//            "MainActivity::onCreate: navHostFragment:${navHostFragment} " +
//                    "supportFragmentManager.findFragmentById(navHostFragment)!!." +
//                    "id:${supportFragmentManager.findFragmentById(navHostFragment)!!.id}  "
//        )
//        NavGraphBuilder.build(
//            navController,
//            this,
//            supportFragmentManager.findFragmentById(navHostFragment)!!.id
//        )

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navController = NavHostFragment.findNavController(fragment!!)
        NavGraphBuilder.build(
            this, fragment.childFragmentManager, navController, fragment.id
        )

        nav_view.setOnNavigationItemSelectedListener(this)

//        var request = GetRequest<JSONObject>("http://www.mooc.com")
//        request.execute();
//
//        request.execute(object :JsonCallback<JSONObject>(){
//            override fun onSuccess(response: ApiResponse<JSONObject>?) {
//                super.onSuccess(response)
//            }
//        })

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val destConfig: HashMap<String, Destination> = AppConfig.getDestConfig()
        val iterator: Iterator<Map.Entry<String, Destination>> =
            destConfig.entries.iterator()
        //遍历 target destination 是否需要登录拦截
        while (iterator.hasNext()) {
            val entry: Map.Entry<String, Destination> = iterator.next()
            val value: Destination = entry.value
            if (value != null && !UserManager.get()
                        //TODO 修复选择时页面重叠的bug
                    .isLogin && value.needLogin && value.id == menuItem.getItemId()
            ) {
                UserManager.get().login(this)
                    .observe(this, Observer<Any?> { user ->
                        if (user != null) {
                            nav_view.setSelectedItemId(menuItem.getItemId())
                        }
                    })
                return false
            }
        }

        navController?.navigate(menuItem.getItemId())
        return !TextUtils.isEmpty(menuItem.getTitle())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var currentPageId = navController?.currentDestination?.id
        var homeDestId = navController?.graph?.startDestination
        if (currentPageId != homeDestId) {
            nav_view.selectedItemId = homeDestId!!
            return
        }

        finish()
    }
}

package com.alading.dream.ui.main

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.alading.dream.R
import com.alading.dream.utils.MyLog
import com.alading.dream.utils.NavGraphBuilder

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var navController : NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        var navHostFragment = R.id.nav_host_fragment
        navController = findNavController(navHostFragment)

        navView.setupWithNavController(navController!!)
//        navController?.handleDeepLink(intent)
MyLog.logD("MainActivity::onCreate: navHostFragment:${navHostFragment} supportFragmentManager.findFragmentById(navHostFragment)!!.id:${supportFragmentManager.findFragmentById(navHostFragment)!!.id}  ")
        NavGraphBuilder.build(navController,this,supportFragmentManager.findFragmentById(navHostFragment)!!.id)

        navView.setOnNavigationItemSelectedListener(this)


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navController?.navigate(item.itemId)
        return !TextUtils.isEmpty(item.title)
    }
}

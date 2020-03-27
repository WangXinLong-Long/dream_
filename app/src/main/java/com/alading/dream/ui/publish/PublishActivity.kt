package com.alading.dream.ui.publish

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
import com.alading.dream.utils.NavGraphBuilder
import com.example.libnavannotation.ActivityDestination

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
class PublishActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_publish)


    }


}

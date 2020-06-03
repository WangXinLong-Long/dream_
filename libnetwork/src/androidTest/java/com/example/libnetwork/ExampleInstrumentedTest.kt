package com.example.libnetwork

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.example.libnetwork.test", appContext.packageName)

        var newSingleThreadExecutor = Executors.newSingleThreadExecutor()
        var submit = newSingleThreadExecutor.submit(Callable {
            println("kaishi ");
            Thread.sleep(3000)
            "hhaaahha"
        })

        println("main running...")
        println("submit result:${submit.get()}")
    }
}

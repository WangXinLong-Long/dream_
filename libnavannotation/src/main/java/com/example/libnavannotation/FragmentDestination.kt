package com.example.libnavannotation

import java.lang.annotation.ElementType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FragmentDestination(
    val pageUrl: String,
    val needLogin: Boolean = false,
    val asStarter: Boolean = false
) {

}
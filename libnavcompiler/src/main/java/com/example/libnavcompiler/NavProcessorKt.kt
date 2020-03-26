package com.example.libnavcompiler

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement


class NavProcessorKt:AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {

        return false
    }
}
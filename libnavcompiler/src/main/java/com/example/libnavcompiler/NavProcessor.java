package com.example.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.libnavannotation.ActivityDestination;
import com.example.libnavannotation.FragmentDestination;
import com.google.auto.service.AutoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;


@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.example.libnavannotation.ActivityDestination", "com.example.libnavannotation.FragmentDestination"})

public class NavProcessor extends AbstractProcessor {

    private Messager messager;

    private Filer filer;

    private static final String OUTPUT_FILE_NAME = "destination.json";


    @Override

    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);

        //日志打印,在java环境下不能使用android.util.log.e()

        messager = processingEnv.getMessager();

        //文件处理工具

        filer = processingEnv.getFiler();

    }


    @Override

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


        Set<? extends Element> fragmentElements = roundEnv.getElementsAnnotatedWith(FragmentDestination.class);

        Set<? extends Element> activityElements = roundEnv.getElementsAnnotatedWith(ActivityDestination.class);


        if (!fragmentElements.isEmpty() || !activityElements.isEmpty()) {

            HashMap<String, JSONObject> destMap = new HashMap<>();

            handleDestination(fragmentElements, FragmentDestination.class, destMap);

            handleDestination(activityElements, ActivityDestination.class, destMap);


            //app/src/main/assets

            FileOutputStream fos = null;

            OutputStreamWriter writer = null;

            try {
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);

                String resourcePath = resource.toUri().getPath();

                messager.printMessage(Diagnostic.Kind.NOTE, "resourcePath:" + resourcePath);

                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);

                String assetsPath = appPath + "src/main/assets/";


                File file = new File(assetsPath);

                if (!file.exists()) {

                    file.mkdirs();

                }


                //此处就是稳健的写入了

                File outPutFile = new File(file, OUTPUT_FILE_NAME);

                if (outPutFile.exists()) {

                    outPutFile.delete();

                }

                outPutFile.createNewFile();


                String content = JSON.toJSONString(destMap);

                fos = new FileOutputStream(outPutFile);

                writer = new OutputStreamWriter(fos, "UTF-8");

                writer.write(content);

                writer.flush();

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                if (writer != null) {

                    try {

                        writer.close();

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }


                if (fos != null) {

                    try {

                        fos.close();

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

            }

        }



        return true;

    }


    private void handleDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClaz, HashMap<String, JSONObject> destMap) {

        for (Element element : elements) {

            TypeElement typeElement = (TypeElement) element;


            String clazName = typeElement.getQualifiedName().toString();

            int id = Math.abs(clazName.hashCode());

            String pageUrl = null;

            boolean needLogin = false;

            boolean asStarter = false;

            boolean isFragment = false;


            Annotation annotation = element.getAnnotation(annotationClaz);

            if (annotation instanceof FragmentDestination) {

                FragmentDestination dest = (FragmentDestination) annotation;

                pageUrl = dest.pageUrl();

                asStarter = dest.asStarter();

                needLogin = dest.needLogin();

                isFragment = true;

            } else if (annotation instanceof ActivityDestination) {

                ActivityDestination dest = (ActivityDestination) annotation;

                pageUrl = dest.pageUrl();

                asStarter = dest.asStarter();

                needLogin = dest.needLogin();

                isFragment = false;

            }


            if (destMap.containsKey(pageUrl)) {

                messager.printMessage(Diagnostic.Kind.ERROR, "different page can use same pageUrl:" + clazName);

            } else {

                JSONObject object = new JSONObject();

                object.put("id", id);

                object.put("needLogin", needLogin);

                object.put("asStarter", asStarter);

                object.put("pageUrl", pageUrl);

                object.put("className", clazName);

                object.put("isFragment", isFragment);

                destMap.put(pageUrl, object);

            }

        }

    }

}

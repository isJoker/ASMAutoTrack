package com.jokerwan.analytics.android.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.apache.commons.codec.digest.DigestUtils

class JokerAnalyticsTransform extends Transform {
    private static Project project
    private JokerAnalyticsExtension jokerAnalyticsExtension

    JokerAnalyticsTransform(Project project, JokerAnalyticsExtension jokerAnalyticsExtension) {
        this.project = project
        this.jokerAnalyticsExtension = jokerAnalyticsExtension
    }

    @Override
    String getName() {
        return "JokerWanAnalytics"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     *
     *  SCOPE_FULL_PROJECT = ImmutableSet.of(Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES);
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _transform(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental)
    }

    void _transform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        if (!isIncremental) {
            outputProvider.deleteAll()
        }

        // Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历
        inputs.each { TransformInput input ->
            // 遍历目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                _processDirectoryInput(outputProvider, directoryInput, context)
            }

            // 遍历 jar
            input.jarInputs.each { JarInput jarInput ->
                _processJarInput(jarInput, outputProvider, context)
            }
        }
    }

    void _processJarInput(JarInput jarInput, TransformOutputProvider outputProvider, Context context) {
        String destName = jarInput.file.name

        // 截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
        // 获取 jar 名字
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }

        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        def modifiedJar = null
        // 修改字节码文件
        if (!jokerAnalyticsExtension.disableAppClick) {
            modifiedJar = JokerAnalyticsClassModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true)
        }
        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }
        FileUtils.copyFile(modifiedJar, dest)
    }

    void _processDirectoryInput(TransformOutputProvider outputProvider, DirectoryInput directoryInput,Context context) {
        // 当前这个 Transform 输出目录
        File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        File dir = directoryInput.file

        if (dir) {
            HashMap<String, File> modifyMap = new HashMap<>()
            // 遍历以".class"扩展名结尾的文件
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                File classFile ->
                    if (JokerAnalyticsClassModifier.isShouldModify(classFile.name)) {
                        File modified = null
                        // 修改字节码文件
                        if (!jokerAnalyticsExtension.disableAppClick) {
                            modified = JokerAnalyticsClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir())
                        }
                        if (modified != null) {
                            // key 为 类名，classFile.absolutePath 如：/com/jokerwan/testasm/MainActivity.class
                            String key = classFile.absolutePath.replace(dir.absolutePath, "")
                            modifyMap.put(key, modified)
                        }
                    }
            }
            FileUtils.copyDirectory(directoryInput.file, dest)
            modifyMap.entrySet().each {
                Map.Entry<String, File> en ->
                    File target = new File(dest.absolutePath + en.getKey())
                    if (target.exists()) {
                        target.delete()
                    }
                    FileUtils.copyFile(en.getValue(), target)
                    en.getValue().delete()
            }
        }
    }
}
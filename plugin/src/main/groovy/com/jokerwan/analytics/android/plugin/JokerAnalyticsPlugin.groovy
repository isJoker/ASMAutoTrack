package com.jokerwan.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class JokerAnalyticsPlugin implements Plugin<Project> {
    void apply(Project project) {

        JokerAnalyticsExtension extension = project.extensions.create("JokerWanAnalytics", JokerAnalyticsExtension)

        boolean disableSensorsAnalyticsPlugin = false
        Properties properties = new Properties()
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            disableSensorsAnalyticsPlugin = Boolean.parseBoolean(properties.getProperty("jokerAnalytics.disablePlugin", "false"))
        }

        if (!disableSensorsAnalyticsPlugin) {
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(new JokerAnalyticsTransform(project, extension))
        } else {
            println("------------您已关闭了JokerWan埋点插件--------------")
        }
    }
}
pluginManagement {
    repositories {
        // 阿里云Gradle插件镜像
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        // 阿里云公共仓库（包含Google仓库）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 阿里云Google仓库镜像（专门用于Android相关依赖）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 保留原始仓库作为备用
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像（优先级最高）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // 其他镜像源作为备用
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        // 保留原始仓库
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        mavenLocal()
    }
}

include(":app")


rootProject.name = "KeepLiveService"
 include(":framework")

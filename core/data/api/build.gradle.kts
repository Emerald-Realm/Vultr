plugins {
  id("voice.kmp.library")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
}

kotlin {
  explicitApi()

  sourceSets.getByName("commonMain").dependencies {
    api(projects.core.common)
    api(projects.core.documentfile)
    api(libs.room.runtime.common)
    api(libs.datastore)
    implementation(libs.metro.runtime)
    implementation(libs.serialization.json)
  }
  sourceSets.getByName("androidMain").dependencies {
    implementation(project.dependencies.platform(libs.compose.bom))
    api(libs.compose.runtime)
    implementation(libs.appCompat)
    implementation(libs.androidxCore)
  }
}

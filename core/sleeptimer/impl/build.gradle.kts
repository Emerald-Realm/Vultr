plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
}

kotlin {
  sourceSets.getByName("commonMain").dependencies {
    implementation(projects.core.sleeptimer.api)
    implementation(projects.core.analytics.api)
    implementation(libs.metro.runtime)
  }
  sourceSets.getByName("androidMain").dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data.api)
    implementation(projects.core.initializer)
    implementation(projects.core.playback)
    implementation(libs.androidxCore)
    implementation(libs.seismic)
    implementation(libs.appCompat)
    implementation(libs.metro.runtime)
  }
  sourceSets.getByName("androidHostTest").dependencies {
    implementation(libs.junit)
    implementation(libs.mockk)
    implementation(libs.koTest.assert)
    implementation(libs.coroutines.test)
    implementation(libs.androidX.test.core)
    implementation(libs.androidX.test.junit)
    implementation(libs.androidX.test.runner)
    implementation(libs.robolectric)
  }
}

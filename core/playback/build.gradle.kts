plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android {
    androidResources {
      enable = true
    }
  }

  sourceSets.getByName("commonMain").dependencies {
    implementation(projects.core.data.api)
    implementation(projects.core.featureflag)
    implementation(libs.datastore)
    implementation(libs.serialization.json)
    implementation(libs.metro.runtime)
  }
  sourceSets.getByName("androidMain").dependencies {
    implementation(projects.core.common)
    implementation(projects.core.strings)
    implementation(projects.core.sleeptimer.api)
    implementation(projects.core.analytics.api)
    implementation(libs.androidxCore)
    implementation(libs.coil)
    implementation(libs.coroutines.guava)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
  }
  sourceSets.getByName("androidHostTest").dependencies {
    implementation(libs.media3.testUtils.core)
    implementation(libs.media3.testUtils.robolectric)
    implementation(libs.robolectric)
    implementation(libs.junit)
    implementation(libs.mockk)
    implementation(libs.coroutines.test)
    implementation(libs.koTest.assert)
    implementation(libs.androidX.test.core)
    implementation(libs.androidX.test.runner)
    implementation(libs.androidX.test.junit)
  }
}

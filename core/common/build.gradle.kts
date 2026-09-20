plugins {
  id("voice.kmp.library")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets.getByName("androidMain").dependencies {
    implementation(libs.androidxCore)
    implementation(libs.serialization.json)
  }
  sourceSets.getByName("androidHostTest").dependencies {
    implementation(libs.robolectric)
    implementation(libs.androidX.test.core)
    implementation(libs.androidX.test.junit)
    implementation(libs.androidX.test.runner)
    implementation(libs.junit)
    implementation(libs.mockk)
    implementation(libs.koTest.assert)
    implementation(libs.coroutines.test)
    implementation(libs.turbine)
  }
}

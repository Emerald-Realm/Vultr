plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
}

kotlin {
  sourceSets.getByName("commonMain").dependencies {
    implementation(projects.core.data.api)
    implementation(libs.metro.runtime)
  }
  sourceSets.getByName("androidHostTest").dependencies {
    implementation(projects.core.data.impl)
    implementation(libs.room.testing)
    implementation(libs.androidX.test.core)
    implementation(libs.androidX.test.junit)
    implementation(libs.androidX.test.runner)
    implementation(libs.robolectric)
    implementation(libs.junit)
    implementation(libs.koTest.assert)
    implementation(libs.coroutines.test)
  }
}

plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
}

kotlin {
  explicitApi()

  sourceSets.getByName("commonMain").dependencies {
    implementation(projects.core.data.api)
    implementation(projects.core.documentfile)
    implementation(projects.core.initializer)
    implementation(libs.metro.runtime)
  }
  sourceSets.getByName("androidMain").dependencies {
    implementation(libs.slf4j.noop)
    implementation(libs.jebml)
    implementation(libs.androidxCore)
    implementation(libs.documentFile)
    implementation(libs.media3.inspector)
    implementation(libs.coroutines.guava)
  }
  sourceSets.getByName("androidHostTest").dependencies {
    implementation(projects.core.data.impl)
    implementation(libs.junit)
    implementation(libs.androidX.test.runner)
    implementation(libs.androidX.test.core)
    implementation(libs.androidX.test.junit)
    implementation(libs.mockk)
    implementation(libs.coroutines.test)
    implementation(libs.robolectric)
    implementation(libs.koTest.assert)
  }
}

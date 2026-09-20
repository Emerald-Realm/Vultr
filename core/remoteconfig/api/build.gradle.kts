plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
}

kotlin {
  sourceSets.getByName("androidMain").dependencies {
    implementation(projects.core.common)
    implementation(projects.core.initializer)
  }
}

plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
}

kotlin {
  sourceSets.getByName("commonMain").dependencies {
    implementation(projects.core.remoteconfig.api)
  }
}

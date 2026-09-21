plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets.getByName("commonMain").dependencies {
    implementation(projects.core.data.api)
    implementation(projects.core.remoteconfig.api)
    implementation(libs.datastore)
    implementation(libs.serialization.json)
    implementation(libs.metro.runtime)
  }
}

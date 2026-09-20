plugins {
  id("voice.kmp.library")
  alias(libs.plugins.metro)
}

kotlin {
  sourceSets.getByName("commonMain").dependencies {
    api(projects.core.analytics.api)
  }
}

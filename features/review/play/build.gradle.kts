plugins {
  id("voice.library")
  id("voice.compose")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.data.api)
  implementation(projects.core.playback)
  implementation(projects.core.featureflag)
  implementation(projects.core.strings)
  implementation(libs.lifecycle)
  implementation(libs.appCompat)

  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
  testImplementation(libs.junit)
}

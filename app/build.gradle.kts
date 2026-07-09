@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.ManagedVirtualDevice
import java.util.Properties

plugins {
  id("voice.app")
  id("voice.compose")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  alias(libs.plugins.crashlytics) apply false
  alias(libs.plugins.googleServices) apply false
}

fun includeProprietaryLibraries(): Boolean {
  val includeProprietaryLibraries = providers.gradleProperty("voice.includeProprietaryLibraries").get().toBooleanStrict()
  if (!includeProprietaryLibraries) {
    return false
  }
  return file("google-services.json").exists()
    .also { present ->
      if (!present) {
        logger.warn("Google Services JSON file not found, disabling proprietary libraries.")
      }
    }
}

if (includeProprietaryLibraries()) {
  pluginManager.apply(libs.plugins.googleServices.get().pluginId)
  pluginManager.apply(libs.plugins.crashlytics.get().pluginId)
}

android {

  namespace = "voice.app"

  androidResources {
    generateLocaleConfig = true
  }

  dependenciesInfo {
    // disable the dependencies info in apks to allow reproducible builds
    // see https://github.com/VoiceAudiobook/Voice/discussions/2862#discussioncomment-13622836
    includeInApk = false
  }

  defaultConfig {
    applicationId = "io.github.emeraldrealm.raven"
    // versionName is user-facing (can be "1.01"); versionCode must be a unique integer for Play
    versionName = providers.gradleProperty("voice.versionName").orNull ?: "1.01"
    versionCode = providers.gradleProperty("voice.versionCode").orNull?.toInt() ?: 2

    testInstrumentationRunner = "voice.app.VoiceJUnitRunner"
  }

  sourceSets {
    named("androidTest") {
      assets.directories += layout.projectDirectory.dir("../Images").asFile.path
    }
  }

  fun createSigningConfig(name: String): ApkSigningConfig? {
    val rootPropertiesFile = layout.settingsDirectory.file("keystore.properties").asFile
      .takeIf { it.canRead() }
    val propertiesFile = rootPropertiesFile
      ?: layout.settingsDirectory.file("signing/$name/signing.properties").asFile.takeIf { it.canRead() }
      ?: layout.settingsDirectory.file("signing/ci/signing.properties").asFile.takeIf { it.canRead() }
      ?: return null
    return signingConfigs.create(name) {
      val properties = Properties()
      propertiesFile.inputStream().use { input ->
        properties.load(input)
      }
      storeFile = properties["storeFile"]
        ?.toString()
        ?.let(::File)
        ?: File(propertiesFile.parentFile, "signing.keystore")
      storePassword = properties["storePassword"]?.toString() ?: properties["STORE_PASSWORD"] as String
      keyAlias = properties["keyAlias"]?.toString() ?: properties["KEY_ALIAS"] as String
      keyPassword = properties["keyPassword"]?.toString() ?: properties["KEY_PASSWORD"] as String
    }
  }

  val playSigningConfig = createSigningConfig("play")
  val githubSigningConfig = createSigningConfig("github")

  val signingFlavor = "signing"
  flavorDimensions += signingFlavor
  productFlavors {
    register("github") {
      dimension = signingFlavor
      githubSigningConfig?.let { signingConfig = it }
    }
    register("play") {
      dimension = signingFlavor
      playSigningConfig?.let { signingConfig = it }
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      ndk {
        // Include native debug symbols in the AAB for Play Console crash/ANR analysis
        debugSymbolLevel = "FULL"
      }
    }
    getByName("debug") {
      isMinifyEnabled = false
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
    all {
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard.pro",
        ),
      )
      buildConfigField(type = "Boolean", name = "INCLUDE_ANALYTICS", value = includeProprietaryLibraries().toString())
    }
  }

  testOptions {
    unitTests {
      isReturnDefaultValues = true
      isIncludeAndroidResources = true
    }
    animationsDisabled = true
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    managedDevices {
      allDevices.create("voiceDevice", ManagedVirtualDevice::class.java) {
        device = "Pixel 9"
        apiLevel = 33
      }
    }
  }

  lint {
    checkDependencies = true
    ignoreTestSources = true
    checkReleaseBuilds = false
    warningsAsErrors = providers.gradleProperty("voice.warningsAsErrors").get().toBooleanStrict()
  }

  packaging {
    with(resources.pickFirsts) {
      add("META-INF/atomicfu.kotlin_module")
      add("META-INF/core.kotlin_module")
    }
  }

  buildFeatures {
    buildConfig = true
  }

  androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
      val hasSigningConfig = variant.productFlavors.any { (_, flavor) ->
        when (flavor) {
          "github" -> githubSigningConfig != null
          "play" -> playSigningConfig != null
          else -> false
        }
      }
      if (!hasSigningConfig) {
        throw GradleException(
          "Release builds require signing. Create an ignored keystore.properties file at the repo root.",
        )
      }
    }
  }
}

dependencies {
  implementation(projects.core.strings)
  implementation(projects.core.ui)
  implementation(projects.core.common)
  implementation(projects.core.data.api)
  implementation(projects.core.data.impl)
  implementation(projects.core.playback)
  implementation(projects.core.scanner)
  implementation(projects.core.featureflag)
  implementation(projects.core.initializer)
  implementation(projects.features.playbackScreen)
  implementation(projects.navigation)
  implementation(projects.core.sleeptimer.api)
  implementation(projects.core.sleeptimer.impl)
  implementation(projects.features.sleepTimer)
  implementation(projects.features.settings)
  implementation(projects.features.folderPicker)
  implementation(projects.features.bookOverview)
  implementation(projects.core.search)
  implementation(projects.features.cover)
  implementation(projects.core.documentfile)
  implementation(projects.features.onboarding)
  implementation(projects.features.widget)

  implementation(libs.appCompat)
  implementation(libs.datastore)

  implementation(libs.navigation3.ui)

  implementation(libs.serialization.json)

  implementation(libs.coil)

  if (includeProprietaryLibraries()) {
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(projects.core.logging.crashlytics)
    implementation(projects.features.review.play)
  } else {
    implementation(projects.features.review.noop)
  }

  implementation(projects.core.remoteconfig.api)
  if (includeProprietaryLibraries()) {
    implementation(projects.core.remoteconfig.firebase)
  } else {
    implementation(projects.core.remoteconfig.noop)
  }

  implementation(projects.core.analytics.api)
  if (includeProprietaryLibraries()) {
    implementation(projects.core.analytics.firebase)
  } else {
    implementation(projects.core.analytics.noop)
  }

  debugImplementation(projects.core.logging.debug)

  implementation(libs.androidxCore)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)

  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)

  testImplementation(libs.androidX.test.runner)
  testImplementation(libs.androidX.test.junit)
  testImplementation(libs.androidX.test.core)
  testImplementation(libs.robolectric)
  testImplementation(libs.coroutines.test)
  testImplementation(kotlin("reflect"))

  debugImplementation(libs.compose.ui.testManifest)

  androidTestImplementation(platform(libs.compose.bom))
  androidTestImplementation(libs.androidX.test.espresso.core)
  androidTestImplementation(libs.androidX.test.runner)
  androidTestImplementation(libs.androidX.test.rules)
  androidTestImplementation(libs.androidX.test.junit)
  androidTestImplementation(libs.media3.testUtils.core)
  androidTestImplementation(libs.koTest.assert)
  androidTestImplementation(libs.androidX.test.services)
  androidTestImplementation(libs.androidX.test.uiautomator)
  androidTestImplementation(libs.compose.ui.testJunit)
  androidTestImplementation(libs.coroutines.test)
  androidTestUtil(libs.androidX.test.orchestrator)
}

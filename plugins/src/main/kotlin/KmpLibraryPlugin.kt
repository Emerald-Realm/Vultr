import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for Kotlin Multiplatform library modules.
 *
 * Shared code lives in `commonMain` and must not reference platform APIs. Android-specific
 * code lives in `androidMain`. An `iosMain` target can be added later without restructuring
 * the shared source set.
 */
class KmpLibraryPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.pluginManager.run {
      apply("voice.ktlint")
      apply("org.jetbrains.kotlin.multiplatform")
      apply("com.android.kotlin.multiplatform.library")
    }
    val libs: VersionCatalog = target.extensions.getByType<VersionCatalogsExtension>().named("libs")
    val jvmBytecodeVersion = libs.findVersion("jvm-bytecode").get().requiredVersion.toInt()
    val jvmToolchainVersion = libs.findVersion("jvm-toolchain").get().requiredVersion.toInt()

    target.extensions.configure<KotlinMultiplatformExtension> {
      jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmToolchainVersion))
      }
      compilerOptions {
        freeCompilerArgs.addAll(
          "-Xannotation-default-target=param-property",
          "-Xreturn-value-checker=full",
        )
        optIn.addAll(
          listOf(
            "kotlin.RequiresOptIn",
            "kotlin.ExperimentalStdlibApi",
            "kotlin.contracts.ExperimentalContracts",
            "kotlin.time.ExperimentalTime",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
            "kotlinx.coroutines.FlowPreview",
          ),
        )
        allWarningsAsErrors.set(
          target.providers.gradleProperty("voice.warningsAsErrors").map(String::toBooleanStrict),
        )
      }
      extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("android") {
        namespace = "voice." + target.path.removePrefix(":").replace(':', '.')
        compileSdk = libs.findVersion("sdk-compile").get().requiredVersion.toInt()
        minSdk = libs.findVersion("sdk-min").get().requiredVersion.toInt()
        withHostTest {}
      }
      targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        compilerOptions {
          jvmTarget.set(JvmTarget.fromTarget(jvmBytecodeVersion.toString()))
        }
      }
      sourceSets.getByName("commonMain").dependencies {
        implementation(libs.findLibrary("coroutines-core").get())
        if (target.path != ":core:logging:api") {
          implementation(project(":core:logging:api"))
        }
      }
      sourceSets.getByName("commonTest").dependencies {
        implementation(libs.findLibrary("kotlin-test").get())
        implementation(libs.findLibrary("coroutines-test").get())
        implementation(libs.findLibrary("turbine").get())
        implementation(libs.findLibrary("koTest-assert").get())
      }
      sourceSets.getByName("androidMain").dependencies {
        implementation(libs.findLibrary("coroutines-android").get())
      }
    }
    target.tasks.register("voiceUnitTest") {
      group = "verification"
      description = "Runs the Android host unit tests of this KMP module."
      dependsOn(target.tasks.matching { it.name == "testAndroidHostTest" })
    }
  }
}

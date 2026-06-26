# Raven

![CI](https://github.com/Emerald-Realm/Vultr/actions/workflows/ci.yml/badge.svg?branch=main)

<a href="https://play.google.com/store/apps/details?id=io.github.emeraldrealm.raven"><img src="https://raw.githubusercontent.com/Emerald-Realm/Vultr/main/fastlane/metadata/android/en-US/images/featureGraphic.jpeg" width="600" ></a>

**Raven** is a minimalist, offline, open-source audiobook player for Android — built for reliability and simplicity. No accounts, no ads, no tracking; just your own audiobooks, played beautifully.

<a href="https://play.google.com/store/apps/details?id=io.github.emeraldrealm.raven">
  <img alt="Get it on Google Play"
       height="80"
       src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" />
</a>

## Documentation

- [Privacy Policy](docs/privacy-policy.md)
- [Terms of Use](docs/terms.md)
- [Open Source Licenses](docs/licenses.md)
- [FAQ](docs/faq.md)
- [Architecture](docs/architecture.md) · [Development](docs/development.md)

Site (GitHub Pages): https://emerald-realm.github.io/Vultr/

## Building

```
./gradlew :app:assemblePlayDebug      # debug APK
./gradlew :app:bundlePlayRelease      # release AAB (requires signing config)
./gradlew voiceUnitTest               # run unit tests
```

See [docs/development.md](docs/development.md) for environment setup.

## License & attribution

Raven is licensed under the [GNU GPLv3](LICENSE.md). By contributing, you agree to license your code under the same terms.

Raven is an independent fork of the open-source [Voice](https://github.com/PaulWoitaschek/Voice) audiobook player (© Paul Woitaschek and contributors, GPLv3). It is not affiliated with or endorsed by the Voice project. See [ATTRIBUTION.md](ATTRIBUTION.md).

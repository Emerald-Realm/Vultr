package voice.app.features.widget

/**
 * Distinct [BaseWidgetProvider] subclasses so each shows up as its own option in the
 * home-screen widget picker. They all share the same update logic; the [WidgetUpdater]
 * renders the right layout per provider.
 */
class RavenWidgetCompactProvider : BaseWidgetProvider()

class RavenWidgetBarProvider : BaseWidgetProvider()

class RavenWidgetControlsProvider : BaseWidgetProvider()

class RavenWidgetShowcaseProvider : BaseWidgetProvider()

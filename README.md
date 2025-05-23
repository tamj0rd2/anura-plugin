# anura-plugin

![Build](https://github.com/tamj0rd2/anura-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/26704-anura.svg)](https://plugins.jetbrains.com/plugin/26704-anura)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/26704-anura.svg)](https://plugins.jetbrains.com/plugin/26704-anura)

<!-- Plugin description -->
Features:

- Navigate to the kotlin definition of variables used in handlebars files
- Navigate to partials that are used in your handlebars file

For this to work, your code is expected to follow some conventions:

- Your hbs Views are called `<Something>View`
- Your kotlin ViewModels are called `<Something>ViewModel`
- Your hbs partials and kotlin models can be called whatever you want, but they should share the same name. i.e I may have a `Person` kotlin class and a `Person.hbs` file

Gotchas:

- For performance reasons, you'll only be taken to kotlin definitions that exist in the same module that the hbs file exists in. 

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "anura-plugin"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/26704-anura) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/26704-anura/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/tamj0rd2/anura-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation

# BuycraftX

BuycraftX is an entirely new plugin for use with the [Buycraft.net service](https://www.buycraft.net).

## The major differences

* A total rewrite of the plugin based on modern coding standards. The new plugin is geared towards reliability and performance.
* Supports multiple platforms:
  * Spigot 1.8 or above
  * BungeeCord (recent versions)
  * Sponge 3.x
* Custom item IDs are not supported, as it is not portable to other platforms and is deprecated.

## Standalone executor

BuycraftX can be integrated into your own custom applications to handle command execution. Most applications will
find `StandaloneBuycraftRunnerBuilder` to be the easiest method for integration, but you can also implement the whole
BuycraftX stack if desired.
# BuycraftX [![Build Status](https://travis-ci.org/20zinnm/BuycraftX.svg?branch=master)](https://travis-ci.org/20zinnm/BuycraftX)

BuycraftX is an entirely new plugin for use with the [Buycraft.net service](https://www.buycraft.net).

## The major differences

* A total rewrite of the plugin based on modern coding standards. The new plugin is geared towards reliability and performance.
* Supports multiple platforms:
  * Spigot 1.8 or above
  * BungeeCord (recent versions)
  * Sponge (coming soon!)
* Custom item IDs are not supported, as it is not portable to other platforms and is deprecated.

## This fork

This fork was created with the intention of porting Buycraft to Sponge-based platforms (such as SpongeVanilla and SpongeForge). It is brand new and
 should not be used on production servers. Current implementations:

 * Commands:
   * /buycraft list: Lists all the available packages and their current price, linking to a checkout page. The list uses Sponge's Pagination
   Service so it is presentable.
   * buycraft secret: The / is omitted on purpose, because this is a console-only command. Fully implemented.
   * /buycraft report: Generates a report to give to Buycraft Staff in the event of an error. This is kind of useless right now because this
   port is not supported yet, but in the future it will be vital you give a copy of the report to the appropriate party.
   * /buycraft refresh: Refreshes the listing.
 * Features:
   * Signs: In progress. Should have these completed soon.
   * GUI: In progress. Just beginning the implementation so don't expect it to be complete for a while.

You are welcome to help out where you can, just make a new fork of this fork and make a new branch. Issues are also welcome.
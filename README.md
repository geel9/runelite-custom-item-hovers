# Custom Item Hovers
This plugin displays arbitrary custom hover text over items.

# Major Features
- **Create your own hovers** to display over items in your inventory, equipment, or bank
- **A rich expression engine**
  - Use variables (such as `fishing_level`, `player_x`, `player_hp`, `qty`, `price`, etc.) and functions (such as `inv_qty()`, `bank_qty()`, etc.) to create dynamic hovers
    - Supports ternary! Wow!
- **Conditional hovers**
  - Only display a hover if certain conditions are met
    - This uses the aforementioned expression engine, so it _also_ supports ternary! The only thing you need!
- **Hot Reload**
  - Hovers are reloaded automatically when you save your hover files
- **Item Data Tables**
  - Define variables on items, which are then usable by your hover expressions

# Examples
_Click any image to go to the example hover file_

[![Bone Shards](/docs/img/examples/example1.png)](/examples/bone_shards.json)

# Changelog

## v1.3.0 - May 20, 2025

- Added a number of [new Functions](docs/Functions.md) to the expression engine
- Added a number of [new Variables](docs/Variables.md) to the expression engine
- Added support for [Conditional Hovers](docs/Conditions.md) to only display hovers if a condition is met
- Expression engine base improvements
    - Added support for ternary expressions (`condition ? ifTrue : ifFalse`)
    - EG `${prayer_level_real == 99 ? 'You are a god' : 'You are a mortal'}`
- _Nerd shit_
  - Refactored expression engine for simplicity
  - Added static evaluation to expression engine
    - During hover file parsing, we statically evaluate expressions as much as possible. 
    - Some expressions can be completely statically evaluated, meaning we never have to process them at runtime.
    - All other expressions will have all constant operations statically evaluated, improving runtime expression evaluation performance
      - EG, `player_level * (99 + pow(2, 2))` will statically evaluate to `player_level * 103`

# Setting up the plugin
Upon first launch, the plugin will create a `customitemhovers` folder in your `.runelite` directory, which is usually located in your `HOME` directory.

For example, on Windows, this might be `C:/Users/{username}/.runelite/customitemhovers`

Place your hover files in this folder. To start, you can copy over the files in the `examples` folder of this repo.

## Easily Navigating to your Hover File Directory

To quickly open the directory containing your hover files, simply execute the configurable chat command to do so.

By default, this is `::openhoverdir`, but this can be changed via plugin config.

# Creating Custom Hovers

See [docs/HoverFiles.md](docs/HoverFiles.md) to learn how to create your own hovers.
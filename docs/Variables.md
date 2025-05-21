# Variables

## Overview

This plugin's expression engine provides a number of variables.

You can define your own variables on an item-by-item basis by using [Item Tables](/docs/ItemTables.md), though this only supports constant values.

## Variables

### Item-Specific Variables
These variables are always specific to the item being hovered over.

|Variable|Type|Description|Example Usage|
|----|----|----|----|
|`id`|Number|The integer ID of the item, e.g., `995` for coins|
|`name`|Text|The name of the item, e.g. `Coins`|
|`qty`|Number|The number of items in the stack being hovered over|Display total XP value of a stack 
|`value`|Number|The in-game value (used to determine high/low alch values) of the item|Display a highlight only if an item is above 1m in value
|`high_alch`|Number|The high-alch value of the item. Equivalent to `value * 0.6`.|


### Player-Specific Variables
These variables are always specific to your own player.

#### Skill Variables
These variables are specific to your own player's skills.

Replace `{name}` with one of the following skill names:

- `agility`
- `attack`
- `construction`
- `cooking`
- `crafting`
- `defence`
- `fletching`
- `fishing`
- `firemaking`
- `farming`
- `herblore`
- `hitpoints`
- `hunter`
- `magic`
- `mining`
- `prayer`
- `ranged`
- `runecraft`
- `slayer`
- `smithing`
- `strength`
- `thieving`
- `woodcutting`


EG, `hitpoints_level` or `woodcutting_xp`

|Variable|Type|Description|Example Usage|
|----|----|----|----|
|`{name}_level`|Number|The current level (including boosts) of the skill|Compute net boosts for a potion|
|`{name}_level_real`|Number|The current level (without boosts) of the skill|Display different hover text depending on whether user has the needed skill level|
|`{name}_level_xp`|Number|The total experience in the skill||


#### All Other Player Variables

|Variable|Type|Description|Example Usage|
|----|----|----|----|
|`player_hp`|Number|Your current hitpoints|Display a warning if a Moons of Peril food item would be wasted|
|`player_prayer`|Number|Your current prayer points|Display a warning if a prayer potion would waste prayer|
|`player_x`|Number|The player's current world map X coordinate|Display a hover only if the user is within a certain area|
|`player_y`|Number|The player's current world map Y coordinate|
|`player_region`|Number|The player's current world map _region identifier_|
|`player_region_x`|Number|The player's current world map region's X coordinate|
|`player_region_y`|Number|The player's current world map region's Y coordinate|


## Functions

*Note: Functions which return a `Boolean` really just return a `Number`: `1` if true, `0` if false.*

### Inventory Functions


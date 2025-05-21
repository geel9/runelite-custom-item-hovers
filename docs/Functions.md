# Functions

## Overview

This plugin's expression engine provides a number of functions.

It is not possible to define your own functions.

*Note: Functions which return a `Boolean` really just return a `Number`: `1` if true, `0` if false.*

## Inventory Functions

All of the Inventory Functions exist for the following three "inventories":
- `inv`
    - The player's current inventory
- `equipment`
    - The player's equipped items
- `bank`
    - The player's bank
    - **Only works if the bank is open**

Simply replace `{name}` in the function name with an above inventory name. For example, `inv_qty('Coins') > 1000` or `equipment_contains('Dragon scimitar')`

### `{name}_contains(itemIdOrName, ...) -> Boolean`

Can pass in any number of item IDs (Numbers) or names (Strings), or just one.

Returns `1` if inventory, equipment, or bank contains any of the specified items.

**Examples**:

- `inv_contains(995)` returns `1` if the player is holding any Coins.
- `equipment_contains('Dragon scimitar', 'Dragon longsword')` returns `1` if the player is wielding _either_ a Dragon scimitar or a Dragon longsword.

### `{name}_qty(itemIdOrName, ...) -> Number`

Can pass in any number of item IDs (Numbers) or names (Strings), or just one.

Returns the total quantity of the specified items in the inventory, equipment, or bank.

If multiple items are specified, the sum of the quantity of all items is returned.

**Examples**:

- `inv_qty(995)` returns the number of Coins in the player's inventory.
- `bank_qty('Grimy harralander', 'Harralander', 'Harralander potion (unf)')` returns the total number of "unmade Harralander potions" the player has in their bank.
- `equipment_qty('Adamant arrow', 'Rune arrow', 'Dragon arrow')` returns the number of arrows above a certain metal type which the player has equipped.


## Varbit / Varplayer functions

`Varbits` are pieces of data that are stored in the game client. They are used to track various game states, such as quest progress, achievement diary completion, etc.

`Varplayers` are very similar to varbits, but to keep things simple: some data is stored in varbits; some data is stored in varplayers. Most of the time, you want varbits.

These functions allow you to query their values.


### `varbit(varbitId) -> Number`

Returns the value of the specified varbit.

[The OSRS Wiki](https://oldschool.runescape.wiki/w/Property:Variable_name?filter=&from=&limit=500&offset=0&until=) has an excellent list of many varbits.

**Examples**:
- `varbit(4464)` returns `1` if the player has completed the Falador Hard Diary
- `varbit(2326)` returns the current progress of the Fairy Tale II quest
  - `100` indicates the quest is complete
- `varbit(13131)` returns `1` if PK skull prevention is enabled; `0` if disabled

### `varplayer(varplayerId) -> Number`

Returns the value of the specified varplayer.

### `varcint(varcintId) -> Number`

Returns the value of the specified varcint.

A `varcint` is a client-only variable, such as the camera's current zoom level.

### `varcstr(varcstrId) -> String`

Returns the value of the specified varcstr.

A `varcstr` is a client-only variable, such as the player's current chat message.


## Math Functions

### `abs(number) -> Number`

Returns the absolute value of the specified number.

### `pow(base, exponent) -> Number`

Returns the value of the base raised to the power of the exponent.

For example, `pow(2, 3)` returns `8` (2^3).

### `ceil(number) -> Number`

Returns the smallest integer greater than or equal to the specified number.

### `floor(number) -> Number`

Returns the largest integer less than or equal to the specified number.

### `max(number1, number2, ...) -> Number`

Can pass in any amount of numbers.

Returns the largest number of all the specified numbers.

### `min(number1, number2, ...) -> Number`

Can pass in any amount of numbers.

Returns the smallest number of all the specified numbers.



## String Functions

### `len(string) -> Number`

Returns the length of the specified string.

**Examples**:
- `len('Hello world!')` returns `12`
- `len(name)` returns the length of name of the item currently being hovered over

### `contains(haystack, needle) -> Boolean`

Returns `1` if `needle` is contained within `haystack`; `0` otherwise

**Examples**:
- `contains('Hello world!', 'world')` returns `1`

### `red(text) -> String`

Returns `text` wrapped in color tags to make it red.

**Examples**:
- `red('Hello world!')` returns `<col=ff0000>Hello world!</col>`

### `green(text) -> String`

Returns `text` wrapped in color tags to make it green.

**Examples**:
- `green('Hello world!')` returns `<col=00ff00>Hello world!</col>`

### `blue(text) -> String`

Returns `text` wrapped in color tags to make it blue.

**Examples**:
- `blue('Hello world!')` returns `<col=0000ff>Hello world!</col>`

### `yellow(text) -> String`

Returns `text` wrapped in color tags to make it yellow.

**Examples**:
- `yellow('Hello world!')` returns `<col=ffff00>Hello world!</col>`


### `rgb(r, g, b, text) -> String`

Returns `text` wrapped in color tags to make it the specified RGB color.

**Examples**:
- `rgb(255, 0, 0, 'Hello world!')` returns `<col=ff0000>Hello world!</col>`
- `rgb(0, 255, 0, 'Hello world!')` returns `<col=00ff00>Hello world!</col>`
- `rgb(0, 0, 255, 'Hello world!')` returns `<col=0000ff>Hello world!</col>`
- `rgb(255, 255, 255, 'Hello world!')` returns `<col=ffffff>Hello world!</col>`

### `redStart() -> String`

Returns the start of a red text tag (`<col=ff0000>`).

**Examples**:
- `redStart() + 'Hello world!'` returns `<col=ff0000>Hello world!`
- `Your fishing level is ${fishing_level == 99 ? greenStart() : redStart()}${fishing_level}${colorEnd()}`
  - Returns `Your fishing level is <col=00ff00>99</col>` if the fishing level is 99 (green text)
  - Returns `Your fishing level is <col=ff0000>99</col>` if the fishing level is not 99 (red text)

### `greenStart() -> String`

Returns the start of a green text tag (`<col=00ff00>`).

### `blueStart() -> String`

Returns the start of a blue text tag (`<col=0000ff>`).

### `yellowStart() -> String`

Returns the start of a yellow text tag (`<col=ffff00>`).

### `rgbStart(r, g, b) -> String`

Returns the start of an RGB text tag (`<col={RRGGBB}>`).

**Examples**:
- `rgbStart(255, 0, 0)` returns `<col=ff0000>`
- `rgbStart(0, 255, 0)` returns `<col=00ff00>`
- `rgbStart(0, 0, 255)` returns `<col=0000ff>`
- `rgbStart(255, 255, 255)` returns `<col=ffffff>`
- `Your fishing level is ${fishing_level == 99 ? rgbStart(0, 255, 0) : rgbStart(255, 0, 0)}${fishing_level}${colorEnd()}`
  - Returns `Your fishing level is <col=00ff00>99</col>` if the fishing level is 99 (green text)
  - Returns `Your fishing level is <col=ff0000>99</col>` if the fishing level is not 99 (red text)

### `colorEnd() -> String`

Returns the end of a color text tag (`</col>`).
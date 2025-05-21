# Item Data Tables
## The Problem
***Note: Read the main guide first to gain some context!***

In the [main walkthrough](/docs/HoverFiles.md), we looked at an example hover file for Ensouled Heads, working off of [ensouled_heads_no_table.json](/examples/ensouled_heads_no_table.json).

For the purpose of demonstrating the problem, I will now paste the _entire file_ here:

```json
{
  "is_hover_map": "absolutely",
  "hovers": [
    {
      "items": ["Ensouled goblin head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 130 (${qty * 130})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled monkey head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 182 (${qty * 182})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled imp head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 286 (${qty * 286})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled minotaur head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 364 (${qty * 364})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled scorpion head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 454 (${qty * 454})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled bear head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 480 (${qty * 480})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled unicorn head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 494 (${qty * 494})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },


    {
      "items": ["Ensouled dog head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 520 (${qty * 520})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled chaos druid head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 584 (${qty * 584})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled giant head"], 
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 650 (${qty * 650})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled ogre head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 716 (${qty * 716})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled elf head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 754 (${qty * 754})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled troll head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 780 (${qty * 780})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled horror head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 832 (${qty * 832})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },


    {
      "items": ["Ensouled kalphite head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: 884 (${qty * 884})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },
    {
      "items": ["Ensouled dagannoth head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: 936 (${qty * 936})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },
    {
      "items": ["Ensouled bloodveld head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: 1,040 (${qty * 1040})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },
    {
      "items": ["Ensouled tzhaar head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: 1,104 (${qty * 1104})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },
    {
      "items": ["Ensouled demon head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: 1,170 (${qty * 1170})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },


    {
      "items": ["Ensouled aviansie head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Master (Level 90)",
          "XP:",
          "  \u2022Prayer: 1,234 (${qty * 1234})",
          "  \u2022Magic: 170 (${qty * 170})",
          "Runes:",
          "  \u2022Nature (4x) (${qty * 4})",
          "  \u2022Blood (2x) (${qty * 2})",
          "  \u2022Soul (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled abyssal head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Master (Level 90)",
          "XP:",
          "  \u2022Prayer: 1,300 (${qty * 1300})",
          "  \u2022Magic: 170 (${qty * 170})",
          "Runes:",
          "  \u2022Nature (4x) (${qty * 4})",
          "  \u2022Blood (2x) (${qty * 2})",
          "  \u2022Soul (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items": ["Ensouled dragon head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Master (Level 90)",
          "XP:",
          "  \u2022Prayer: 1,560 (${qty * 1560})",
          "  \u2022Magic: 170 (${qty * 170})",
          "Runes:",
          "  \u2022Nature (4x) (${qty * 4})",
          "  \u2022Blood (2x) (${qty * 2})",
          "  \u2022Soul (4x) (${qty * 4})"
        ]
      ]
    }
  ]
}
```

## The Solution

The above file contains a huge amount of bloat. How can we simplify it?

### 1. Collapse the Hovers
First, we can observe that, although there are a lot of different items to make hovers for, they each fall into one of four categories -- that being which tier of reanimation spell they use.

So, we can use Regex to target all the items of each reanimation tier at once, and give them the same hover!

```json
{
  "is_hover_map": "absolutely",
  "hovers": [
    {
      "items_regex": ["Ensouled (goblin|monkey|imp|minotaur|scorpion|bear|unicorn) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: 130 (${qty * 130})",
          "  \u2022Magic: 32 (${qty * 32})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items_regex": ["Ensouled (dog|chaos druid|giant|ogre|elf|troll|horror) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: 520 (${qty * 520})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items_regex": ["Ensouled (kalphite|dagannoth|bloodveld|tzhaar|demon) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: 884 (${qty * 884})",
          "  \u2022Magic: 80 (${qty * 80})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },
    {
      "items_regex": ["Ensouled (aviansie|abyssal|dragon) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Master (Level 90)",
          "XP:",
          "  \u2022Prayer: 1,234 (${qty * 1234})",
          "  \u2022Magic: 170 (${qty * 170})",
          "Runes:",
          "  \u2022Nature (4x) (${qty * 4})",
          "  \u2022Blood (2x) (${qty * 2})",
          "  \u2022Soul (4x) (${qty * 4})"
        ]
      ]
    }
  ]
}
```

### 2. Introduce Tables

So, we've got a _significantly_ shorter hover file now -- only four hovers defined; one for each category of item.

This would be perfect, except for one thing: the Prayer XP given by each item is different.
With things as they are, we'll be showing incorrect information. How do we fix this while keeping our simplified hovers?

Let's create an item data table.

We're going to create the following file, named `ensouled_heads_table.json` in `{YOUR_HOVER_DIR}/tables`. All item tables must go in `/tables`.

```json
{
  "name": "ensouled_heads",
  "is_table_file": "absolutely",
  "items": {
    "Ensouled goblin head": {
      "reanimation_prayer_xp": "130"
    },
    "Ensouled monkey head": {
      "reanimation_prayer_xp": "182"
    },
    "Ensouled imp head": {
      "reanimation_prayer_xp": "286"
    },
    "Ensouled minotaur head": {
      "reanimation_prayer_xp": "364"
    },
    "Ensouled scorpion head": {
      "reanimation_prayer_xp": "454"
    },
    "Ensouled bear head": {
      "reanimation_prayer_xp": "480"
    },
    "Ensouled unicorn head": {
      "reanimation_prayer_xp": "494"
    },

    "Ensouled dog head": {
      "reanimation_prayer_xp": "520"
    },
    "Ensouled chaos druid head": {
      "reanimation_prayer_xp": "584"
    },
    "Ensouled giant head": {
      "reanimation_prayer_xp": "650"
    },
    "Ensouled ogre head": {
      "reanimation_prayer_xp": "716"
    },
    "Ensouled elf head": {
      "reanimation_prayer_xp": "754"
    },
    "Ensouled troll head": {
      "reanimation_prayer_xp": "780"
    },
    "Ensouled horror head": {
      "reanimation_prayer_xp": "832"
    },

    "Ensouled kalphite head": {
      "reanimation_prayer_xp": "884"
    },
    "Ensouled dagannoth head": {
      "reanimation_prayer_xp": "936"
    },
    "Ensouled bloodveld head": {
      "reanimation_prayer_xp": "1040"
    },
    "Ensouled tzhaar head": {
      "reanimation_prayer_xp": "1104"
    },
    "Ensouled demon head": {
      "reanimation_prayer_xp": "1170"
    },

    "Ensouled aviansie head": {
      "reanimation_prayer_xp": "1234"
    },
    "Ensouled abyssal head": {
      "reanimation_prayer_xp": "1300"
    },
    "Ensouled dragon head": {
      "reanimation_prayer_xp": "1560"
    }
  }
}
```

Perfect! Now, we'll have set the `reanimation_prayer_xp` variable for all the items in the list above. We can now use it in our hovers!

### 3. Use Variables

Now that we have the item data table set up, let's modify the hover file to use the new variable:

```json
{
  "is_hover_map": "absolutely",
  "hovers": [
    {
      "items_regex": ["Ensouled (goblin|monkey|imp|minotaur|scorpion|bear|unicorn) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Basic (Level 16)",
          "XP:",
          "  \u2022Prayer: ${reanimation_prayer_xp} (${reanimation_prayer_xp * qty})",
          "  \u2022Magic: 32 (${32 * qty})",
          "Runes:",
          "  \u2022Nature (2x) (${qty * 2})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items_regex": ["Ensouled (dog|chaos druid|giant|ogre|elf|troll|horror) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Adept (Level 41)",
          "XP:",
          "  \u2022Prayer: ${reanimation_prayer_xp} (${reanimation_prayer_xp * qty})",
          "  \u2022Magic: 80 (${80 * qty})",
          "Runes:",
          "  \u2022Soul (1x) (${qty})",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Body (4x) (${qty * 4})"
        ]
      ]
    },
    {
      "items_regex": ["Ensouled (kalphite|dagannoth|bloodveld|tzhaar|demon) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Expert (Level 72)",
          "XP:",
          "  \u2022Prayer: ${reanimation_prayer_xp} (${reanimation_prayer_xp * qty})",
          "  \u2022Magic: 80 (${80 * qty})",
          "Runes:",
          "  \u2022Nature (3x) (${qty * 3})",
          "  \u2022Blood (1x) (${qty})",
          "  \u2022Soul (2x) (${qty * 2})"
        ]
      ]
    },
    {
      "items_regex": ["Ensouled (aviansie|abyssal|dragon) head"],
      "hovers": [
        [
          "<col=3498db>---Reanimation---</col>",
          "Master (Level 90)",
          "XP:",
          "  \u2022Prayer: ${reanimation_prayer_xp} (${reanimation_prayer_xp * qty})",
          "  \u2022Magic: 170 (${170 * qty})",
          "Runes:",
          "  \u2022Nature (4x) (${qty * 4})",
          "  \u2022Blood (2x) (${qty * 2})",
          "  \u2022Soul (4x) (${qty * 4})"
        ]
      ]
    }
  ]
}
```

And that's it! You're done!

## Expressions As Variables

As of right now, **variable values cannot be expressions**. This is to keep the expression engine simple.


## Examples

- [Hunter Meats HoverFile](examples/hunter_meats.json) + [Hunter Meats Table](examples/tables/hunter_meats_table.json)
  - Very simple table; just sets the number of whistle charges each Hunter Meat grants as a variable on said item
  - Used in the hover file to show how many charges are in a player's inventory
- [Spicy Stew HoverFile](examples/spicy_stews.json) + [Spicy Stew Table](examples/tables/spicy_stews.json)
  - More complex table; computes the number of stews a player can make from the number of spices in a stack / their inventory

## FAQ

- How can I set variables on multiple items with one entry in a table?
    - Not well supported right now. You can use `$also` -- see example in `examples/tables/hunter_meats_table.json` or `examples/tables/spicy_stews.json`.
- How do I make variables in a table available to my hovers?
    - Variables are always available to all hovers. You don't need to do anything to link a hover file and a table file.
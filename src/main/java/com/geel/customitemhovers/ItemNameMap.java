package com.geel.customitemhovers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Stores an (item name -> item id) and (item id -> item name) mapping for all items in game
 *
 * In this context, "item name" is really the item's name in the enum of `ItemID`.
 * EG, instead of "Pineapple Sapling", we use "PINEAPPLE_SAPLING", as that's the name
 * of the entry in the `ItemID` enum for the Pineapple Sapling item.
 */
@Slf4j
public class ItemNameMap {
    //Map of (item name -> item id)
    private final static Map<String, Integer> itemNameToID = new HashMap<>();

    //Map of (item id -> item name)
    private final static Map<Integer, String> itemIDToName = new HashMap<>();

    /**
     * Returns the enum name of the given item ID
     */
    public static String GetItemName(int itemID) {
        prepareMaps();

        return itemIDToName.getOrDefault(itemID, null);
    }

    /**
     * Returns the item ID which corresponds to the given string name
     *
     * @param itemName The name of an entry in the `ItemID` class, eg, "PINEAPPLE_SAPLING"
     */
    public static int GetItemID(String itemName) {prepareMaps();
        prepareMaps();

        return itemNameToID.getOrDefault(itemName, -1);
    }


    /**
     * Returns all IDs of items whose (enum) names match the given regex
     * <p>
     * Here, "name" means "entry in `ItemID`", not the proper english name.
     * For example, "PINEAPPLE_SAPLING" is the 'name' of the "Pineapple sapling" item.
     *
     * @param itemNameRegex A regex string to match item names against
     * @return A list of all item IDs whose enum names match the given regex
     */
    public static ArrayList<Integer> GetItemIDsRegex(String itemNameRegex) {
        prepareMaps();

        ArrayList<Integer> ret = new ArrayList<>(1);
        Pattern finder = Pattern.compile(itemNameRegex);

        if (finder == null) {
            return ret;
        }

        for (String entry : itemNameToID.keySet()) {
            if (!finder.matcher(entry).matches())
                continue;

            ret.add(itemNameToID.get(entry));
        }

        return ret;
    }


    /**
     * Prepare `itemNameToID` and `itemIDToName`
     */
    private static void prepareMaps() {
        //Don't do anything if already prepared
        if (itemNameToID.size() > 0 && itemIDToName.size() > 0)
            return;

        //Clear just to be safe
        itemNameToID.clear();
        itemIDToName.clear();

        //Get all enum entries of `ItemID`
        Field[] declaredFields = ItemID.class.getDeclaredFields();
        for (Field field : declaredFields) {
            //Filter out anything which isn't an integer
            if (!field.getType().getTypeName().equals("int"))
                continue;

            //Filter out anything that isn't a static variable (ie, enum entry)
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                continue;

            String itemName = field.getName();

            //Filter out anything that isn't a valid item name (valid item names are all-uppercase)
            if (!isValidItemName(itemName)) {
                log.error("[CUSTOM_ITEM_HOVERS]: " + itemName + " is an invalid item name");
                continue;
            }

            int itemID = -1;
            try {
                itemID = field.getInt(null);
            } catch (IllegalAccessException e) {
                continue;
            }

            if (itemID < 0) {
                log.error("[CUSTOM_ITEM_HOVERS]: " + itemName + " has invalid item ID " + itemID);
                continue;
            }

            if (itemNameToID.containsKey(itemName)) {
                log.error("[CUSTOM_ITEM_HOVERS]: " + itemName + " is a duplicate item ID " + itemID);
                continue;
            }

            itemNameToID.put(itemName, itemID);
            itemIDToName.put(itemID, itemName);
        }
    }

    /**
     * Returns whether the given item name is a valid item name
     * <p>
     * This simply checks for the existence of any lowercase characters, and returns false if it finds any.
     * An item name should be the name of an entry in `ItemID`, which are all uppercase.
     */
    private static boolean isValidItemName(String itemName) {
        for (int i = 0; i < itemName.length(); i++) {
            if (Character.isLowerCase(itemName.charAt(i)))
                return false;
        }

        return true;
    }
}

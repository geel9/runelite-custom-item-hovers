package com.geel.customitemhovers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Stores an (item name -> [item ids]) and (item id -> item name) mapping for all items in game
 */
@Slf4j
public class ItemNameMap {
    //Map of (item name -> [item id, ...])
    private final static Map<String, ArrayList<Integer>> itemNameToIDs = new HashMap<>();

    /**
     * Prepare map of item names to IDs
     *
     * @param client      Reference to RL Client; used to fetch total number of items
     * @param itemManager Reference to ItemManager; used to fetch item compositions
     */
    public static void PrepareMap(Client client, ItemManager itemManager) {
        //Don't do anything if already prepared
        if (itemNameToIDs.size() > 0)
            return;

        //Clear just to be safe
        itemNameToIDs.clear();

        Set<Integer> processedIDs = new HashSet<>();

        for (int i = 0; i < client.getItemCount(); i++) {
            int canonicalID = itemManager.canonicalize(i);

            if(processedIDs.contains(canonicalID))
                continue;

            processedIDs.add(canonicalID);

            ItemComposition comp = itemManager.getItemComposition(canonicalID);
            String itemName = comp.getName();

            if(itemName.toLowerCase().equals("null")) {
                log.error("Item ID " + canonicalID + " has a null name");
                continue;
            }

            //Create list for item name if it doesn't exist
            if (!itemNameToIDs.containsKey(itemName)) {
                itemNameToIDs.put(itemName, new ArrayList<>(1));
            }

            itemNameToIDs.get(itemName).add(canonicalID);
        }
    }

    /**
     * Returns all item IDs which correspond to the given string name
     *
     * @param itemName The name of an entry in the `ItemID` class, eg, "PINEAPPLE_SAPLING"
     */
    public static int[] GetItemIDs(String itemName) {
        ArrayList<Integer> itemIDs = itemNameToIDs.getOrDefault(itemName, new ArrayList<>(0));

        //The lack of a sane toArray() function is going to kill me
        int[] ret = new int[itemIDs.size()];

        for (int i = 0; i < itemIDs.size(); i++) {
            ret[i] = itemIDs.get(i);
        }

        return ret;
    }


    /**
     * Returns all IDs of items whose names match the given regex
     *
     * @param itemNameRegex A regex string to match item names against
     * @return A list of all item IDs whose names match the given regex
     */
    public static ArrayList<Integer> GetItemIDsRegex(String itemNameRegex) {
        ArrayList<Integer> ret = new ArrayList<>(1);
        Pattern finder = Pattern.compile(itemNameRegex);

        if (finder == null) {
            return ret;
        }

        for (String entry : itemNameToIDs.keySet()) {
            if (!finder.matcher(entry).matches())
                continue;

            for (int itemID : itemNameToIDs.get(entry)) {
                //O(n^2), sue me (don't)
                if (!ret.contains(itemID))
                    ret.add(itemID);
            }
        }

        return ret;
    }
}

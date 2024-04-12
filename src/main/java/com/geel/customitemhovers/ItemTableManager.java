package com.geel.customitemhovers;

import com.geel.customitemhovers.models.itemtables.ItemTableFile;
import com.geel.customitemhovers.models.itemtables.ItemTable;
import com.geel.customitemhovers.models.itemtables.ItemTableCollection;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Singleton
@Slf4j
/**
 * Manages ItemTables for the plugin.
 *
 * Responsible for loading, parsing, storing, and providing ItemTables from JSON files.
 */
public class ItemTableManager {
    @Inject
    private Gson gson;

    @Inject
    private ItemManager itemManager;

    private HashMap<String, ItemTableFile> parsedFiles = new HashMap<>();
    private HashMap<String, ItemTableCollection> itemMap = new HashMap<>();

    /**
     * Clear the in-memory ItemTable database and load it from scratch
     */
    public void reparseItemDatabase(Path tablePath) {
        if (!Files.isDirectory(tablePath) || !Files.isReadable(tablePath)) {
            return;
        }

        HashMap<String, ItemTableFile> newParsedFiles = new HashMap<>();
        HashMap<String, ItemTableCollection> newItemMap = new HashMap<>();

        try {
            Stream fileStream = Files.list(tablePath);
            for (Iterator it = fileStream.iterator(); it.hasNext(); ) {
                Path p = (Path) it.next();

                //Ensure it's a regular readable file
                if (!Files.isRegularFile(p) || !Files.isReadable(p))
                    continue;

                //Ensure it's a json file
                if (!p.toString().endsWith(".json"))
                    continue;

                ItemTableFile file = parseTableFile(p);

                //Must absolutely be a hover file
                if (file == null || file.IsTableFile == null || !file.IsTableFile.equals("absolutely"))
                    continue;

                // Process file
                newParsedFiles.put(file.Name, file);

                for (String baseItemName : file.Items.keySet()) {
                    ItemTable itemTable = file.Items.get(baseItemName);

                    // Set properties on TableItem
                    itemTable.setParentFile(file);
                    itemTable.setItemName(baseItemName);

                    String[] itemNames = itemTable.getAllNames();
                    for(String itemName : itemNames) {
                        // Store an (itemName -> collection) reference in newItemMap
                        if (!newItemMap.containsKey(itemName)) {
                            newItemMap.put(itemName, new ItemTableCollection());
                        }

                        // Put TableItem into itemMapEntry
                        ItemTableCollection itemMapEntry = newItemMap.get(itemName);
                        itemMapEntry.getItemTables().put(file.Name, itemTable);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }

        parsedFiles.clear();
        itemMap.clear();

        parsedFiles.putAll(newParsedFiles);
        itemMap.putAll(newItemMap);
    }

    /**
     * Get the ItemCollection associated with the given item name
     */
    public ItemTableCollection getCollectionForItem(String itemName)
    {
        return itemMap.getOrDefault(itemName, new ItemTableCollection());
    }

    /**
     * Get the base ItemTable for the given item.
     *
     * This is a special table, available for every item, which defines certain variables -- eg "id", "qty", etc.
     */
    public ItemTable getBaseTableForItem(Item item) {
        ItemComposition itemComposition = itemManager.getItemComposition(item.getId());

        ItemTable baseTable = new ItemTable();
        baseTable.put("id", String.valueOf(item.getId()));
        baseTable.put("qty", String.valueOf(item.getQuantity()));
        baseTable.put("value", String.valueOf(itemComposition.getPrice()));
        baseTable.put("high_alch", String.valueOf(itemComposition.getHaPrice()));

        return baseTable;
    }

    /**
     * Parse a file from JSON
     * @param tableFile
     * @return
     */
    private ItemTableFile parseTableFile(Path tableFile) {
        try {
            byte[] fileBytes = Files.readAllBytes(tableFile);
            String fileString = new String(fileBytes);

            return gson.fromJson(fileString, ItemTableFile.class);
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }
}

package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.EvaluationUtil;
import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.models.VariableType;
import com.geel.customitemhovers.models.itemtables.ParsedItemTableEntry;
import com.geel.customitemhovers.models.itemtables.ItemTableCollection;
import com.geel.customitemhovers.models.itemtables.ItemTableFile;
import com.geel.customitemhovers.models.itemtables.RawItemTableEntry;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
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
                    RawItemTableEntry rawItemTable = file.Items.get(baseItemName);
                    ParsedItemTableEntry parsedItemTable = this.processItemTableEntry(rawItemTable);

                    String[] itemNames = rawItemTable.getAllNames(baseItemName);
                    for (String itemName : itemNames) {
                        // Store an (itemName -> collection) reference in newItemMap
                        if (!newItemMap.containsKey(itemName)) {
                            newItemMap.put(itemName, new ItemTableCollection());
                        }

                        // Put TableItem into itemMapEntry
                        ItemTableCollection itemMapEntry = newItemMap.get(itemName);
                        itemMapEntry.getItemTables().put(file.Name, parsedItemTable);
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

    private ParsedItemTableEntry processItemTableEntry(RawItemTableEntry entry) {
        ParsedItemTableEntry ret = new ParsedItemTableEntry();

        // Parse out each entry into a Variable.
//        Set<String> expressionKeys = new HashSet<>(); TODO: Enable support for expressions in variables
        for (String name : entry.keySet()) {
            String value = entry.get(name);

            // Skip special entries
            if (name.equals("$also")) continue; //teehee

            // Add parsed variable entry
            Variable parsed = Variable.fromExpressionOrConstantString(value);
            ret.put(name, parsed);

            // TODO: Enable support for expressions in variables
//            if (parsed.getType() == VariableType.EXPRESSION) {
//                expressionKeys.add(name);
//            }
        }

        // If any variables were expressions, they were compiled in the previous step.
        // Now, we need to statically evaluate the expressions as much as possible.
        // In the ideal case, a variable will evaluate to a constant value, and we can just use that,
        // instead of re-evaluating the expression every damn time.
        // TODO: Enable support for expressions in variables
        /*
        for (String name : expressionKeys) {
            Variable variable = ret.resolveVariable(name);

            // Create a new context with the parsed item table entry itself as the variable provider
            // That way, expressions can use any variable defined in the same entry.
            // Genius. Might need to be removed.
            ExecutionContext context = new ExecutionContext(functionProvider, ret);

            // Try to statically evaluate the expression
            try {
                CompiledExpression evaluated = EvaluationUtil.EvaluateStatically(variable.getExpression(), context);
                variable = new Variable(evaluated);
            } catch (Exception e) {
                // Failed to evaluate. Fuck it. Variable doesn't get to exist now.
                variable = null;
            }

            // Update the stored variable
            ret.put(name, variable);
        }
        */

        return ret;
    }

    /**
     * Get the ItemCollection associated with the given item name
     */
    public ItemTableCollection getCollectionForItem(String itemName) {
        return itemMap.getOrDefault(itemName, new ItemTableCollection());
    }

    /**
     * Get the base ItemTable for the given item/player combo.
     * <p>
     * This is a special table, available for every item, which defines certain variables -- eg "id", "qty", etc.
     */
    public ParsedItemTableEntry getBaseTableForItemAndPlayer(Item item, Client client) {
        // TODO: It's silly to construct this every time. So many heap allocations :(
        //       Instead, we should just compute these values on the fly when an expression actually needs one.
        ItemComposition itemComposition = itemManager.getItemComposition(item.getId());

        ParsedItemTableEntry baseTable = new ParsedItemTableEntry();
        baseTable.put("id", Variable.fromConstant(item.getId()));
        baseTable.put("name", Variable.fromConstant(itemComposition.getName()));
        baseTable.put("qty", Variable.fromConstant(item.getQuantity()));
        baseTable.put("value", Variable.fromConstant(itemComposition.getPrice()));
        baseTable.put("high_alch", Variable.fromConstant(itemComposition.getHaPrice()));

        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            return baseTable;
        }

        // Prepare player variables
        // TODO: move this somewhere else
        Player player = client.getLocalPlayer();

        // Skill levels/xp
        for (Skill skill : Skill.values()) {
            String skillName = skill.getName().toLowerCase(Locale.ROOT);
            baseTable.put(skillName + "_level", Variable.fromConstant(client.getBoostedSkillLevel(skill)));
            baseTable.put(skillName + "_level_real", Variable.fromConstant(client.getRealSkillLevel(skill)));
            baseTable.put(skillName + "_xp", Variable.fromConstant(client.getSkillExperience(skill)));
        }

        // World location
        baseTable.put("player_x", Variable.fromConstant(player.getWorldLocation().getX()));
        baseTable.put("player_y", Variable.fromConstant(player.getWorldLocation().getY()));
        baseTable.put("player_plane", Variable.fromConstant(player.getWorldLocation().getPlane()));
        baseTable.put("player_region", Variable.fromConstant(player.getWorldLocation().getRegionID()));
        baseTable.put("player_region_x", Variable.fromConstant(player.getWorldLocation().getRegionX()));
        baseTable.put("player_region_y", Variable.fromConstant(player.getWorldLocation().getRegionY()));

        // Health/prayer
        baseTable.put("player_hp", Variable.fromConstant(client.getBoostedSkillLevel(Skill.HITPOINTS)));
        baseTable.put("player_prayer", Variable.fromConstant(client.getBoostedSkillLevel(Skill.PRAYER)));

        return baseTable;
    }

    /**
     * Parse a file from JSON
     *
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

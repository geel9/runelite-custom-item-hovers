package com.geel.customitemhovers;

import javax.inject.Inject;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.EvaluationUtil;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.providers.INamespace;
import com.geel.customitemhovers.models.hovers.HoverFile;
import com.geel.customitemhovers.models.hovers.HoverDef;
import com.geel.customitemhovers.models.hovers.ParsedHover;
import com.geel.customitemhovers.models.itemtables.ParsedItemTableEntry;
import com.geel.customitemhovers.models.itemtables.ItemTableCollection;
import com.geel.customitemhovers.namespaces.RootNamespace;
import com.geel.customitemhovers.providers.ItemStackVariableProvider;
import com.geel.customitemhovers.providers.PlayerVariableProvider;
import com.geel.customitemhovers.providers.RootFunctionProvider;
import com.geel.customitemhovers.providers.RootVariableProvider;
import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
@PluginDescriptor(
        name = "Custom Item Hovers",
        description = "Enables custom item hovers. Read github page.",
        tags = {"custom", "item", "hovers", "tooltips"},
        enabledByDefault = true
)
public class CustomItemHoversPlugin extends Plugin {
    private static final String PLUGIN_FOLDER_NAME = "customitemhovers";

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ItemManager itemManager;

    @Inject
    private CustomItemHoversOverlay overlay;

    @Inject
    private CustomItemHoversConfig config;

    @Inject
    private Gson gson;

    @Inject
    private HoverFileParser parser;

    @Inject
    private ItemTableManager itemTableManager;

    @Inject
    private RootFunctionProvider functionProvider;

    @Inject
    private PlayerVariableProvider playerVariableProvider;

    @Provides
    CustomItemHoversConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(CustomItemHoversConfig.class);
    }

    //Map between an Item ID and all of its associated HoverDefs.
    public Map<Integer, ArrayList<HoverDef>> hovers = new HashMap<>();

    // Watchers of the hover directory
    WatchService hoverWatcher;
    WatchKey hoverWatchKey;
    WatchKey tableWatchKey;

    @Override
    protected void startUp() throws Exception {
        prepareHoverFolder();

        //Invoke this on the client thread because `itemManager.canonicalize()` must be run in the client thread
        clientThread.invokeLater(() -> {
            prepareItemNameMap();
            prepareItemTables();
            prepareHoverMap();
            prepareHoverWatcher();
        });

        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        if (hoverWatcher != null)
            hoverWatcher.close();

        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged ev) {
        if (ev.getGroup().equals("customitemhovers") && ev.getKey().equals("hoverEnableHotReload")) {
            if (config.hoverEnableHotReload()) {
                clientThread.invoke(this::prepareHoverWatcher);
            } else {
                clientThread.invoke(this::stopHoverWatcher);
            }
        }
    }

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted) {
        if (!commandExecuted.getCommand().equals(config.openDirChatCommand())) {
            return;
        }

        Path hoverPath = getHoverPath();

        if (hoverPath == null) {
            return;
        }

        try {
            Desktop.getDesktop().open(hoverPath.toFile());
        } catch (Exception e) {
            log.error("Got exception opening hover folder", e);
        }
    }

    /**
     * Returns an array of hover texts that should be rendered for a given item
     */
    private Item lastItem = null;
    private String[] lastHovers = null;

    /**
     * Resets the cache of the item hover.
     *
     * This system seems badly coupled. I should refactor this. Blech.
     */
    public void resetHoveredItemCache() {
        lastHovers = null;
        lastItem = null;
    }

    /**
     *
     * For the given Item in a supported container, returns a String array, each representing
     * HTML that should be rendered in a hover box.
     *
     * Fully processes all expressions contained within hover texts.
     */
    public String[] getItemHovers(Item item) {
        // Cache hovers
        // TODO: FIXME: This prevents users from creating hovers which update dynamically without having to re-hover
        if (item.equals(lastItem)) {
            return lastHovers;
        }

        //Do a hot-reload if we should
        if (config.hoverEnableHotReload()) {
            if (hoverWatcherTriggered()) {
                prepareHoverMap();
            }

            if (tableWatcherTriggered()) {
                prepareItemTables();
            }
        }

        // Canonicalize the item and get its composition
        int itemID = itemManager.canonicalize(item.getId());
        ItemComposition comp = itemManager.getItemComposition(itemID);

        //If item's ID is not in `hovers`, it has no hovers.
        if (!hovers.containsKey(itemID))
            return new String[0];

        // Prepare expression execution context
        // TODO: cache ItemStackVariableProvider
        ItemTableCollection tableCollection = itemTableManager.getCollectionForItem(comp.getName());
        RootVariableProvider variableProvider = new RootVariableProvider(tableCollection, playerVariableProvider, new ItemStackVariableProvider(item, comp));
        INamespace namespace = new RootNamespace(functionProvider, variableProvider);
        ExecutionContext context = new ExecutionContext(namespace);

        //For each hover associated with this item, add its transformed text to the resultant array
        boolean obsoleteExpressionDetected = false;
        ArrayList<String> ret = new ArrayList<>();
        for (HoverDef hoverDef : hovers.get(itemID)) {
            // If the hoverdef has a condition, evaluate it first
            if(hoverDef.Condition != null) {
                Token conditionResult = EvaluationUtil.EvaluateAtRuntime(hoverDef.Condition, context);
                if(conditionResult.getNumericValue().intValue() == 0) {
                    continue;
                }
            }

            for (ParsedHover parsedHover : hoverDef.ParsedHoverTexts) {
                String transformed = HoverEvaluator.Evaluate(parsedHover, context);
                if(transformed == null) {
                    continue;
                }

                ret.add(transformed);

                // Warn about obsolete expressions
                if (config.enableObsoleteMessage()) {
                    obsoleteExpressionDetected = obsoleteExpressionDetected ||
                            Arrays
                                    .stream(parsedHover.getExpressions())
                                    .anyMatch(e -> e.isLegacy());
                }
            }
        }

        // Warn about obsolete expressions if we find any
        if(obsoleteExpressionDetected) {
            warnObsoleteExpression();
        }

        //Turn `ret` into an array from an ArrayList
        String[] retArr = new String[ret.size()];
        retArr = ret.toArray(retArr);

        lastItem = item;
        lastHovers = retArr;

        return retArr;
    }

    private void warnObsoleteExpression() {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                "",
                "<col=e74c3c>[Custom Item Hovers]</col> " +
                        "Obsolete expression detected. " +
                        "See the plugin support page (or GitHub) to learn about the new, better expression system. " +
                        "Turn this message off in settings.",
                "");
    }

    /**
     * Parses the config's hover dirs path and returns it if it's a readable directory.
     *
     * @return Path|null The path if it's a valid, readable directory Path, null otherwise.
     */
    public Path getHoverPath() {
        return Paths.get(RuneLite.RUNELITE_DIR.getAbsolutePath() + "/" + PLUGIN_FOLDER_NAME);
    }

    /**
     * Parses the config's table path and returns it if it's a readable directory.
     *
     * @return Path|null The path if it's a valid, readable directory Path, null otherwise.
     */
    public Path getTablePath() {
        return Paths.get(RuneLite.RUNELITE_DIR.getAbsolutePath() + "/" + PLUGIN_FOLDER_NAME + "/tables");
    }

    /**
     * Prepare ItemNameMap
     */
    protected void prepareItemNameMap() {
        ItemNameMap.PrepareMap(client, itemManager);
    }

    /**
     * Creates, if necessary, the `customitemhovers` folder in the user's `.runelite` directory
     *
     * @throws IOException
     */
    protected void prepareHoverFolder() throws IOException {
        Path rlPath = RuneLite.RUNELITE_DIR.toPath();

        if (!Files.isDirectory(rlPath) || !Files.isReadable(rlPath) || !Files.isWritable(rlPath)) {
            log.error("[CUSTOMITEMHOVERS] Bad .runelite path");
            return;
        }

        // Ensure that `/customitemhovers` and `/customitemhovers/tables` exist in RL directory
        prepareSpecificHoverFolder(getHoverPath());
        prepareSpecificHoverFolder(getTablePath());
    }

    /**
     * Creates a directory if it doesn't exist, logging an error if creation failed
     */
    private void prepareSpecificHoverFolder(Path path) throws IOException {
        // Create directory if it doesn't exist
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

        // Make sure we actually created the directory and it's readable
        if (!Files.isDirectory(path) || !Files.isReadable(path)) {
            log.error("[CUSTOMITEMHOVERS] Couldn't ensure path " + path);
        }
    }

    /**
     * Reads all files from the `customitemhovers` directory, parses them, and
     * prepares a map of (itemID, hovers) for each item that has a hover.
     */
    protected void prepareHoverMap() {
        hovers.clear();

        //Read all hover files
        ArrayList<HoverFile> hoverFiles = parser.readHoverFiles(getHoverPath());

        for (HoverFile f : hoverFiles) {
            for (HoverDef d : f.Hovers) {
                //Compute which item IDs this HoverDef is attached to. This fills in `d.ItemIDs`.
                parseHoverDefNames(d);

                //Add this HoverDef to the map for every Item ID it represents
                for (int itemID : d.ItemIDs) {
                    if (!hovers.containsKey(itemID))
                        hovers.put(itemID, new ArrayList<>());

                    ArrayList<HoverDef> curArr = hovers.get(itemID);
                    curArr.add(d);
                }
            }
        }
    }

    /**
     * TODO document
     */
    protected void prepareItemTables() {
        itemTableManager.reparseItemDatabase(getTablePath());
    }

    /**
     * @return True if the `customitemhovers` directory has changed since the last time this function was called
     */
    private boolean hoverWatcherTriggered() {
        if (hoverWatchKey == null || !hoverWatchKey.isValid())
            return false;

        //If we've received any filesystem events, then assume it changed
        boolean triggered = hoverWatchKey.pollEvents().size() > 0;

        //Enable more events to be queued
        hoverWatchKey.reset();

        return triggered;
    }

    /**
     * @return True if the `customitemhovers` directory has changed since the last time this function was called
     */
    private boolean tableWatcherTriggered() {
        if (tableWatchKey == null || !tableWatchKey.isValid())
            return false;

        //If we've received any filesystem events, then assume it changed
        boolean triggered = tableWatchKey.pollEvents().size() > 0;

        //Enable more events to be queued
        tableWatchKey.reset();

        return triggered;
    }

    /**
     * Set up a filesystem watcher on the `customitemhovers` directory.
     * <p>
     * This enables hot-reloading.
     */
    private void prepareHoverWatcher() {
        if (!config.hoverEnableHotReload())
            return;

        Path hoverPath = getHoverPath();
        Path tablePath = getTablePath();

        //Nothing to watch
        if (hoverPath == null)
            return;

        try {
            hoverWatcher = FileSystems.getDefault().newWatchService();

            hoverWatchKey = hoverPath.register(hoverWatcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);

            if (tablePath != null) {
                tableWatchKey = tablePath.register(hoverWatcher,
                        ENTRY_CREATE,
                        ENTRY_DELETE,
                        ENTRY_MODIFY);
            }
        } catch (Exception e) {

        }
    }

    /**
     * Closes `hoverWatcher` and `hoverWatchKey`, if possible.
     */
    private void stopHoverWatcher() {
        if (hoverWatcher != null) {
            try {
                hoverWatcher.close();
            } catch (IOException e) {
                log.error("[CUSTOMITEMHOVERS]: exception closing hover watcher", e);
            }

            hoverWatcher = null;
        }

        if (hoverWatchKey != null) {
            hoverWatchKey.cancel();
            hoverWatchKey = null;
        }

        if (tableWatchKey != null) {
            tableWatchKey.cancel();
            tableWatchKey = null;
        }
    }

    /**
     * Computes all item IDs that a HoverDef is targeting, and stores the results in
     * its `ItemIDs` member variable.
     *
     * @param d HoverDef to parse names for
     */
    private void parseHoverDefNames(HoverDef d) {
        Set<Integer> itemIDs = new HashSet<>();

        //If ItemNamesRegex is non-empty, insert all item IDs whose name matches any of the given regexes
        if (d.ItemNamesRegex != null) {
            for (String name : d.ItemNamesRegex) {
                itemIDs.addAll(ItemNameMap.GetItemIDsRegex(name));
            }
        }

        //If ItemNames is non-empty, insert all item IDs with the exact name(s) specified
        if (d.ItemNames != null) {
            for (String name : d.ItemNames) {
                for (int id : ItemNameMap.GetItemIDs(name)) {
                    itemIDs.add(id);
                }
            }
        }

        //If ItemIDs has any IDs specified, copy them in
        if (d.ItemIDs != null && d.ItemIDs.length > 0) {
            for (int id : d.ItemIDs) {
                itemIDs.add(id);
            }
        }

        //Convert `itemIDs` into an array and store it in `d.ItemIDs`
        d.ItemIDs = new int[itemIDs.size()];
        int i = 0;
        for (Iterator<Integer> it = itemIDs.iterator(); it.hasNext(); ) {
            int id = it.next();
            d.ItemIDs[i++] = id;
        }
    }
}

package com.geel.customitemhovers;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
@PluginDescriptor(
        name = "Custom Item Hovers",
        description = "Enables custom item hovers. Read github page.",
        tags = {"custom", "item", "hovers", "tooltips"},
        enabledByDefault = true
)
public class CustomItemHoversPlugin extends Plugin {
    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CustomItemHoversOverlay overlay;

    @Inject
    private CustomItemHoversConfig config;


    @Provides
    CustomItemHoversConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(CustomItemHoversConfig.class);
    }

    public Map<Integer, ArrayList<HoverDef>> hovers = new HashMap<>();

    private Map<String, Integer> reverseItemLookup = new HashMap<>();

    WatchService hoverWatcher;
    WatchKey hoverWatchKey;

    @Override
    protected void startUp() throws Exception {
        prepareHoverMap();
        prepareHoverWatcher();
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
            if(config.hoverEnableHotReload()) {
                clientThread.invoke(this::prepareHoverWatcher);
            } else {
                clientThread.invoke(this::stopHoverWatcher);
            }
        }
    }

    public String[] getItemHovers(int itemID) {
        //Do a hot-reload if we should
        if (config.hoverEnableHotReload() && hoverWatcherTriggered()) {
            prepareHoverMap();
        }

        if (!hovers.containsKey(itemID))
            return new String[0];

        ArrayList<String> ret = new ArrayList<>();
        for (HoverDef d : hovers.get(itemID)) {
            ret.addAll(Arrays.asList(d.ParsedHoverTexts));
        }

        String[] retArr = new String[ret.size()];
        retArr = ret.toArray(retArr);

        return retArr;
    }

    /**
     * Parses the config's hover dirs path and returns it if it's a readable directory.
     *
     * @return Path|null The path if it's a valid, readable directory Path, null otherwise.
     */
    public Path getHoverPath() {
        Path dirPath = Paths.get(config.hoversDir());
        if (dirPath == null || !Files.isDirectory(dirPath) || !Files.isReadable(dirPath)) {
            return null;
        }

        return dirPath;
    }

    protected void prepareHoverMap() {
        if (reverseItemLookup.size() == 0)
            makeItemNameReverseLookup();

        hovers.clear();

        ArrayList<HoverFile> hoverFiles = HoverFileParser.readHoverFiles(config.hoversDir());

        for (HoverFile f : hoverFiles) {
            for (HoverDef d : f.Hovers) {
                parseHoverDefNames(d);

                for (int itemID : d.ItemIDs) {
                    if (!hovers.containsKey(itemID))
                        hovers.put(itemID, new ArrayList<>());

                    ArrayList<HoverDef> curArr = hovers.get(itemID);
                    curArr.add(d);
                }
            }
        }
    }

    private boolean hoverWatcherTriggered() {
        if (hoverWatchKey == null || !hoverWatchKey.isValid())
            return false;

        //If we've received any filesystem events, then assume it changed
        boolean triggered = hoverWatchKey.pollEvents().size() > 0;

        //Enable more events to be queued
        hoverWatchKey.reset();

        return triggered;
    }

    private void prepareHoverWatcher() {
        //todo: watch for config changes (hot-reload config :~))
        if (!config.hoverEnableHotReload())
            return;

        Path hoverPath = getHoverPath();

        //Nothing to watch
        if (hoverPath == null)
            return;

        try {
            hoverWatcher = FileSystems.getDefault().newWatchService();

            hoverWatchKey = hoverPath.register(hoverWatcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
        } catch (Exception e) {

        }
    }

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
    }

    private void parseHoverDefNames(HoverDef d) {
        ArrayList<Integer> itemIDs = new ArrayList<Integer>();

        if (d.ItemNames != null) {
            for (String name : d.ItemNames) {
                int itemID = itemNameToID(name);
                if (itemID == -1) {
                    continue;
                }

                itemIDs.add(itemID);
            }
        }

        if (d.ItemNamesRegex != null) {
            for (String name : d.ItemNamesRegex) {
                itemIDs.addAll(itemNameToIDs(name));
            }
        }

        d.ItemIDs = new int[itemIDs.size()];
        for (int i = 0; i < itemIDs.size(); i++) {
            d.ItemIDs[i] = itemIDs.get(i);
        }
    }

    private void makeItemNameReverseLookup() {
        reverseItemLookup.clear();

        Field[] declaredFields = ItemID.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!field.getType().getTypeName().equals("int"))
                continue;

            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                continue;

            String itemName = field.getName();
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

            if (itemID <= 0) {
                log.error("[CUSTOM_ITEM_HOVERS]: " + itemName + " has invalid item ID " + itemID);
                continue;
            }

            if (reverseItemLookup.containsKey(itemName)) {
                log.error("[CUSTOM_ITEM_HOVERS]: " + itemName + " is a duplicate item ID " + itemID);
            }

            reverseItemLookup.put(itemName, itemID);
        }
    }

    private int itemNameToID(String itemNameRegex) {
        return reverseItemLookup.getOrDefault(itemNameRegex, -1);
    }

    private ArrayList<Integer> itemNameToIDs(String itemNameRegex) {
        ArrayList<Integer> ret = new ArrayList<>(1);
        Pattern finder = Pattern.compile(itemNameRegex);

        if (finder == null) {
            return ret;
        }

        for (String entry : reverseItemLookup.keySet()) {
            if (!finder.matcher(entry).matches())
                continue;

            ret.add(reverseItemLookup.get(entry));
        }

        return ret;
    }

    private boolean isValidItemName(String itemName) {
        for (int i = 0; i < itemName.length(); i++) {
            if (Character.isLowerCase(itemName.charAt(i)))
                return false;
        }

        return true;
    }
}

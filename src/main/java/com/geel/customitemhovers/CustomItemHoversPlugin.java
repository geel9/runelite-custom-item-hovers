package com.geel.customitemhovers;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import java.io.File;
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
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private CustomItemHoversOverlay overlay;

	@Inject
	private CustomItemHoversConfig config;

	@Provides
	CustomItemHoversConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(CustomItemHoversConfig.class);
	}

	//Map between an Item ID and all of its associated HoverDefs.
	public Map<Integer, ArrayList<HoverDef>> hovers = new HashMap<>();

	WatchService hoverWatcher;
	WatchKey hoverWatchKey;

	@Override
	protected void startUp() throws Exception {
		prepareHoverFolder();

		//Invoke this on the client thread because `itemManager.canonicalize()` must be run in the client thread
		clientThread.invokeLater(() -> {
			prepareItemNameMap();
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
	 *
	 * @param item
	 * @return
	 */
	public String[] getItemHovers(Item item) {
		//Do a hot-reload if we should
		if (config.hoverEnableHotReload() && hoverWatcherTriggered()) {
			prepareHoverMap();
		}

		int itemID = itemManager.canonicalize(item.getId());
		ItemComposition comp = itemManager.getItemComposition(itemID);

		//If item's ID is not in `hovers`, it has no hovers.
		if (!hovers.containsKey(itemID))
			return new String[0];

		//For each hover associated with this item, add its transformed text to the resultant array
		ArrayList<String> ret = new ArrayList<>();
		for (HoverDef d : hovers.get(itemID)) {
			ret.addAll(Arrays.asList(d.GetTransformedTexts(item, comp)));
		}

		//Turn `ret` into an array from an ArrayList
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
		return Paths.get(RuneLite.RUNELITE_DIR.getAbsolutePath() + "/" + PLUGIN_FOLDER_NAME);
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

		Path hoverPath = getHoverPath();

		//Create plugin folder in `.runelite` if it doesn't exist
		if (Files.notExists(hoverPath)) {
			Files.createDirectory(hoverPath);
		}

		//Make sure we actually created the path and it's readable
		if (!Files.isDirectory(rlPath) || !Files.isReadable(rlPath)) {
			log.error("[CUSTOMITEMHOVERS] Bad hover path");
		}
	}

	/**
	 * Reads all files from the `customitemhovers` directory, parses them, and
	 * prepares a map of (itemID, hovers) for each item that has a hover.
	 */
	protected void prepareHoverMap() {
		hovers.clear();

		//Read all hover files
		ArrayList<HoverFile> hoverFiles = HoverFileParser.readHoverFiles(getHoverPath());

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
	 * Set up a filesystem watcher on the `customitemhovers` directory.
	 * <p>
	 * This enables hot-reloading.
	 */
	private void prepareHoverWatcher() {
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

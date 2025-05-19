package com.geel.customitemhovers;

import net.runelite.api.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.api.MenuAction.CC_OP;
import static net.runelite.api.MenuAction.WIDGET_TARGET;
import static net.runelite.api.MenuAction.ITEM_USE;
import static net.runelite.api.MenuAction.ITEM_FIRST_OPTION;
import static net.runelite.api.MenuAction.ITEM_SECOND_OPTION;
import static net.runelite.api.MenuAction.ITEM_THIRD_OPTION;
import static net.runelite.api.MenuAction.ITEM_FOURTH_OPTION;
import static net.runelite.api.MenuAction.ITEM_FIFTH_OPTION;

class CustomItemHoversOverlay extends Overlay
{
	//Interface IDs for the standard inventory container
	private static final Set<Integer> INVENTORY_COMPONENT_IDS = new HashSet<>(
			Arrays.asList(
					InterfaceID.INVENTORY,
					InterfaceID.BANK_INVENTORY,
					InterfaceID.SEED_VAULT_INVENTORY,
					InterfaceID.EXPLORERS_RING
			)
	);

	//Interface IDs which an item hover MenuAction will be related to
	private static final Set<Integer> VALID_INTERFACE_IDS = new HashSet<>(
			Arrays.asList(InterfaceID.INVENTORY, InterfaceID.BANK, InterfaceID.BANK_INVENTORY,
					InterfaceID.SEED_VAULT, InterfaceID.SEED_VAULT_INVENTORY, InterfaceID.EXPLORERS_RING)
	);

	//MenuActions which indicate an item is being hovered on
	private static final Set<MenuAction> VALID_MENU_ACTIONS = new HashSet<>(
			Arrays.asList(CC_OP, ITEM_USE, WIDGET_TARGET, ITEM_FIRST_OPTION, ITEM_SECOND_OPTION,
					ITEM_THIRD_OPTION, ITEM_FOURTH_OPTION, ITEM_FIFTH_OPTION)
	);

	private final Client client;
	private final CustomItemHoversConfig config;
	private final CustomItemHoversPlugin plugin;
	private final TooltipManager tooltipManager;

	@Inject
	CustomItemHoversOverlay(Client client, CustomItemHoversPlugin plugin, CustomItemHoversConfig config, TooltipManager tooltipManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.tooltipManager = tooltipManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// If the player is not hovering over any items, reset the cache
		if(!innerRender(graphics)) {
			plugin.resetHoveredItemCache();
		}

		return null;
	}

	/**
	 * Handles the hover.
	 *
	 * Returns true if the user is presently hovering over any item, inlcuding those without a hover.
	 */
	private boolean innerRender(Graphics2D graphics) {
		//Don't display anything if a right-click menu is open.
		if (client.isMenuOpen())
			return false;

		//Get the current menu entries; if there aren't any, return.
		final MenuEntry[] menuEntries = client.getMenuEntries();
		if (menuEntries.length <= 0)
			return false;

		final MenuEntry lastEntry = menuEntries[menuEntries.length - 1];
		final MenuAction action = lastEntry.getType();
		final Widget widget = lastEntry.getWidget();

		if(widget == null) {
			return false;
		}

		final int widgetId = widget.getId();
		final int interfaceId = widgetId >>> 16;

		if (!VALID_MENU_ACTIONS.contains(action))
			return false;

		if (!VALID_INTERFACE_IDS.contains(interfaceId))
			return false;

		//We should now display a hover.
		ItemContainer container = getContainer(interfaceId);

		if (container == null)
			return false;

		//Get the Item from the Container
		final int containerItemIndex = lastEntry.getParam0();
		final Item item = container.getItem(containerItemIndex);
		if (item == null)
			return false;

		//Get the hovers for this item ID
		String[] hoverTexts = plugin.getItemHovers(item);
		if (hoverTexts.length == 0)
			return true;

		for (String s : hoverTexts) {
			tooltipManager.add(new Tooltip(ColorUtil.prependColorTag(s, config.defaultHoverColor())));
		}

		return true;
	}

	private ItemContainer getContainer(int interfaceId)
	{
		if (INVENTORY_COMPONENT_IDS.contains(interfaceId))
			return client.getItemContainer(InventoryID.INVENTORY);
		else if (interfaceId == InterfaceID.BANK)
			return client.getItemContainer(InventoryID.BANK);
		else if (interfaceId == InterfaceID.SEED_VAULT) {
			return client.getItemContainer(InventoryID.SEED_VAULT);
		}

		return null;
	}
}

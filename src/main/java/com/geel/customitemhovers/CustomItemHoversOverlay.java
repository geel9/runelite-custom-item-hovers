/*
 * Copyright (c) 2018, Charlie Waters
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.geel.customitemhovers;

import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
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
import static net.runelite.api.MenuAction.ITEM_USE;
import static net.runelite.api.MenuAction.ITEM_FIRST_OPTION;
import static net.runelite.api.MenuAction.ITEM_SECOND_OPTION;
import static net.runelite.api.MenuAction.ITEM_THIRD_OPTION;
import static net.runelite.api.MenuAction.ITEM_FOURTH_OPTION;
import static net.runelite.api.MenuAction.ITEM_FIFTH_OPTION;

import static net.runelite.api.widgets.WidgetID.INVENTORY_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.BANK_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.BANK_INVENTORY_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.SEED_VAULT_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.SEED_VAULT_INVENTORY_GROUP_ID;

class CustomItemHoversOverlay extends Overlay
{
	private static final int BANK_ITEM_WIDGETID = WidgetInfo.BANK_ITEM_CONTAINER.getPackedId();
	private static final int SEED_VAULT_ITEM_WIDGETID = WidgetInfo.SEED_VAULT_ITEM_CONTAINER.getPackedId();

	//Widget IDs for the standard inventory container
	private static final Set<Integer> INVENTORY_WIDGET_IDs = new HashSet<>(
			Arrays.asList(WidgetInfo.INVENTORY.getPackedId(),
					WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getPackedId(),
					WidgetInfo.EXPLORERS_RING_ALCH_INVENTORY.getPackedId(),
					WidgetInfo.SEED_VAULT_INVENTORY_ITEMS_CONTAINER.getPackedId())
	);

	//Widget group IDs which an item hover MenuAction will be related to
	private static final Set<Integer> VALID_WIDGET_GROUP_IDS = new HashSet<>(
			Arrays.asList(INVENTORY_GROUP_ID, BANK_GROUP_ID, BANK_INVENTORY_GROUP_ID,
					SEED_VAULT_GROUP_ID, SEED_VAULT_INVENTORY_GROUP_ID)
	);

	//MenuActions which indicate an item is being hovered on
	private static final Set<MenuAction> VALID_MENU_ACTIONS = new HashSet<>(
			Arrays.asList(CC_OP, ITEM_USE, ITEM_FIRST_OPTION, ITEM_SECOND_OPTION,
					ITEM_THIRD_OPTION, ITEM_FOURTH_OPTION, ITEM_FIFTH_OPTION)
	);
	
	private final Client client;
	private final CustomItemHoversConfig config;
	private final CustomItemHoversPlugin plugin;
	private final TooltipManager tooltipManager;

	@Inject
	ItemManager itemManager;

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
		//Don't display anything if a right-click menu is open.
		if (client.isMenuOpen())
			return null;

		//Get the current menu entries; if there aren't any, return.
		final MenuEntry[] menuEntries = client.getMenuEntries();
		if (menuEntries.length <= 0)
			return null;

		final MenuEntry lastEntry = menuEntries[menuEntries.length - 1];
		final MenuAction action = lastEntry.getType();
		final int widgetId = lastEntry.getParam1();
		final int groupId = WidgetInfo.TO_GROUP(widgetId);

		if(!VALID_MENU_ACTIONS.contains(action))
			return null;

		if(!VALID_WIDGET_GROUP_IDS.contains(groupId))
			return null;

		//We should now display a hover.
		ItemContainer container = getContainer(widgetId);

		if (container == null)
			return null;

		//Get the Item from the Container
		final int containerItemIndex = lastEntry.getParam0();
		final Item item = container.getItem(containerItemIndex);
		if (item == null)
			return null;

		int id = itemManager.canonicalize(item.getId());

		//Get the hovers for this item ID
		String[] hoverTexts = plugin.getItemHovers(id);
		if(hoverTexts.length == 0)
			return null;

		for(String s : hoverTexts) {
			tooltipManager.add(new Tooltip(ColorUtil.prependColorTag(s, config.defaultHoverColor())));
		}

		return null;
	}

	private ItemContainer getContainer(int widgetId) {
		if (INVENTORY_WIDGET_IDs.contains(widgetId))
			return client.getItemContainer(InventoryID.INVENTORY);
		else if (widgetId == BANK_ITEM_WIDGETID)
			return client.getItemContainer(InventoryID.BANK);
		else if (widgetId == SEED_VAULT_ITEM_WIDGETID)
		{
			return client.getItemContainer(InventoryID.SEED_VAULT);
		}

		return null;
	}
}

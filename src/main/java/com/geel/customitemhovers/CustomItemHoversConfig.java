package com.geel.customitemhovers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("customitemhovers")
public interface CustomItemHoversConfig extends Config
{
	@ConfigItem(
			keyName = "hoverEnableHotReload",
			name = "Hot Reload",
			description = "Whether or not Hot Reload is enabled.",
			position = 1
	)
	default boolean hoverEnableHotReload() {
		return false;
	}

	@ConfigItem(
			keyName = "hoverDefaultColor",
			name = "Default Text Color",
			description = "The default text color for a hover when no color is specified",
			position = 2
	)
	default Color defaultHoverColor() {
		return new Color(238, 238, 238);
	}

	@ConfigItem(
			keyName = "openDirChatCommand",
			name = "Hover Directory Chat Command",
			description = "Chat command to open hoverfile directory in your file explorer",
			position = 3
	)
	default String openDirChatCommand()
	{
		return "openhoverdir";
	}
}

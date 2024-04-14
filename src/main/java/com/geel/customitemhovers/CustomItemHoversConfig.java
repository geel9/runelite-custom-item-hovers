package com.geel.customitemhovers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("customitemhovers")
public interface CustomItemHoversConfig extends Config {
    @ConfigSection(
            name = "Informational Messages",
            description = "Configures messages that the plugin posts to game chat",
            position = 2
    )
    static String messageSection = "messages";

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
            keyName = "enableTutorialMessage",
            name = "Tutorial Message",
            description = "Display a message on login if you have no hovers defined",
            section = messageSection,
            position = 1
    )
    default boolean enableTutorialMessage() {
        return true;
    }

    @ConfigItem(
            keyName = "enableObsoleteMessage",
            name = "Deprecated Expression",
            description = "Display a message whenever your hover files are displayed if they contain obsolete expressions",
            section = messageSection,
            position = 2
    )
    default boolean enableObsoleteMessage() {
        return true;
    }

    @ConfigItem(
            keyName = "hoverDefaultColor",
            name = "Default Text Color",
            description = "The default text color for a hover when no color is specified",
            position = 3
    )
    default Color defaultHoverColor() {
        return new Color(238, 238, 238);
    }

    @ConfigItem(
            keyName = "openDirChatCommand",
            name = "Hover Directory Chat Command",
            description = "Chat command to open hoverfile directory in your file explorer",
            position = 4
    )
    default String openDirChatCommand() {
        return "openhoverdir";
    }
}

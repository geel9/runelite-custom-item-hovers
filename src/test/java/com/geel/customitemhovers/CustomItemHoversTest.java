package com.geel.customitemhovers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CustomItemHoversTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CustomItemHoversPlugin.class);
		RuneLite.main(args);
	}
}
package filo.friendlist.tabs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FLOverhaulPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FLOverhaulPlugin.class);
		RuneLite.main(args);
	}
}
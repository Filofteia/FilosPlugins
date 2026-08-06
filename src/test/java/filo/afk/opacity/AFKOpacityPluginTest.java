package filo.afk.opacity;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AFKOpacityPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AFKOpacityPlugin.class);
		RuneLite.main(args);
	}
}
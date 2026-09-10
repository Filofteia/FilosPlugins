package filo.cm.checklist;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CMChecklistTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CMChecklistPlugin.class);
		RuneLite.main(args);
	}
}
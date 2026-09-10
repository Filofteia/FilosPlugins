package filo.cm.checklist.panel;

import filo.cm.checklist.CMChecklistConfig;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.panel.comp.preset.PresetPanel;
import filo.cm.checklist.panel.comp.storage.StoragePanel;
import javax.swing.BoxLayout;
import filo.cm.checklist.util.ItemBoxFactory;
import filo.cm.checklist.util.SaveManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class CMChecklistPanel extends PluginPanel
{
	private StoragePanel storagePanel;
	private PresetPanel presetPanel;

	public CMChecklistPanel(
			CMChecklistPlugin plugin,
			CMChecklistConfig config,
			SaveManager saveManager,
			ClientThread clientThread,
			ItemBoxFactory itemBoxFactory
	)
	{
		storagePanel = new StoragePanel(plugin, config, clientThread, saveManager, itemBoxFactory, null);
		presetPanel = new PresetPanel(plugin, saveManager);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(presetPanel);
	}

	public void displayPresetPanel()
	{
		removeAll();
		presetPanel.build();
		add(presetPanel);
	}

	public void displayStoragePanel()
	{
		removeAll();
		storagePanel.build();
		add(storagePanel);
	}

	public void requestSetup(InstanceTemplate template, RoomSetup setup)
	{
		storagePanel.loadFromSetup(template, setup);
	}

	public void buildStorageUI()
	{
		storagePanel.build();
		repaint();
		revalidate();
	}

	public void buildPresetUI()
	{
		presetPanel.build();
		repaint();
		revalidate();
	}
}

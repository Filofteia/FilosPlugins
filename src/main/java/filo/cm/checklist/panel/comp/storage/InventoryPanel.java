package filo.cm.checklist.panel.comp.storage;

import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemBoxType;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.ItemBoxFactory;
import filo.cm.checklist.util.SaveManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.SwingUtil;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class InventoryPanel extends JPanel
{
	private ClientThread clientThread;
	private SaveManager saveManager;
	private ItemBoxFactory itemBoxFactory;
	private InstanceTemplate template;
	public InventoryPanel(ClientThread clientThread, SaveManager saveManager, ItemBoxFactory itemBoxFactory, InstanceTemplate template)
	{
		this.clientThread = clientThread;
		this.saveManager = saveManager;
		this.itemBoxFactory = itemBoxFactory;
		this.template = template;

		setLayout(new GridLayout(7, 4, 2, 2));
		setBorder(new EmptyBorder(2, 28, 2, 28));

		build(template, null);
	}

	public void build(InstanceTemplate template, RoomSetup setup)
	{
		List<Integer> itemIds = setup != null ? setup.getInventoryItems() : new ArrayList<>();
		SwingUtil.fastRemoveAll(this);

		for (int i = 0; i < 28; i++)
		{
			ItemBox itemBox = itemBoxFactory.createItemBox(i, ItemBoxType.INVENTORY, template);
			if (i < itemIds.size())
				itemBox.setItemById(itemIds.get(i));
			else
				itemBox.setItemById(0);
			add(itemBox);
		}
	}

	public void loadFromSetup(InstanceTemplate template, RoomSetup setup)
	{
		build(template, setup);
	}
}

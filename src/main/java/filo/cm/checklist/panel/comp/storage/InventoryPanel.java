package filo.cm.checklist.panel.comp.storage;

import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemType;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.ItemBoxFactory;
import net.runelite.client.util.SwingUtil;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class InventoryPanel extends JPanel
{
	private final ItemBox[] itemBoxes = new ItemBox[28];
	public InventoryPanel(ItemBoxFactory itemBoxFactory, InstanceTemplate template)
	{
		setLayout(new GridLayout(7, 4, 2, 2));
		setBorder(new EmptyBorder(2, 28, 2, 28));

		for (int i = 0; i < 28; i++)
		{
			itemBoxes[i] = itemBoxFactory.createItemBox(i, ItemType.INVENTORY, null);
			add(itemBoxes[i]);
		}

		build(template, null);
	}

	public void build(InstanceTemplate template, RoomSetup setup)
	{
		List<Integer> itemIds = setup != null ? setup.getInventoryItems() : new ArrayList<>();

		for (int i = 0; i < itemBoxes.length; i++)
		{
			ItemBox itemBox = itemBoxes[i];
			itemBox.setTemplate(template);
			itemBox.setItemById(i < itemIds.size() ? itemIds.get(i) : 0);
		}
	}
}

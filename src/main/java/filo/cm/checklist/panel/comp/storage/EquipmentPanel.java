package filo.cm.checklist.panel.comp.storage;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemBoxType;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.ItemBoxFactory;
import filo.cm.checklist.util.SaveManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.SwingUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EquipmentPanel extends JPanel
{
	// {-1, -1} is null entries, usually like jaw slot or something to skip making box
	private static final int[][] BOX_COORD =
			{
					{1, 0},
					{0, 1}, {1, 1},
					{0, 2},	{1, 2}, {2, 2},
					{-1, -1},
					{1, 3},
					{-1, -1},
					{0, 4}, {1, 4}, {-1, -1}, {2, 4}, {2, 1}
			};

	private ClientThread clientThread;
	private SaveManager saveManager;
	private ItemBoxFactory itemBoxFactory;
	private InstanceTemplate template;

	public EquipmentPanel(ClientThread clientThread, SaveManager saveManager, ItemBoxFactory itemBoxFactory, InstanceTemplate template)
	{
		this.clientThread = clientThread;
		this.saveManager = saveManager;
		this.itemBoxFactory = itemBoxFactory;
		this.template = template;

		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(2, 30, 2, 30));

		build(template, null);
	}

	public void build(InstanceTemplate template, RoomSetup setup)
	{
		List<Integer> equipmentIds = setup != null ? setup.getEquippedItems() : new ArrayList<>();
		SwingUtil.fastRemoveAll(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(1,1,1,1);

		for (int i = 0; i < BOX_COORD.length; i++)
		{
			int boxX = BOX_COORD[i][0];
			int boxY = BOX_COORD[i][1];
			if (boxX == -1 || boxY == -1)
				continue;

			gbc.gridx = boxX;
			gbc.gridy = boxY;
			ItemBox itemBox = itemBoxFactory.createItemBox(i, ItemBoxType.EQUIPMENT, template);

			if (i < equipmentIds.size())
				itemBox.setItemById(equipmentIds.get(i));

			add(itemBox, gbc);
		}
	}

	public void loadFromSetup(InstanceTemplate template, RoomSetup setup)
	{
		build(template, setup);
	}
}

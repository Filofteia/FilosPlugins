package filo.cm.checklist.panel.comp.storage;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemType;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.ItemBoxFactory;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentPanel extends JPanel
{
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
	private final ItemBox[] itemBoxes = new ItemBox[BOX_COORD.length];

	public EquipmentPanel(ItemBoxFactory itemBoxFactory, InstanceTemplate template)
	{
		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(2, 30, 2, 30));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(1,1,1,1);

        for (int i = 0; i < BOX_COORD.length; i++) {
            int boxX = BOX_COORD[i][0];
            int boxY = BOX_COORD[i][1];
            if (boxX == -1 || boxY == -1)
                continue;

            gbc.gridx = boxX;
            gbc.gridy = boxY;

            itemBoxes[i] = itemBoxFactory.createItemBox(i, ItemType.EQUIPMENT, null);
            add(itemBoxes[i], gbc);
        }

		build(template, null);
	}

	public void build(InstanceTemplate template, RoomSetup setup)
	{
		List<Integer> equipmentIds = setup != null ? setup.getEquippedItems() : new ArrayList<>();

        for (int i = 0; i < itemBoxes.length; i++) {
			ItemBox itemBox = itemBoxes[i];
			if (itemBox == null)
				continue;

            itemBox.setTemplate(template);
			itemBox.setItemById(i < equipmentIds.size() ? equipmentIds.get(i) : 0);
        }
	}
}

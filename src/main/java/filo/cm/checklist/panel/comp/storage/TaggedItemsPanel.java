package filo.cm.checklist.panel.comp.storage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemType;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.ItemBoxFactory;
import filo.cm.checklist.util.SaveManager;

public class TaggedItemsPanel extends JPanel
{
	private final boolean isDeposit;
	private final SaveManager saveManager;
	private final ItemBoxFactory itemBoxFactory;
	private InstanceTemplate template;

	private final List<ItemBox> itemBoxes = new ArrayList<>();
	public TaggedItemsPanel(
			SaveManager saveManager,
			ItemBoxFactory itemBoxFactory,
			InstanceTemplate template,
			boolean isDeposit
	)
	{
		this.isDeposit = isDeposit;
		this.saveManager = saveManager;
		this.itemBoxFactory = itemBoxFactory;
		this.template = template;

		Color redColour = new Color(147, 46, 46);
		Color greenColour = new Color(46, 147, 54);
		setBorder(new MatteBorder(1, 1, 1, 1, isDeposit ? redColour : greenColour));
		addRows(1);
		build(template, null);
	}

	public void loadFromSetup(InstanceTemplate template, RoomSetup setup)
	{
		this.template = template;
		build(template, setup);
	}

	private List<Integer> getTaggedItems(RoomSetup setup)
	{
		if (setup == null)
			return new ArrayList<>();

		return isDeposit
				? setup.getTaggedDepositItems()
				: setup.getTaggedWithdrawItems();
	}

	private void build(InstanceTemplate template, RoomSetup setup)
	{
		List<Integer> itemIds = getTaggedItems(setup);
		int itemCount = itemIds.size();
		int rows = itemCount / 4 + 1;
		int uiRows = itemBoxes.size() / 4;

		int rowDiff = Math.abs(rows - uiRows);
		boolean uiRefresh = uiRows != rows;
		if (uiRefresh)
		{
			if (rows > uiRows)	// fix for multiple rows.
				addRows(rowDiff);
			else
				deleteRows(rowDiff);
		}

		setLayout(new GridLayout(0, 4, 2, 2));
		for (int i = 0; i < itemBoxes.size(); i++)
		{
			ItemBox itemBox = itemBoxes.get(i);
			int itemId = itemIds.size() > i ? itemIds.get(i) : 0;

			itemBox.setTemplate(template);
			itemBox.setItemById(itemId);
		}

		setMaximumSize(getPreferredSize());
	}

	private void addRows(int rows)
	{
		ItemType type = isDeposit ? ItemType.DEPOSIT : ItemType.WITHDRAW;
		for (int i = 0; i < rows * 4; i++)
		{
			ItemBox itemBox = itemBoxFactory.createItemBox(itemBoxes.size(), type, template, this::refreshCallback);
			itemBoxes.add(itemBox);
			add(itemBox);
		}
	}

	private void deleteRows(int rows)
	{
		if (itemBoxes.isEmpty())
			return;

		if (rows * 4 > itemBoxes.size())
			return;

		int itemSize = itemBoxes.size() - 1;
		for (int i = 0; i < rows * 4; i++)
		{
			ItemBox itemBox = itemBoxes.get(itemSize - i);
			remove(itemBox);
			itemBoxes.remove(itemSize - i);
		}
	}

	private void refreshCallback()
	{
		RoomSetup room = saveManager.getRoom(template);
		build(template, room);
	}
}

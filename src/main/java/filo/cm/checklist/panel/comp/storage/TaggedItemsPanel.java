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
import net.runelite.client.util.SwingUtil;

public class TaggedItemsPanel extends JPanel
{
	private final boolean isDeposit;
	private final SaveManager saveManager;
	private final ItemBoxFactory itemBoxFactory;
	private InstanceTemplate template;

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

		setBorder(new MatteBorder(1, 1, 1, 1, isDeposit ? Color.RED : Color.GREEN));
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
		SwingUtil.fastRemoveAll(this);
		List<Integer> itemIds = setup == null ? new ArrayList<>() : getTaggedItems(setup);
		int itemCount = itemIds.size();
		int rows = itemCount / 4 + 1;

		setLayout(new GridLayout(rows, 4, 2, 2));
		for (int i = 0; i < rows * 4; i++)
		{
			ItemType type = isDeposit ? ItemType.DEPOSIT : ItemType.WITHDRAW;
			ItemBox itemBox = itemBoxFactory.createItemBox(i, type, template, this::refreshCallback);

			if (i < itemCount)
				itemBox.setItemById(itemIds.get(i));

			add(itemBox);
		}

		setMaximumSize(getPreferredSize());
	}

	private void refreshCallback()
	{
		RoomSetup room = saveManager.getRoom(template);
		build(template, room);
	}
}

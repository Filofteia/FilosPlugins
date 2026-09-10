package filo.cm.checklist.data.save;

import java.util.ArrayList;
import java.util.List;

import filo.cm.checklist.data.ItemBoxType;
import filo.cm.checklist.data.ItemType;
import filo.cm.checklist.data.PotionRole;
import filo.cm.checklist.data.PotionType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RoomSetup
{
	List<Integer> equippedItems = new ArrayList<>();
	List<Integer> inventoryItems = new ArrayList<>();
	List<Integer> taggedWithdrawItems = new ArrayList<>();
	List<Integer> taggedDepositItems = new ArrayList<>();
	int brewCount = 0;
	int restCount = 0;

	public void toggleWithdrawTag(int itemId)
	{
		if (taggedDepositItems.contains(itemId) || equippedItems.contains(itemId))
			return;

		if (taggedWithdrawItems.contains(itemId))
			taggedWithdrawItems.remove((Integer) itemId);
		else
			taggedWithdrawItems.add(itemId);
	}

	public void toggleDepositTag(int itemId)
	{
		if (taggedWithdrawItems.contains(itemId) || equippedItems.contains(itemId))
			return;

		if (taggedDepositItems.contains(itemId))
			taggedDepositItems.remove((Integer) itemId);
		else
			taggedDepositItems.add(itemId);
	}

	public ItemType getItemType(int itemId)
	{
		boolean isWithdraw = taggedWithdrawItems.contains(itemId);
		boolean isDeposit = taggedDepositItems.contains(itemId);
		boolean isEquipment = equippedItems.contains(itemId);
		boolean isInventory = inventoryItems.contains(itemId);

		if (isEquipment)
			return ItemType.EQUIP;
		if (isDeposit)
			return ItemType.DEPOSIT;
		if (isWithdraw)
			return ItemType.WITHDRAW;

		return ItemType.NONE;
	}

	public void clearSetup()
	{
		equippedItems.clear();
		inventoryItems.clear();
		taggedDepositItems.clear();
		taggedWithdrawItems.clear();
	}

	public boolean isItemTagged(int itemId)
	{
		return taggedWithdrawItems.contains(itemId) || taggedDepositItems.contains(itemId);
	}

	public void updateItem(int idx, int itemId, ItemBoxType type)
	{
		switch (type)
		{
			case EQUIPMENT:
				updateSlot(equippedItems, idx, itemId);
				break;
			case INVENTORY:
				updateSlot(inventoryItems, idx, itemId);
				break;
			case WITHDRAW:
				updateTaggedItem(taggedWithdrawItems, idx, itemId);
				break;
			case DEPOSIT:
				updateTaggedItem(taggedDepositItems, idx, itemId);
				break;
		}
	}

	public void setInventoryItems(List<Integer> items)
	{
		if (items.size() > 28)
			return;

		inventoryItems = items;
		calculateBrews();
		calculateRests();
	}

	private void updateSlot(List<Integer> items, int idx, int itemId)
	{
		while (items.size() <= idx)
		{
			items.add(0);
		}

		items.set(idx, itemId);
		calculateBrews();
		calculateRests();
	}

	private void updateTaggedItem(List<Integer> items, int idx, int itemId)
	{
		if (itemId <= 0)
		{
			if (idx >= 0 && idx < items.size())
			{
				items.remove(idx);
			}
		}
		else if (idx >= items.size())
		{
			items.add(itemId);
		}
		else
		{
			items.set(idx, itemId);
		}
	}

	public void calculateBrews()
	{
		int brewLocal = 0;
		for (int i : inventoryItems)
		{
			PotionType type = PotionType.fromItemId(i);
			if (type == null)
				continue;

			boolean inc = type.getRole() == PotionRole.BREW;
			if (inc)
				brewLocal++;
		}

		this.brewCount = brewLocal;
	}

	private void calculateRests()
	{
		int restLocal = 0;
		for (int i : inventoryItems)
		{
			PotionType type = PotionType.fromItemId(i);
			if (type == null)
				continue;

			boolean inc = type.getRole() == PotionRole.RESTORE;
			if (inc)
				restLocal++;
		}

		restCount = restLocal;
	}
}

package filo.cm.checklist.data.save;

import java.util.ArrayList;
import java.util.List;
import filo.cm.checklist.data.ItemType;
import filo.cm.checklist.data.PotionRole;
import filo.cm.checklist.data.PotionType;
import lombok.Data;

@Data
public class RoomSetup
{
	List<Integer> equippedItems = new ArrayList<>();
	List<Integer> inventoryItems = new ArrayList<>();
	List<Integer> taggedWithdrawItems = new ArrayList<>();
	List<Integer> taggedDepositItems = new ArrayList<>();
	int brewCount;
	int restCount;

	public void toggleWithdrawTag(int itemId)
	{
		toggleTag(taggedWithdrawItems, taggedDepositItems, itemId);
	}

	public void toggleDepositTag(int itemId)
	{
		toggleTag(taggedDepositItems, taggedWithdrawItems, itemId);
	}

	private void toggleTag(List<Integer> tags, List<Integer> otherTags, int itemId)
	{
		otherTags.remove((Integer) itemId);

		if (!tags.remove(Integer.valueOf(itemId)))
			tags.add(itemId);
	}

	public ItemType getItemType(int itemId)
	{
		if (equippedItems.contains(itemId))
			return ItemType.EQUIPMENT;
		if (taggedDepositItems.contains(itemId))
			return ItemType.DEPOSIT;
		if (taggedWithdrawItems.contains(itemId))
			return ItemType.WITHDRAW;

		return ItemType.NONE;
	}

	public void clearSetup()
	{
		equippedItems.clear();
		inventoryItems.clear();
		taggedDepositItems.clear();
		taggedWithdrawItems.clear();
		brewCount = 0;
		restCount = 0;
	}

	public boolean isItemTagged(int itemId)
	{
		return taggedWithdrawItems.contains(itemId) || taggedDepositItems.contains(itemId);
	}

	public void updateItem(int idx, int itemId, ItemType type)
	{
		switch (type)
		{
			case EQUIPMENT:
				updateSlot(equippedItems, idx, itemId);
				break;
			case INVENTORY:
				updateSlot(inventoryItems, idx, itemId);
				calculatePotions();
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
		calculatePotions();
	}

	private void updateSlot(List<Integer> items, int idx, int itemId)
	{
		while (items.size() <= idx)
		{
			items.add(0);
		}

		items.set(idx, itemId);
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

	private void calculatePotions()
	{
		brewCount = 0;
		restCount = 0;

		for (int itemId : inventoryItems)
		{
			PotionType type = PotionType.fromItemId(itemId);
			if (type == null)
				continue;

			if (type.getRole() == PotionRole.BREW)
				brewCount++;
			else if (type.getRole() == PotionRole.RESTORE)
				restCount++;
		}
	}
}

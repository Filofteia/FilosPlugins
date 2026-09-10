package filo.cm.checklist.overlay;

import filo.cm.checklist.CMChecklistConfig;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.BrewContext;
import filo.cm.checklist.data.PotionRole;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.data.ItemType;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;

import filo.cm.checklist.util.PotionUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class ItemHighlights extends WidgetItemOverlay
{
	private final CMChecklistPlugin plugin;
	private final CMChecklistConfig config;
	private final ItemManager itemManager;

	@Inject
	ItemHighlights(CMChecklistPlugin plugin, CMChecklistConfig config, ItemManager itemManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		showOnInterfaces(
				InterfaceID.RAIDS_STORAGE_PRIVATE,
				InterfaceID.RAIDS_STORAGE_SIDE,
				InterfaceID.INVENTORY
		);
	}

	private BrewContext brewContext;
	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getActiveRoomSetup() == null)
			return super.render(graphics);

		restoreRenders = 0;
		brewRenders = 0;

		brewContext = plugin.getBrewContext();
		return super.render(graphics);
	}

	private java.util.List<Integer> mismatchedItems = new ArrayList<>();
	private int restoreRenders = 0;
	private int brewRenders = 0;
	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (plugin.getActiveTemplate() == null)
			return;

		RoomSetup templateSetup = plugin.getActiveRoomSetup();
		if (templateSetup == null)
			return;

		int widgetQty = widgetItem.getQuantity();
		int itemIndex = widgetItem.getWidget().getIndex();
		ItemType itemType = templateSetup.getItemType(itemId);
		Rectangle widgetBounds = widgetItem.getCanvasBounds();

		boolean widgetFakeItem = widgetItem.getWidget().getName().isBlank();	// maybe make this better soon
		if (widgetFakeItem)
		{
			if (config.ghostOutline())
				renderItemOverlay(graphics, itemId, widgetQty, config.ghostColour(), widgetBounds, false);

			return;
		}

		int containerGroupId = widgetItem.getWidget().getId() >> 16;
		if (mismatchedItems.contains(itemIndex) && config.highlightMismatched())
		{
			if (containerGroupId != InterfaceID.RAIDS_STORAGE_PRIVATE)
			{
				graphics.setColor(config.highlightMismatchedColour());
				graphics.drawRect(widgetBounds.x, widgetBounds.y, widgetBounds.width, widgetBounds.height);
			}
		}

		if (itemIndexes.contains(itemIndex) && (!config.zigzag() || !config.zigzagInventory()) && config.highlightInventory())
		{
			if (containerGroupId == InterfaceID.RAIDS_STORAGE_PRIVATE)
				renderItemOverlay(graphics, itemId, widgetQty, config.inventoryColour(), widgetBounds, true);
		}

		boolean isBrew = PotionUtil.getRole(itemId) == PotionRole.BREW;
		if (isBrew && brewContext != null)
		{
			if (brewContext.isSkipRoom())
				return;

			int withdrawQuantity = brewContext.getMissingBrews() - brewRenders;
			int depositQuantity = brewContext.getSurplusBrews() - brewRenders;

			boolean isRendered = renderPotionWarnings(graphics, withdrawQuantity, depositQuantity, itemId, widgetQty, widgetBounds, containerGroupId);
			if (isRendered)
				brewRenders++;

			return;
		}

		boolean isRestore = PotionUtil.getRole(itemId) == PotionRole.RESTORE;
		if (isRestore && brewContext != null)
		{
			if (brewContext.isSkipRoom() || widgetItem.getWidget().getName().isBlank())
				return;

			int withdrawQuantity = brewContext.getMissingRestores() - restoreRenders;
			int depositQuantity = brewContext.getSurplusRestores() - restoreRenders;
			boolean isRendered = renderPotionWarnings(graphics, withdrawQuantity, depositQuantity, itemId, widgetQty, widgetBounds, containerGroupId);
			if (isRendered)
				restoreRenders++;

			return;
		}

		switch (itemType)
		{
			case NONE:
				return;
			case EQUIP:
				if (config.highlightEquipment())
					renderItemOverlay(graphics, itemId, widgetQty, config.equipmentColour(), widgetBounds, true);
				break;
			case WITHDRAW:
				if (containerGroupId != InterfaceID.RAIDS_STORAGE_PRIVATE)	// No inventory
					return;

				if (config.highlightWithdraw())
					renderItemOverlay(graphics, itemId, widgetQty, config.withdrawColour(), widgetBounds, true);
				break;
			case DEPOSIT:
				if (containerGroupId == InterfaceID.RAIDS_STORAGE_PRIVATE)	// Only inventory
					return;

				if (config.highlightDeposit())
					renderItemOverlay(graphics, itemId, widgetQty, config.depositColour(), widgetBounds, true);
				break;
		}
	}


	private boolean renderPotionWarnings(Graphics2D graphics, int withdrawQty, int depositQty, int itemId, int widgetQty, Rectangle widgetBounds, int containerGroupId)
	{
		if (withdrawQty > 0)
		{
			if (containerGroupId != InterfaceID.RAIDS_STORAGE_PRIVATE)
				return false;

			renderItemOverlay(graphics, itemId, widgetQty, config.withdrawColour(), widgetBounds, true);
			return true;
		}

		if (depositQty > 0)
		{
			renderItemOverlay(graphics, itemId, widgetQty, config.depositColour(), widgetBounds, true);
			return true;
		}

		return false;
	}

	private void renderItemOverlay(Graphics2D graphics, int itemId, int itemQty, Color color, Rectangle bounds, boolean fill)
	{
		graphics.drawImage(
				itemManager.getItemOutline(itemId, itemQty, color),
				(int) bounds.getX(),
				(int) bounds.getY(),
				null
		);

		if (!fill)
			return;

		graphics.drawImage(
				getFillImage(color, itemId, itemQty),
				(int) bounds.getX(),
				(int) bounds.getY(),
				null
		);
	}

	private Image getFillImage(Color color, int itemId, int qty)
	{
		final Color fillColor = ColorUtil.colorWithAlpha(color, 72);
        return ImageUtil.fillImage(itemManager.getImage(itemId, qty, false), fillColor);
	}

	private List<Integer> itemIndexes = new ArrayList<>();
	public void updateInventoryItems(List<Integer> itemIndex)
	{
		itemIndexes = itemIndex;
	}

	public void updateMismatchedItems(List<Integer> mismatchIndexes)
	{
		this.mismatchedItems = mismatchIndexes;
	}

	public void clearIndexes()
	{
		this.mismatchedItems.clear();
	}
}

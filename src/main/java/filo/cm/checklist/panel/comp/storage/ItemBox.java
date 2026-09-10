package filo.cm.checklist.panel.comp.storage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemBoxType;
import filo.cm.checklist.util.SaveManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;

@Slf4j
public class ItemBox extends JPanel
{
	private final JLabel itemLabel;
	private final ChatboxItemSearch chatboxItemSearch;
	private final ClientThread clientThread;
	private final Client client;
	private final SaveManager saveManager;
	private final ItemManager itemManager;
	private final InstanceTemplate template;
	private final Runnable refresh;

	public ItemBox(ClientThread clientThread,
				   Client client,
				   SaveManager saveManager,
				   ItemManager itemManager,
				   int idx,
				   ItemBoxType type,
				   ChatboxItemSearch chatboxItemSearch,
				   InstanceTemplate template,
				   Runnable refresh
	)
	{
		this.clientThread = clientThread;
		this.client = client;
		this.chatboxItemSearch = chatboxItemSearch;
		this.saveManager = saveManager;
		this.itemManager = itemManager;
		this.template = template;
		this.refresh = refresh;

		itemLabel = new JLabel();
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(2, 2, 2, 2));

		Dimension pref = new Dimension(37, 37);
		setPreferredSize(pref);
		setMaximumSize(pref);
		setMinimumSize(pref);

		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		add(itemLabel, BorderLayout.CENTER);

		if (template != null)
			addPopupMenu(idx, type);
	}

	/**
	 * This should be called on loading, or from 'update from equipped items'.
	 * @param itemId Item's ID
	 */
	public void setItemById(int itemId)
	{
		if (itemId <= 0)
		{
			SwingUtilities.invokeLater(() -> {
				itemLabel.setIcon(null);
				itemLabel.setToolTipText("");
				itemLabel.repaint();
				itemLabel.revalidate();
			});
			return;
		}

		AsyncBufferedImage icon = itemManager.getImage(itemId);
		clientThread.invokeLater(() -> {
			ItemComposition itemComposition = itemManager.getItemComposition(itemId);
			String tooltip = itemComposition.getMembersName();

			SwingUtilities.invokeLater(() -> {
				icon.addTo(itemLabel);
				itemLabel.setToolTipText(tooltip);
				itemLabel.revalidate();
				itemLabel.repaint();
				revalidate();
				repaint();
			});
		});
	}

	private void addPopupMenu(int idx, ItemBoxType type)
	{
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem replaceFromInventory = new JMenuItem("Update from Inventory");
		replaceFromInventory.addActionListener(e -> {
			updateFromInventory(idx, type);
		});

		JMenuItem addFromSearch = new JMenuItem("Add from search");
		addFromSearch.addActionListener(e -> {
			searchForItem(idx, type);
		});

		JMenuItem removeItem = new JMenuItem("Clear Slot");
		removeItem.addActionListener(e -> {
			removeItem(idx, type);
		});


		if (type == ItemBoxType.EQUIPMENT)
			replaceFromInventory.setText("Update from Equipment");
		if (type != ItemBoxType.DEPOSIT && type != ItemBoxType.WITHDRAW)
			popupMenu.add(replaceFromInventory);
		popupMenu.add(addFromSearch);
		popupMenu.add(removeItem);

		setComponentPopupMenu(popupMenu);
		itemLabel.setComponentPopupMenu(popupMenu);
	}

	private void updateFromInventory(int idx, ItemBoxType type)
	{
		clientThread.invokeLater(() -> {
			boolean isInventory = type == ItemBoxType.INVENTORY;
			ItemContainer itemContainer = client.getItemContainer(isInventory ? InventoryID.INV : InventoryID.WORN);

			if (itemContainer == null)
			{
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "Could not access inventory."
                ));
				return;
			}

            Item[] playerItems = itemContainer.getItems();
			int itemId = playerItems.length > idx
					? playerItems[idx].getId()
					: -1;

			// I forget why I used the Consumer version here
			saveManager.updateItem(
					idx,
					type,
					itemId,
					template,
					status ->
			{
				SwingUtilities.invokeLater(() -> {
					if (status && refresh != null)
						refresh.run();
					else if (status)
						setItemById(itemId);
				});
			});
		});
	}

	private void searchForItem(int idx, ItemBoxType type)
	{
		clientThread.invokeLater(() -> {
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				SwingUtilities.invokeLater(() ->
						JOptionPane.showMessageDialog(
								this,
								"You must be logged in to search for items."
						)
				);

				return;
			}

			chatboxItemSearch
					.tooltipText("Change Item ():")
					.onItemSelected((itemId) ->
					{
						saveManager.updateItem(
								idx,
								type,
								itemId,
								template
						);
						SwingUtilities.invokeLater(() -> {
							if ((type == ItemBoxType.DEPOSIT
								|| type == ItemBoxType.WITHDRAW)
								&& refresh != null)
							{
								refresh.run();
							}
							else {
								setItemById(itemId);
							}
						});
					})
					.build();
		});
	}

	private void removeItem(int idx, ItemBoxType type)
	{
		clientThread.invokeLater(() ->
		{
			saveManager.updateItem(
					idx,
					type,
					-1,
					template
			);
			SwingUtilities.invokeLater(() -> {
				if ((type == ItemBoxType.DEPOSIT
					|| type == ItemBoxType.WITHDRAW)
					&& refresh != null)
				{
					refresh.run();
				}
				else
				{
					setItemById(0);
				}
			});
		});
	}
}

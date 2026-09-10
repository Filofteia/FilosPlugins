package filo.cm.checklist.panel.comp.layout;

import filo.cm.checklist.CMChecklistConfig;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.CMLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class FloorLayoutPanel extends JPanel
{
	@Getter	@Setter	private int currentFloor = 3;
	private final int MAX_FLOOR = 3;

	private CMChecklistPlugin plugin;
	private CMChecklistConfig config;

	private JComponent[] roomButtons;
	private int[] roomOrder;

	public FloorLayoutPanel(CMChecklistPlugin plugin, CMChecklistConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setLayout(new GridLayout(2, 4, 10, 10));
		setOpaque(false);
		setPreferredSize(new Dimension(170, 80));
		setMinimumSize(new Dimension(170, 80));

		loadFloorLayout(currentFloor);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		drawConnections((Graphics2D) g);
	}

	private void drawConnections(Graphics2D graphics)
	{
		if (roomOrder == null || roomButtons == null || currentFloor == 0)
			return;

		graphics.setColor(ColorScheme.BRAND_ORANGE);
		graphics.setStroke(new BasicStroke(1f));

		for (int i = 0; i < roomOrder.length - 1; i++)
		{
			if (i == 0)
				graphics.setColor(config.entryRoomColour());
			else if (i == 6)
				graphics.setColor(config.endRoomColour());
			else
				graphics.setColor(ColorScheme.BRAND_ORANGE_TRANSPARENT);

			graphics.setStroke(new BasicStroke(10f - (float)i));
			JLabel a = (JLabel)roomButtons[roomOrder[i]];
			JLabel b = (JLabel)roomButtons[roomOrder[i+1]];

			Point a1 = getBtnCenter(a);
			Point b1 = getBtnCenter(b);

			graphics.drawLine(a1.x, a1.y, b1.x, b1.y);
		}
	}

	private Point getBtnCenter(JComponent btn)
	{
		if (btn == null)
			return new Point(0, 0);	// For Olm

		int centerX = btn.getX() + btn.getWidth() / 2;
		int centerY = btn.getY() + btn.getHeight() / 2;

		return new Point(centerX, centerY);
	}

	public void setFloor(int floorId)
	{
		if (floorId > MAX_FLOOR || floorId < 0 || floorId == currentFloor)
			return;

		currentFloor = floorId;
		SwingUtil.fastRemoveAll(this);
		loadFloorLayout(floorId);
		revalidate();
		repaint();
	}

	public void incrementFloor()
	{
		if (currentFloor >= MAX_FLOOR)
			return;

		currentFloor++;
		SwingUtil.fastRemoveAll(this);
		loadFloorLayout(currentFloor);
		revalidate();
		repaint();
	}

	public void decrementFloor()
	{
		if (currentFloor <= 0)
			return;

		currentFloor--;
		SwingUtil.fastRemoveAll(this);
		loadFloorLayout(currentFloor);
		revalidate();
		repaint();
	}

	public void resetFloor()
	{
		currentFloor = MAX_FLOOR;
		SwingUtil.fastRemoveAll(this);
		loadFloorLayout(currentFloor);
		revalidate();
		repaint();
	}

	private void loadFloorLayout(int floorIdx)
	{
		roomOrder = new int[8];
		roomButtons = new JComponent[8];
		CMLayout[] floorLayout = CMLayout.getByFloor(floorIdx);

		if (floorIdx == 0)
		{
			setLayout(new GridLayout(1, 1, 10, 10));
			setOpaque(false);
		}
		else
		{
			setLayout(new GridLayout(2, 4, 10, 10));
			setOpaque(false);
		}

		for (CMLayout room : floorLayout)
		{
			if (room == null) continue;
			int roomIndex = room.ordinal() % 8;
//			boolean isSelectedTemplate = template != null
//					? room.getInstanceTemplate() == template
//					: false;

			roomOrder[roomIndex] = room.getRoomPos();

			JLabel roomBtn = new JLabel(String.valueOf(roomIndex + 1));

			roomBtn.setHorizontalAlignment(SwingConstants.CENTER);
			roomBtn.setPreferredSize(new Dimension(35, 35));
			roomBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			roomBtn.setToolTipText(room.getRoomName());
			roomBtn.setOpaque(true);
			roomBtn.setBorder(new MatteBorder(new Insets(2, 2, 2, 2), getRoomColour(room, roomIndex)));
			if (room.isHasStorage())
			{
				roomBtn.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						super.mouseClicked(e);
						if (e.getButton() == MouseEvent.BUTTON1)
						{
							if (room.getInstanceTemplate() != null)
							{
								plugin.requestLoadPreset(room.getInstanceTemplate());
							}
						}
					}
;
					@Override
					public void mousePressed(MouseEvent e)
					{
						super.mousePressed(e);
						roomBtn.setBackground(new Color(20, 20, 20));
					}

					@Override
					public void mouseReleased(MouseEvent e)
					{
						super.mouseReleased(e);
						roomBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
					}

					@Override
					public void mouseEntered(MouseEvent e)
					{
						super.mouseEntered(e);
						roomBtn.setBackground(ColorScheme.DARK_GRAY_COLOR);
					}

					@Override
					public void mouseExited(MouseEvent e)
					{
						super.mouseExited(e);
						roomBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
					}
				});
			}

			roomButtons[room.getRoomPos()] = roomBtn;
		}

		for (JComponent comp : roomButtons)
		{
			if (comp == null) continue;
			add(comp);
		}
	}

	private Color getRoomColour(CMLayout room, int roomIndex)
	{
		if (roomIndex == 7)	// End first, as it always has storage
			return config.endRoomColour();

		if (room.isHasStorage())
			return config.storageRoomColour();

		if (roomIndex == 0)
			return config.entryRoomColour();

		return ColorScheme.MEDIUM_GRAY_COLOR;
	}
}
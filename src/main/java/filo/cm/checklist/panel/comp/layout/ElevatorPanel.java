package filo.cm.checklist.panel.comp.layout;

import filo.cm.checklist.CMChecklistPlugin;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

public class ElevatorPanel extends JPanel
{
	private static final ImageIcon UP_ICON;
	private static final ImageIcon DOWN_ICON;
	private static final ImageIcon UP_ICON_HOVERED;
	private static final ImageIcon DOWN_ICON_HOVERED;

	static
	{
		final BufferedImage upIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "up_icon.png");
		final BufferedImage downIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "down_icon.png");
		UP_ICON  = new ImageIcon(upIcon);
		DOWN_ICON = new ImageIcon(downIcon);
		UP_ICON_HOVERED = new ImageIcon(ImageUtil.alphaOffset(upIcon, -220));
		DOWN_ICON_HOVERED = new ImageIcon(ImageUtil.alphaOffset(downIcon, -220));
	}

	private CMChecklistPlugin plugin;
	private FloorLayoutPanel floorLayoutPanel;

	private JButton upBtn;
	private JButton downBtn;
	private JLabel floorLbl;
	public ElevatorPanel(CMChecklistPlugin plugin, FloorLayoutPanel floorLayoutPanel)
	{
		this.plugin = plugin;
		this.floorLayoutPanel = floorLayoutPanel;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		upBtn = new JButton(UP_ICON);
		downBtn = new JButton(DOWN_ICON);
		floorLbl = new JLabel(String.valueOf(floorLayoutPanel.getCurrentFloor()));

		SwingUtil.removeButtonDecorations(upBtn);
		upBtn.setRolloverIcon(UP_ICON_HOVERED);
		downBtn.setOpaque(true);
		upBtn.setToolTipText("Increment Floor");
		upBtn.addActionListener(e -> incrementFloor(floorLbl));

		SwingUtil.removeButtonDecorations(downBtn);
		downBtn.setRolloverIcon(DOWN_ICON_HOVERED);
		downBtn.setOpaque(true);
		downBtn.setToolTipText("Decrement Floor");
		downBtn.addActionListener(e -> decrementFloor(floorLbl));

		floorLbl.setHorizontalAlignment(SwingConstants.CENTER);
		floorLbl.setFont(FontManager.getRunescapeBoldFont());

		add(upBtn, BorderLayout.NORTH);
		add(floorLbl, BorderLayout.CENTER);
		add(downBtn, BorderLayout.SOUTH);
	}

	private void incrementFloor(JLabel floorLbl)
	{
		floorLayoutPanel.incrementFloor();
		floorLbl.setText(String.valueOf(floorLayoutPanel.getCurrentFloor()));
	}

	private void decrementFloor(JLabel floorLbl)
	{
		floorLayoutPanel.decrementFloor();
		floorLbl.setText(String.valueOf(floorLayoutPanel.getCurrentFloor()));
	}

	public void updateFloorLabel()
	{
		floorLbl.setText(String.valueOf(floorLayoutPanel.getCurrentFloor()));
	}

	public void resetFloor()
	{
		floorLayoutPanel.resetFloor();
		floorLbl.setText(String.valueOf(floorLayoutPanel.getCurrentFloor()));
	}
}

package filo.cm.checklist.panel.comp.layout;

import filo.cm.checklist.CMChecklistConfig;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.InstanceTemplate;
import net.runelite.client.util.SwingUtil;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class RaidRoomPanel extends JPanel
{
	private CMChecklistPlugin plugin;

	private ElevatorPanel elevatorPanel;
	private FloorLayoutPanel layoutPanel;
	public RaidRoomPanel(CMChecklistPlugin plugin, CMChecklistConfig config)
	{
		this.plugin = plugin;
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5,10,5,10));

		layoutPanel = new FloorLayoutPanel(plugin, config);
		elevatorPanel = new ElevatorPanel(plugin, layoutPanel);
		elevatorPanel.setBorder(new EmptyBorder(17,2,17,2));

		build();
	}

	public void setFloor(int newFloor)
	{
		int currentFloor = layoutPanel.getCurrentFloor();
		if (newFloor == currentFloor)
			return;

		layoutPanel.setFloor(newFloor);
		elevatorPanel.updateFloorLabel();
	}

	public void build()
	{
		removeAll();
		elevatorPanel.resetFloor();
		add(layoutPanel, BorderLayout.CENTER);
		add(elevatorPanel, BorderLayout.EAST);
		setMaximumSize(getPreferredSize());
	}
}

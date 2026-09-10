package filo.cm.checklist.panel.comp.storage;

import filo.cm.checklist.CMChecklistConfig;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.save.RaidSetup;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.panel.comp.layout.RaidRoomPanel;
import filo.cm.checklist.util.ItemBoxFactory;
import filo.cm.checklist.util.SaveManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StoragePanel extends JPanel
{
    private RaidRoomPanel elevatorPanel;

    private EquipmentPanel equipmentPanel;
    private InventoryPanel inventoryPanel;
    private TaggedItemsPanel depositPanel;
    private TaggedItemsPanel withdrawPanel;

    private JLabel equipmentLabel;
    private JLabel inventoryLabel;
    private JLabel depositLabel;
    private JLabel withdrawLabel;

    private static final ImageIcon SETUP_ICON;
    private static final ImageIcon SETUP_ICON_HOVERED;
    private static final ImageIcon BACKPACK_ICON;
    private static final ImageIcon BACKPACK_ICON_HOVERED;
    private static final ImageIcon EQUIP_ICON;
    private static final ImageIcon EQUIP_ICON_HOVERED;
    private static final ImageIcon EXIT_ICON;
    private static final ImageIcon EXIT_ICON_HOVERED;

    static
    {
        final BufferedImage setupIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "update_all.png");
        final BufferedImage backpackIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "backpack.png");
        final BufferedImage equipIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "invent.png");
        final BufferedImage exitIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "delete_icon.png");
        SETUP_ICON = new ImageIcon(setupIcon);
        SETUP_ICON_HOVERED  = new ImageIcon(ImageUtil.alphaOffset(setupIcon, -220));
        BACKPACK_ICON = new ImageIcon(backpackIcon);
        BACKPACK_ICON_HOVERED = new ImageIcon(ImageUtil.alphaOffset(backpackIcon, -220));
        EQUIP_ICON = new ImageIcon(equipIcon);
        EQUIP_ICON_HOVERED = new ImageIcon(ImageUtil.alphaOffset(equipIcon, -220));
        EXIT_ICON = new ImageIcon(exitIcon);
        EXIT_ICON_HOVERED = new ImageIcon(ImageUtil.alphaOffset(exitIcon, -220));
    }

    private CMChecklistPlugin plugin;
    private SaveManager saveManager;
    private InstanceTemplate template;
    public StoragePanel(
            CMChecklistPlugin plugin,
            CMChecklistConfig config,
            ClientThread clientThread,
            SaveManager saveManager,
            ItemBoxFactory itemBoxFactory,
            InstanceTemplate template
    )
    {
        this.plugin = plugin;
        this.template = template;
        this.saveManager = saveManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(CENTER_ALIGNMENT);

        elevatorPanel = new RaidRoomPanel(plugin, config);
        equipmentPanel = new EquipmentPanel(clientThread, saveManager, itemBoxFactory, template);
        inventoryPanel = new InventoryPanel(clientThread, saveManager, itemBoxFactory, template);
        depositPanel = new TaggedItemsPanel(clientThread, saveManager, itemBoxFactory, template, true);
        withdrawPanel = new TaggedItemsPanel(clientThread, saveManager, itemBoxFactory, template, false);
        equipmentLabel = new JLabel("Equipment");
        inventoryLabel = new JLabel("Inventory");
        depositLabel = new JLabel("Deposit");
        withdrawLabel = new JLabel("Withdraw");

        build();
    }


    public JPanel createHeader()
    {
        final String FAILED_MSG = "Failed to save preset.";
        final String FAILED_TITLE = "Save Failed";

        JLabel setupName = new JLabel("Setup Name");

        RaidSetup setup = saveManager.getActiveSetup();
        if (setup != null)
            setupName.setText(setup.getName());

        boolean tryRefresh = false; // future feature
        RoomSetup roomSetup = saveManager.getRoom(template);
        if (roomSetup != null)
            tryRefresh = true;

        setupName.setFont(FontManager.getRunescapeBoldFont());

        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel headerControlPanel = new JPanel();
        headerControlPanel.setLayout(new BoxLayout(headerControlPanel, BoxLayout.X_AXIS));

        JButton updateAll = createButton(SETUP_ICON, SETUP_ICON_HOVERED, true, () -> {
            saveManager.saveAll(template, saved -> {
                if (saved) {
                    build();
                    inventoryPanel.build(template, roomSetup);
                    equipmentPanel.build(template, roomSetup);
                }
                else
                    JOptionPane.showMessageDialog(
                            null,
                            FAILED_MSG,
                            FAILED_TITLE,
                            JOptionPane.ERROR_MESSAGE
                    );
            });
        });

        JButton updateInventory = createButton(BACKPACK_ICON, BACKPACK_ICON_HOVERED, true, () -> {
            saveManager.saveInventory(template, saved -> {
                if (saved) {
                    build();
                    inventoryPanel.build(template, roomSetup);
                }
                else
                    JOptionPane.showMessageDialog(
                            null,
                            FAILED_MSG,
                            FAILED_TITLE,
                            JOptionPane.ERROR_MESSAGE
                    );
            });
        });

        JButton updateEquipment = createButton(EQUIP_ICON, EQUIP_ICON_HOVERED, true, () -> {
            saveManager.saveEquipment(template, saved -> {
                if (saved)
                {
                    build();
                    equipmentPanel.build(template, roomSetup);
                }
                else
                    JOptionPane.showMessageDialog(
                            null,
                            FAILED_MSG,
                            FAILED_TITLE,
                            JOptionPane.ERROR_MESSAGE
                    );
            });
        });

        JButton exitPreset = createButton(EXIT_ICON, EXIT_ICON_HOVERED, false, () -> {
            closeSetup();
            elevatorPanel.build();
            plugin.requestClosePreset();
            saveManager.setActiveSetup(null);
        });

        headerControlPanel.add(updateAll);
        headerControlPanel.add(updateInventory);
        headerControlPanel.add(updateEquipment);
        headerControlPanel.add(exitPreset);

        headerPanel.add(setupName, BorderLayout.CENTER);
        headerPanel.add(headerControlPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JButton createButton(ImageIcon icon, ImageIcon hover, boolean needLogin, Runnable runnable)
    {
        JButton button = new JButton(icon);
        button.setRolloverIcon(hover);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(20, 20));
        if (needLogin)
            addListener(button, runnable);
        else
            button.addActionListener(e -> runnable.run());
        SwingUtil.removeButtonDecorations(button);
        return button;
    }

    private void addListener(JButton button, Runnable action)
    {
        button.addActionListener(e -> {
            if (!saveManager.loggedIn())
            {
                JOptionPane.showMessageDialog(
                        null,
                        "You must be logged in to update a preset.",
                        "Not Logged In",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (template == null)
            {
                JOptionPane.showMessageDialog(
                        null,
                        "You must have a room selected to update it.",
                        "No Room Selected",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to update this preset?",
                    "Update Preset",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION)
            {
                action.run();
            }
        });
    }

    public void build()
    {
        removeAll();

        add(createHeader());
        add(Box.createVerticalStrut(15));

        addCenteredComp(elevatorPanel);
        add(Box.createVerticalStrut(5));

        if (template == null)
        {
            PluginErrorPanel errorPanel = new PluginErrorPanel();
            errorPanel.setContent(
                    "No room selected",
                    "Select a room from the above panel, and the setup will be loaded here."
            );
            addCenteredComp(errorPanel);

            return;
        }

        JLabel templateLabel = new JLabel(template.getRoomName());
        templateLabel.setFont(FontManager.getDefaultBoldFont());

        addCenteredComp(templateLabel);
        add(Box.createVerticalStrut(5));

        addCenteredComp(equipmentLabel);
        addCenteredComp(equipmentPanel);
        add(Box.createVerticalStrut(5));

        addCenteredComp(inventoryLabel);
        addCenteredComp(inventoryPanel);
        add(Box.createVerticalStrut(5));

        addCenteredComp(withdrawLabel);
        addCenteredComp(withdrawPanel);
        add(Box.createVerticalStrut(5));

        addCenteredComp(depositLabel);
        addCenteredComp(depositPanel);

        revalidate();
        repaint();
    }

    private void addCenteredComp(JComponent component)
    {
        component.setAlignmentX(CENTER_ALIGNMENT);
        component.setMaximumSize(component.getPreferredSize());
        add(component);
        add(Box.createVerticalStrut(5));
    }

    public void loadFromSetup(InstanceTemplate template, RoomSetup setup)
    {
        this.template = template;

        elevatorPanel.setFloor(template.getPlayerFloor());

        equipmentPanel.loadFromSetup(template, setup);
        inventoryPanel.loadFromSetup(template, setup);
        depositPanel.loadFromSetup(template, setup);
        withdrawPanel.loadFromSetup(template, setup);

        build();
        revalidate();
        repaint();
    }

    private void closeSetup()
    {
        this.template = null;
        revalidate();
        repaint();
    }
}
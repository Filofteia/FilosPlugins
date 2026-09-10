package filo.cm.checklist.panel.comp.preset;

import com.google.gson.Gson;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.save.RaidSetup;
import filo.cm.checklist.util.SaveManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class PresetEntry extends JPanel
{
    private static final ImageIcon RENAME_PRESET;
    private static final ImageIcon OPEN_PRESET;
    private static final ImageIcon DELETE_PRESET;
    private static final ImageIcon RENAME_PRESET_HOVERED;
    private static final ImageIcon OPEN_PRESET_HOVERED;
    private static final ImageIcon DELETE_PRESET_HOVERED;

    static
    {
        final BufferedImage renameIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "rename.png");
        final BufferedImage openIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "visible_icon.png");
        final BufferedImage deleteIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "cancel_icon.png");
        RENAME_PRESET = new ImageIcon(renameIcon);
        OPEN_PRESET = new ImageIcon(openIcon);
        DELETE_PRESET = new ImageIcon(deleteIcon);
        RENAME_PRESET_HOVERED = new ImageIcon(ImageUtil.alphaOffset(renameIcon, -220));
        OPEN_PRESET_HOVERED = new ImageIcon(ImageUtil.alphaOffset(openIcon, -220));
        DELETE_PRESET_HOVERED = new ImageIcon(ImageUtil.alphaOffset(deleteIcon, -220));
    }

    private JPanel leftPanel;
    private FlatTextField presetLabel;
    private JLabel presetLabelR;

    private JPanel rightPanel;
    private JButton editNameButton;
    private JButton openPresetButton;
    private JButton deletePresetButton;

    private SaveManager saveManager;
    private String name;

    private CMChecklistPlugin plugin;
    public PresetEntry(CMChecklistPlugin plugin, SaveManager saveManager, RaidSetup setup) {
        this.plugin = plugin;
        this.name = setup.getName();
        this.saveManager = saveManager;
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(ColorScheme.BORDER_COLOR);
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(5, 5));
        rightPanel.setBackground(ColorScheme.BORDER_COLOR);
        rightPanel.setOpaque(true);

        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(5, 5));
        leftPanel.setOpaque(true);

        JPopupMenu menu = createPresetMenu(setup);
        leftPanel.setComponentPopupMenu(menu);
        setComponentPopupMenu(menu);

        editNameButton = createButton(RENAME_PRESET, RENAME_PRESET_HOVERED, () -> {
            showRenameField(setup);
        });

        openPresetButton = createButton(OPEN_PRESET, OPEN_PRESET_HOVERED, () -> {
            cancel(setup);
            plugin.requestStoragePanel(setup);
        });

        deletePresetButton = createButton(DELETE_PRESET, DELETE_PRESET_HOVERED, () -> {
            plugin.requestDeletePreset(setup);
        });

        rightPanel.add(editNameButton, BorderLayout.WEST);
        rightPanel.add(openPresetButton, BorderLayout.CENTER);
        rightPanel.add(deletePresetButton, BorderLayout.EAST);

        showRenameField(setup); //just to make it iinitailized
        showLabel(setup);

        add(rightPanel, BorderLayout.EAST);
        add(leftPanel, BorderLayout.CENTER);
    }

    private JButton createButton(ImageIcon icon, ImageIcon hovered, Runnable runnable)
    {
        JButton button = new JButton(icon);
        button.setRolloverIcon(hovered);
        button.setPreferredSize(new Dimension(16, 16));
        button.setOpaque(true);
        button.addActionListener(e -> runnable.run());
        SwingUtil.removeButtonDecorations(button);

        return button;
    }

    private void showLabel(RaidSetup setup)
    {
        presetLabelR = new JLabel(setup.getName());
        presetLabelR.setOpaque(true);
        presetLabelR.setBackground(ColorScheme.BORDER_COLOR);
        presetLabelR.setToolTipText(setup.getName());
        presetLabelR.setComponentPopupMenu(createPresetMenu(setup));

        leftPanel.removeAll();
        leftPanel.add(presetLabelR, BorderLayout.CENTER);
        leftPanel.revalidate();
        leftPanel.repaint();
    }

    private void showRenameField(RaidSetup setup)
    {
        presetLabel = new FlatTextField();
        presetLabel.setText(setup.getName());
        presetLabel.setBorder(null);
        presetLabel.setEditable(true);
        presetLabel.setBackground(ColorScheme.BORDER_COLOR);
        presetLabel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    save(setup);
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel(setup);
                }
            }
        });

        leftPanel.removeAll();
        leftPanel.add(presetLabel, BorderLayout.CENTER);
        leftPanel.revalidate();
        leftPanel.repaint();

        presetLabel.requestFocusInWindow();
    }

    private JPopupMenu createPresetMenu(RaidSetup setup)
    {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem export = new JMenuItem("Export Preset");
        export.addActionListener(e -> copySetupToClipboard(setup));

        JMenuItem moveup = new JMenuItem("Move  Up");
        moveup.addActionListener(e -> {
            saveManager.moveSetup(setup, -1);
            plugin.rebuildPresetPanel();
        });

        JMenuItem movedown = new JMenuItem("Move Down");
        movedown.addActionListener(e -> {
            saveManager.moveSetup(setup, 1);
            plugin.rebuildPresetPanel();
        });

        menu.add(export);
        menu.addSeparator();
        menu.add(moveup);
        menu.add(movedown);

        return menu;
    }

    private void copySetupToClipboard(RaidSetup setup)
    {
        Gson gson = new Gson();
        String json = gson.toJson(setup);
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(json), null);
    }

    public void save(RaidSetup setup)
    {
        presetLabel.setEditable(false);
        name = presetLabel.getText();
        boolean renamed = saveManager.renameSetup(setup.getId(), name);

        if (!renamed)
            presetLabel.setText(setup.getName());

        showLabel(setup);
    }

    public void cancel(RaidSetup setup)
    {
        presetLabel.setEditable(false);
        presetLabel.setText(setup.getName());
        showLabel(setup);
    }
}

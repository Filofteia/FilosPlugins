package filo.cm.checklist.panel.comp.preset;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.save.RaidSetup;
import filo.cm.checklist.util.SaveManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PresetPanel extends JPanel
{
    private CMChecklistPlugin plugin;
    private SaveManager saveManager;
    private Gson gson;

    private IconTextField searchBar = new IconTextField();
    private JPanel presetListPanel = new JPanel();

    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_ICON_HOVERED;
    private static final ImageIcon ADD_IMPORT;
    private static final ImageIcon ADD_IMPORT_HOVERED;

    static
    {
        final BufferedImage renameIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "add_icon.png");
        final BufferedImage clipboardIcon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "clipboard_add.png");
        ADD_ICON = new ImageIcon(renameIcon);
        ADD_ICON_HOVERED = new ImageIcon(ImageUtil.alphaOffset(renameIcon, -220));
        ADD_IMPORT = new ImageIcon(clipboardIcon);
        ADD_IMPORT_HOVERED = new ImageIcon(ImageUtil.alphaOffset(clipboardIcon, -220));
    }

    public PresetPanel(CMChecklistPlugin plugin, SaveManager saveManager) {
        this.plugin = plugin;
        this.saveManager = saveManager;
        this.gson = plugin.getGson();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headerLabel = new JLabel("CM Storage");
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new BorderLayout());

        JButton createButton = createButton(ADD_ICON, ADD_ICON_HOVERED, () -> {
            String name = JOptionPane.showInputDialog("Enter a name for the new setup:");
            if (name != null && !name.isEmpty())
            {
                saveManager.createSetup(name);
                refreshPresets(presetListPanel, searchBar);
            }
        });

        JButton importButton = createButton(ADD_IMPORT, ADD_IMPORT_HOVERED, () -> {
            RaidSetup importedSetup = requestClipboardSetup();
            if (importedSetup == null)
                return;
            if (importedSetup.getId() == null)
                return;

            saveManager.importSetup(importedSetup);
            refreshPresets(presetListPanel, searchBar);
        });

        buttonPanel.add(createButton, BorderLayout.WEST);
        buttonPanel.add(importButton, BorderLayout.EAST);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel);
        add(Box.createVerticalStrut(5));

        presetListPanel.setLayout(new BoxLayout(presetListPanel, BoxLayout.Y_AXIS));
        presetListPanel.add(Box.createVerticalGlue());

        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));

        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                refreshPresets(presetListPanel, searchBar);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                refreshPresets(presetListPanel, searchBar);
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                refreshPresets(presetListPanel, searchBar);
            }
        });

        add(searchBar);

        add(Box.createVerticalStrut(7));
        if (saveManager.getRaidSetupList() != null) {
            for (RaidSetup room : saveManager.getRaidSetupList()) {
                presetListPanel.add(new PresetEntry(plugin, saveManager, room));
                presetListPanel.add(Box.createVerticalStrut(3));
            }
        }
        add(presetListPanel);
    }

    private void refreshPresets(JPanel presetListPanel, IconTextField searchBar)
    {
        SwingUtilities.invokeLater(() -> {
            presetListPanel.removeAll();

            if (saveManager.getRaidSetupList() != null) {
                for (RaidSetup room : searchResults(saveManager.getRaidSetupList(), searchBar.getText())) {
                    presetListPanel.add(new PresetEntry(plugin, saveManager, room));
                    presetListPanel.add(Box.createVerticalStrut(3));
                }
            }

            presetListPanel.revalidate();
        });
    }

    private List<RaidSetup> searchResults(List<RaidSetup> presetList, String query)
    {
        if (query.isBlank())
            return presetList;

        return presetList.stream()
                .filter(e -> e.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    private JButton createButton(ImageIcon icon, ImageIcon hover, Runnable runnable)
    {
        JButton button = new JButton(icon);
        button.setRolloverIcon(hover);
        button.setPreferredSize(new Dimension(20, 20));
        button.setOpaque(true);
        button.addActionListener(e -> runnable.run());
        SwingUtil.removeButtonDecorations(button);
        return button;
    }

    private RaidSetup requestClipboardSetup()
    {
        String clipboard = requestClipboard();
        if (clipboard == null)
            return null;

        RaidSetup importedSetup = null;
        try
        {
            importedSetup = gson.fromJson(clipboard, RaidSetup.class);
        }
        catch (JsonSyntaxException ex)
        {
            log.warn(ex.getMessage());
        }


        return importedSetup;
    }

    private String requestClipboard()
    {
        String clipboard;
        try
        {
            clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor)
                    .toString();
        }
        catch (IOException | UnsupportedFlavorException ex)
        {
            throw new RuntimeException(ex);
        }

        return clipboard;
    }

    public void build()
    {
        presetListPanel.removeAll();
        refreshPresets(presetListPanel, searchBar);
    }
}

package filo.cm.checklist.util;

import filo.cm.checklist.data.PotionRole;
import filo.cm.checklist.data.PotionType;
import net.runelite.api.Item;

public class PotionUtil {
    public static PotionRole getRole(int itemId)
    {
        PotionType type = PotionType.fromItemId(itemId);
        if (type != null)
            return type.getRole();

        return null;
    }

    public static int getPotionQuantity(int minDose, Item[] items, PotionType... types)
    {
        int potionQuantity = 0;
        for (Item item : items)
        {
            int widgetId = item.getId();
            for (PotionType potionType : types)
            {
                if (potionType.contains(widgetId) && potionType.getDoses(widgetId) >= minDose)
                    potionQuantity++;
            }
        }

        return potionQuantity;
    }

    public static int getPotionQuantity(int minDose, Item[] items, PotionRole role)
    {
        int potionQuantity = 0;
        for (Item item : items)
        {
            int widgetId = item.getId();

            PotionType itemType = PotionType.fromItemId(widgetId);
            if (itemType != null)
            {
                if (itemType.getRole() == role)
                    potionQuantity++;
            }
        }

        return potionQuantity;
    }
}

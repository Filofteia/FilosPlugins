package filo.cm.checklist.data;

import lombok.Getter;
import net.runelite.api.gameval.ItemID;

public enum PotionType {
    SARADOMIN_BREW(PotionRole.BREW, ItemID._4DOSEPOTIONOFSARADOMIN, ItemID._3DOSEPOTIONOFSARADOMIN, ItemID._2DOSEPOTIONOFSARADOMIN, ItemID._1DOSEPOTIONOFSARADOMIN),
    SUPER_RESTORE(PotionRole.RESTORE, ItemID._4DOSE2RESTORE, ItemID._3DOSE2RESTORE, ItemID._2DOSE2RESTORE, ItemID._1DOSE2RESTORE),
    STAMINA(ItemID._4DOSESTAMINA, ItemID._3DOSESTAMINA, ItemID._2DOSESTAMINA, ItemID._1DOSESTAMINA),
    EXTENDED_STAMINA(ItemID._4DOSE2STAMINA, ItemID._3DOSE2STAMINA, ItemID._2DOSE2STAMINA, ItemID._1DOSE2STAMINA),
    ANTIPOISON_PP(ItemID._4DOSE2ANTIPOISON, ItemID._3DOSE2ANTIPOISON, ItemID._2DOSE2ANTIPOISON, ItemID._1DOSE2ANTIPOISON),

    OVERLOAD_STRONG(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_4, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_3, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_2, ItemID.RAIDS_VIAL_OVERLOAD_STRONG_1),
    OVERLOAD(ItemID.RAIDS_VIAL_OVERLOAD_4, ItemID.RAIDS_VIAL_OVERLOAD_3, ItemID.RAIDS_VIAL_OVERLOAD_2, ItemID.RAIDS_VIAL_OVERLOAD_1),
    OVERLOAD_WEAK(ItemID.RAIDS_VIAL_OVERLOAD_WEAK_4, ItemID.RAIDS_VIAL_OVERLOAD_WEAK_3, ItemID.RAIDS_VIAL_OVERLOAD_WEAK_2, ItemID.RAIDS_VIAL_OVERLOAD_WEAK_1),

    PRAYER_ENHANCE_STRONG(ItemID.RAIDS_VIAL_PRAYER_STRONG_4, ItemID.RAIDS_VIAL_PRAYER_STRONG_3, ItemID.RAIDS_VIAL_PRAYER_STRONG_2, ItemID.RAIDS_VIAL_PRAYER_STRONG_1),
    PRAYER_ENHANCE(ItemID.RAIDS_VIAL_PRAYER_4, ItemID.RAIDS_VIAL_PRAYER_3, ItemID.RAIDS_VIAL_PRAYER_2, ItemID.RAIDS_VIAL_PRAYER_1),
    PRAYER_ENHANCE_WEAK(ItemID.RAIDS_VIAL_PRAYER_WEAK_4, ItemID.RAIDS_VIAL_PRAYER_WEAK_3, ItemID.RAIDS_VIAL_PRAYER_WEAK_2, ItemID.RAIDS_VIAL_PRAYER_WEAK_1),

    XERICS_AID_STRONG(PotionRole.BREW, ItemID.RAIDS_VIAL_XERICAID_STRONG_4, ItemID.RAIDS_VIAL_XERICAID_STRONG_3, ItemID.RAIDS_VIAL_XERICAID_STRONG_2, ItemID.RAIDS_VIAL_XERICAID_STRONG_1),
    XERICS_AID(PotionRole.BREW, ItemID.RAIDS_VIAL_XERICAID_4, ItemID.RAIDS_VIAL_XERICAID_3, ItemID.RAIDS_VIAL_XERICAID_2, ItemID.RAIDS_VIAL_XERICAID_1),
    XERICS_AID_WEAK(PotionRole.BREW, ItemID.RAIDS_VIAL_XERICAID_WEAK_4, ItemID.RAIDS_VIAL_XERICAID_WEAK_3, ItemID.RAIDS_VIAL_XERICAID_WEAK_2, ItemID.RAIDS_VIAL_XERICAID_WEAK_1),

    REVITALISATION_STRONG(PotionRole.RESTORE, ItemID.RAIDS_VIAL_REVITALISATION_STRONG_4, ItemID.RAIDS_VIAL_REVITALISATION_STRONG_3, ItemID.RAIDS_VIAL_REVITALISATION_STRONG_2, ItemID.RAIDS_VIAL_REVITALISATION_STRONG_1),
    REVITALISATION(PotionRole.RESTORE, ItemID.RAIDS_VIAL_REVITALISATION_4, ItemID.RAIDS_VIAL_REVITALISATION_3, ItemID.RAIDS_VIAL_REVITALISATION_2, ItemID.RAIDS_VIAL_REVITALISATION_1),
    REVITALISATION_WEAK(PotionRole.RESTORE, ItemID.RAIDS_VIAL_REVITALISATION_WEAK_4, ItemID.RAIDS_VIAL_REVITALISATION_WEAK_3, ItemID.RAIDS_VIAL_REVITALISATION_WEAK_2, ItemID.RAIDS_VIAL_REVITALISATION_WEAK_1);

    private final int[] itemIds;
    @Getter
    private final PotionRole role;

    PotionType(PotionRole role, int... itemIds) {
        this.itemIds = itemIds;
        this.role = role;
    }

    PotionType(int... itemIds) {
        this.itemIds = itemIds;
        this.role = PotionRole.OTHER;
    }

    public int getDoses(int itemId)
    {
        for (int i = 0; i < itemIds.length; i++)
        {
            if (itemId == itemIds[i])
                return 4 - i;
        }

        return -1;
    }

    public static PotionType fromItemId(int itemId)
    {
        for (PotionType type : values())
        {
            if (type.contains(itemId))
                return type;
        }

        return null;
    }

    public static boolean isPotionMatch(int reqId, int itemId)
    {
        PotionType reqType = fromItemId(reqId);
        PotionType itemType = fromItemId(itemId);
        if (reqType == null || itemType == null)
            return false;

        return reqType == itemType;
    }

    public boolean contains(int itemId)
    {
        return getDoses(itemId) != -1;
    }
}


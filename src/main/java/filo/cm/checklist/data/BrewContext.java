package filo.cm.checklist.data;

import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.PotionUtil;
import lombok.Getter;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.ItemID;

@Getter
public class BrewContext {
    boolean skipRoom = false;
    private final int requiredBrews;
    private final int currentBrews;
    private final int missingBrews;
    private final int surplusBrews;
    private final int requiredRestores;
    private final int currentRestores;
    private final int missingRestores;
    private final int surplusRestores;

    // No clue how impactful this is, but it felt better than checking a player's inventory per item per frame
    public BrewContext(RoomSetup roomSetup, ItemContainer playerInventory) {
        if (roomSetup.getInventoryItems().isEmpty())
            skipRoom = true;

        this.requiredBrews = roomSetup.getBrewCount();
        this.currentBrews = PotionUtil.getPotionQuantity(1, playerInventory.getItems(), PotionType.SARADOMIN_BREW, PotionType.XERICS_AID_STRONG);

        this.requiredRestores = roomSetup.getRestCount();
        this.currentRestores = PotionUtil.getPotionQuantity(1, playerInventory.getItems(), PotionType.SUPER_RESTORE, PotionType.REVITALISATION_STRONG, PotionType.REVITALISATION, PotionType.REVITALISATION_WEAK);

        this.missingBrews = Math.max(0, requiredBrews - currentBrews);
        this.missingRestores = Math.max(0, requiredRestores - currentRestores);
        this.surplusBrews = Math.max(0, currentBrews - requiredBrews);
        this.surplusRestores = Math.max(0, currentRestores - requiredRestores);
    }
}

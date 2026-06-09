package gripe._90.appliede.client.screen;

import java.util.List;

import ae2.api.config.FuzzyMode;
import ae2.api.config.Settings;
import ae2.client.gui.Icon;
import ae2.client.gui.implementations.GuiUpgradeable;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.widgets.IconButton;
import ae2.client.gui.widgets.ServerSettingToggleButton;
import ae2.client.gui.widgets.SettingToggleButton;
import ae2.container.SlotSemantics;
import ae2.core.definitions.AEItems;
import ae2.core.localization.ButtonToolTips;
import ae2.core.localization.GuiText;
import gripe._90.appliede.menu.EMCInterfaceMenu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;

public class EMCInterfaceScreen extends GuiUpgradeable<EMCInterfaceMenu> {
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final List<SetAmountButton> amountButtons = new ObjectArrayList<>();

    public EMCInterfaceScreen(EMCInterfaceMenu menu, InventoryPlayer playerInventory, ITextComponent title, GuiStyle style) {
        super(menu, playerInventory, title, style);

        fuzzyMode = addToLeftToolbar(new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL));

        var configSlots = menu.getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < configSlots.size(); i++) {
            int slotIndex = i;
            SetAmountButton button = new SetAmountButton(() -> {
                var configSlot = (ae2.container.slot.AppEngSlot) menu.getSlots(SlotSemantics.CONFIG).get(slotIndex);
                menu.openSetAmountMenu(configSlot.getSlotIndex());
            });
            button.setDisableBackground(true);
            button.setMessage(ButtonToolTips.InterfaceSetStockAmount.text());
            widgets.add("amtButton" + (i + 1), button);
            amountButtons.add(button);
        }
    }

    private static void repositionRowPair(List<Slot> slots, int firstRowY, int secondRowY) {
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            int row = i / 9;
            int col = i % 9;
            slot.xPos = 8 + col * 18;
            slot.yPos = row == 0 ? firstRowY : secondRowY;
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        repositionRowPair(container.getSlots(SlotSemantics.CONFIG), 53, 113);
        repositionRowPair(container.getSlots(SlotSemantics.STORAGE), 71, 131);

        fuzzyMode.set(container.getFuzzyMode());
        fuzzyMode.setVisibility(container.hasUpgrade(AEItems.FUZZY_CARD.item()));

        var configSlots = container.getSlots(SlotSemantics.CONFIG);
        for (int i = 0; i < amountButtons.size(); i++) {
            amountButtons.get(i).setVisibility(!configSlots.get(i).getStack().isEmpty());
        }
    }

    static class SetAmountButton extends IconButton {
        SetAmountButton(Runnable onPress) {
            super(onPress);
        }

        @Override
        protected Icon getIcon() {
            return hovered ? Icon.COG : Icon.COG_DISABLED;
        }
    }
}

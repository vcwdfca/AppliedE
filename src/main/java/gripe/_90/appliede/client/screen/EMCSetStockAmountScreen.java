package gripe._90.appliede.client.screen;

import ae2.client.gui.AEBaseGui;
import ae2.client.gui.NumberEntryType;
import ae2.client.gui.implementations.AESubGui;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.widgets.NumberEntryWidget;
import ae2.core.localization.GuiText;
import gripe._90.appliede.menu.EMCSetStockAmountMenu;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.ITextComponent;

public class EMCSetStockAmountScreen extends AEBaseGui<EMCSetStockAmountMenu> {
    private final NumberEntryWidget amount;
    private boolean amountInitialized;

    public EMCSetStockAmountScreen(
        EMCSetStockAmountMenu menu,
        InventoryPlayer playerInventory,
        ITextComponent title,
        GuiStyle style
    ) {
        super(menu, playerInventory, style);

        widgets.addButton("save", GuiText.Set.text(), this::confirm);
        AESubGui.addBackButton(menu, "back", widgets);

        amount = widgets.addNumberEntryWidget("amountToStock", NumberEntryType.UNITLESS);
        amount.setLongValue(1);
        amount.setTextFieldStyle(style.getWidget("amountToStockInput"));
        amount.setMinValue(0);
        amount.setHideValidationIcon(true);
        amount.setOnConfirm(this::confirm);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (!amountInitialized) {
            var whatToStock = container.getWhatToStock();
            if (whatToStock != null) {
                amount.setType(NumberEntryType.of(whatToStock));
                amount.setLongValue(container.getInitialAmount());
                amount.setMaxValue(container.getMaxAmount());
                amountInitialized = true;
            }
        }
    }

    private void confirm() {
        amount.getIntValue().ifPresent(container::confirm);
    }
}

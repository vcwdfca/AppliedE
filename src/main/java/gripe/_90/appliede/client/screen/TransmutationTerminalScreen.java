package gripe._90.appliede.client.screen;

import java.util.List;

import ae2.client.gui.Icon;
import ae2.client.gui.me.common.GuiMEStorage;
import ae2.client.gui.style.GuiStyle;
import ae2.client.gui.widgets.IconButton;
import ae2.client.gui.widgets.ToggleButton;
import gripe._90.appliede.AppliedENetwork;
import gripe._90.appliede.me.misc.LearnAllItemsPacket;
import gripe._90.appliede.menu.TransmutationTerminalMenu;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TransmutationTerminalScreen extends GuiMEStorage<TransmutationTerminalMenu> {
    private final ToggleButton shiftMode;

    public TransmutationTerminalScreen(
        TransmutationTerminalMenu menu,
        InventoryPlayer playerInventory,
        ITextComponent title,
        GuiStyle style
    ) {
        super(menu, playerInventory, title, style);

        shiftMode = new ToggleButton(Icon.REGULATE_STOCK_ON, Icon.REGULATE_STOCK_OFF, menu::setShiftToTransmute);
        shiftMode.setTooltipOn(List.of(
            new TextComponentTranslation("gui.appliede.shift_transmuting"),
            new TextComponentTranslation("gui.appliede.toggle_storage")));
        shiftMode.setTooltipOff(List.of(
            new TextComponentTranslation("gui.appliede.shift_storing"),
            new TextComponentTranslation("gui.appliede.toggle_transmutation")));
        widgets.add("toggleShiftToTransmute", shiftMode);

        IconButton learnAll = new IconButton(() -> AppliedENetwork.CHANNEL.sendToServer(LearnAllItemsPacket.INSTANCE)) {
            @Override
            protected Icon getIcon() {
                return Icon.CRAFT_HAMMER;
            }
        };
        learnAll.setMessage(new TextComponentTranslation("gui.appliede.learn_all"));
        widgets.add("learnAllItems", learnAll);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        shiftMode.setState(container.shiftToTransmute);
        setTextContent("learned", container.learnedLabelTicks > 0
            ? new TextComponentTranslation("gui.appliede.learned")
            : null);
        setTextContent("unlearned", container.unlearnedLabelTicks > 0
            ? new TextComponentTranslation("gui.appliede.unlearned")
            : null);

        if (container.learnedLabelTicks > 0) {
            container.decrementLearnedTicks();
        }
        if (container.unlearnedLabelTicks > 0) {
            container.decrementUnlearnedTicks();
        }
    }
}

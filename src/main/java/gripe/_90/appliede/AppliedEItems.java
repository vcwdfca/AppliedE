package gripe._90.appliede;

import java.util.function.Function;
import java.util.function.IntSupplier;

import ae2.api.parts.IPart;
import ae2.api.parts.IPartItem;
import ae2.api.parts.PartModels;
import ae2.core.AEConfig;
import ae2.api.upgrades.Upgrades;
import ae2.items.parts.PartItem;
import ae2.items.parts.PartModelsHelper;
import gripe._90.appliede.item.WirelessTransmutationTerminalItem;
import gripe._90.appliede.part.EMCExportBusPart;
import gripe._90.appliede.part.EMCImportBusPart;
import gripe._90.appliede.part.EMCInterfacePart;
import gripe._90.appliede.part.EMCModulePart;
import gripe._90.appliede.part.TransmutationTerminalPart;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public final class AppliedEItems {
    public static final Item EMC_MODULE = item("emc_module", part(EMCModulePart.class, EMCModulePart::new));
    public static final ItemBlock EMC_INTERFACE = blockItem("emc_interface", new ItemBlock(AppliedEBlocks.EMC_INTERFACE));
    public static final Item CABLE_EMC_INTERFACE =
        item("cable_emc_interface", part(EMCInterfacePart.class, EMCInterfacePart::new));
    public static final Item EMC_EXPORT_BUS = item("emc_export_bus", part(EMCExportBusPart.class, EMCExportBusPart::new));
    public static final Item EMC_IMPORT_BUS = item("emc_import_bus", part(EMCImportBusPart.class, EMCImportBusPart::new));
    public static final Item TRANSMUTATION_TERMINAL =
        item("transmutation_terminal", part(TransmutationTerminalPart.class, TransmutationTerminalPart::new));
    public static final Item LEARNING_CARD = item("learning_card", Upgrades.createUpgradeCardItem());
    public static final Item DUMMY_EMC_ITEM = item("dummy_emc_item", new Item());
    public static final WirelessTransmutationTerminalItem WIRELESS_TRANSMUTATION_TERMINAL =
        item("wireless_transmutation_terminal",
            new WirelessTransmutationTerminalItem(getWirelessTerminalBattery()));

    private AppliedEItems() {
    }

    public static void register(IForgeRegistry<Item> registry) {
        registry.register(EMC_INTERFACE);
        registry.register(EMC_MODULE);
        registry.register(CABLE_EMC_INTERFACE);
        registry.register(EMC_EXPORT_BUS);
        registry.register(EMC_IMPORT_BUS);
        registry.register(TRANSMUTATION_TERMINAL);
        registry.register(LEARNING_CARD);
        registry.register(DUMMY_EMC_ITEM);
        registry.register(WIRELESS_TRANSMUTATION_TERMINAL);
    }

    private static ItemBlock blockItem(String id, ItemBlock item) {
        item.setRegistryName(AppliedE.id(id));
        item.setTranslationKey(AppliedE.MODID + "." + id);
        item.setCreativeTab(CreativeTabs.MISC);
        return item;
    }

    private static <T extends Item> T item(String id, T item) {
        item.setRegistryName(AppliedE.id(id));
        item.setTranslationKey(AppliedE.MODID + "." + id);
        item.setCreativeTab(CreativeTabs.MISC);
        return item;
    }

    private static <P extends IPart> PartItem<P> part(Class<P> partClass, Function<IPartItem<P>, P> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return new PartItem<>(partClass, factory);
    }

    private static double getWirelessTerminalBattery() {
        return getConfiguredBattery(AEConfig.instance()::getWirelessTerminalBattery, 1600000);
    }

    private static double getConfiguredBattery(IntSupplier supplier, double fallback) {
        try {
            return supplier.getAsInt();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }
}

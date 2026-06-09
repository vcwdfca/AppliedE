package gripe._90.appliede;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = AppliedE.MODID)
public final class AppliedERegistry {
    private AppliedERegistry() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        AppliedEBlocks.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        AppliedEItems.register(event.getRegistry());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerInventoryModel(AppliedEItems.EMC_INTERFACE);
        registerInventoryModel(AppliedEItems.EMC_MODULE);
        registerInventoryModel(AppliedEItems.CABLE_EMC_INTERFACE);
        registerInventoryModel(AppliedEItems.EMC_EXPORT_BUS);
        registerInventoryModel(AppliedEItems.EMC_IMPORT_BUS);
        registerInventoryModel(AppliedEItems.TRANSMUTATION_TERMINAL);
        registerInventoryModel(AppliedEItems.LEARNING_CARD);
        registerInventoryModel(AppliedEItems.DUMMY_EMC_ITEM);
        registerInventoryModel(AppliedEItems.WIRELESS_TRANSMUTATION_TERMINAL);
    }

    private static void registerInventoryModel(Item item) {
        ResourceLocation id = item.getRegistryName();
        if (id != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(id, "inventory"));
        }
    }
}

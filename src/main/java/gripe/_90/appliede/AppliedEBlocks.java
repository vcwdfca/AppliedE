package gripe._90.appliede;

import gripe._90.appliede.block.EMCInterfaceBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.registries.IForgeRegistry;

public final class AppliedEBlocks {
    public static final EMCInterfaceBlock EMC_INTERFACE = block("emc_interface", new EMCInterfaceBlock());

    private AppliedEBlocks() {
    }

    public static void register(IForgeRegistry<Block> registry) {
        registry.register(EMC_INTERFACE);
    }

    private static <T extends Block> T block(String id, T block) {
        block.setRegistryName(AppliedE.id(id));
        block.setTranslationKey(AppliedE.MODID + "." + id);
        block.setCreativeTab(CreativeTabs.MISC);
        return block;
    }
}

package gripe._90.appliede;

import java.math.BigInteger;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.networking.GridServices;
import ae2.api.stacks.AEKeyTypes;
import gripe._90.appliede.gui.AppliedEGuiHandler;
import gripe._90.appliede.me.key.EMCKey;
import gripe._90.appliede.me.key.EMCKeyType;
import gripe._90.appliede.me.misc.EMCContainerItemStrategy;
import gripe._90.appliede.me.service.KnowledgeService;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = AppliedE.MODID,
    name = AppliedE.NAME,
    version = AppliedE.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:ae2;required-after:projecte"
)
public final class AppliedE {
    public static final String MODID = Tags.MOD_ID;
    public static final String NAME = Tags.MOD_NAME;
    public static final String VERSION = Tags.VERSION;
    public static final BigInteger TIER_LIMIT = BigInteger.valueOf(1_000_000_000_000L);
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static AppliedE INSTANCE;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} preInit", NAME);
        AppliedEConfig.init(event.getSuggestedConfigurationFile());
        AppliedENetwork.init();
        AppliedEBlockEntities.init();
        AppliedEWireless.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new AppliedEGuiHandler());
        MinecraftForge.EVENT_BUS.register(AppliedEProjectEEvents.class);
        AEKeyTypes.register(EMCKeyType.TYPE);
        if (event.getSide().isClient()) {
            gripe._90.appliede.client.AppliedEClient.init();
        }
        GridServices.register(KnowledgeService.class, KnowledgeService.class);
        ContainerItemStrategy.register(EMCKeyType.TYPE, EMCKey.class, EMCContainerItemStrategy.INSTANCE);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} init", NAME);
        AppliedEUpgrades.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("{} postInit", NAME);
        AppliedERecipes.initRuntimeHooks();
    }
}

package gripe._90.appliede;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public final class AppliedEConfig {
    public static AppliedEConfig CONFIG;

    private final Configuration configuration;
    private double moduleEnergyUsage;
    private double transmutationPowerMultiplier;
    private int emcPerByte;
    private boolean terminalExtractFromOwnEmcOnly;
    private int syncThrottleInterval;

    private AppliedEConfig(Configuration configuration) {
        this.configuration = configuration;
        load();
    }

    public static void init(File file) {
        CONFIG = new AppliedEConfig(new Configuration(file));
    }

    private void load() {
        moduleEnergyUsage = configuration.getFloat(
            "moduleEnergyUsage",
            Configuration.CATEGORY_GENERAL,
            25.0F,
            0.0F,
            Float.MAX_VALUE,
            "The amount of AE energy per tick used by the ME Transmutation Module.");
        transmutationPowerMultiplier = configuration.getFloat(
            "transmutationPowerMultiplier",
            Configuration.CATEGORY_GENERAL,
            1.0F,
            0.0F,
            Float.MAX_VALUE,
            "The amount of AE energy used to transmute items, per 2000 EMC.");
        emcPerByte = configuration.getInt(
            "emcPerByte",
            Configuration.CATEGORY_GENERAL,
            1_000_000,
            1,
            Integer.MAX_VALUE,
            "The number of EMC units per byte as used in AE2 auto-crafting.");
        terminalExtractFromOwnEmcOnly = configuration.getBoolean(
            "terminalExtractFromOwnEmcOnly",
            Configuration.CATEGORY_GENERAL,
            false,
            "When extracting items from a Transmutation Terminal via EMC, deduct EMC only from the player using it.");
        syncThrottleInterval = configuration.getInt(
            "syncThrottleInterval",
            Configuration.CATEGORY_GENERAL,
            20,
            1,
            200,
            "How many ticks to wait before the next player EMC sync when manipulating stored EMC.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public double getModuleEnergyUsage() {
        return moduleEnergyUsage;
    }

    public double getTransmutationPowerMultiplier() {
        return transmutationPowerMultiplier;
    }

    public int getEmcPerByte() {
        return emcPerByte;
    }

    public boolean terminalExtractFromOwnEmcOnly() {
        return terminalExtractFromOwnEmcOnly;
    }

    public int getSyncThrottleInterval() {
        return syncThrottleInterval;
    }
}

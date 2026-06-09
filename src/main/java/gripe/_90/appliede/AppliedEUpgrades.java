package gripe._90.appliede;

import ae2.api.upgrades.Upgrades;
import ae2.core.definitions.AEItems;

public final class AppliedEUpgrades {
    private static boolean initialized;

    private AppliedEUpgrades() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        Upgrades.add(AEItems.REDSTONE_CARD.item(), AppliedEItems.EMC_EXPORT_BUS, 1, "gui.appliede.io_buses");
        Upgrades.add(AEItems.CAPACITY_CARD.item(), AppliedEItems.EMC_EXPORT_BUS, 5, "gui.appliede.io_buses");
        Upgrades.add(AEItems.SPEED_CARD.item(), AppliedEItems.EMC_EXPORT_BUS, 4, "gui.appliede.io_buses");
        Upgrades.add(AEItems.REDSTONE_CARD.item(), AppliedEItems.EMC_IMPORT_BUS, 1, "gui.appliede.io_buses");
        Upgrades.add(AEItems.CAPACITY_CARD.item(), AppliedEItems.EMC_IMPORT_BUS, 5, "gui.appliede.io_buses");
        Upgrades.add(AEItems.SPEED_CARD.item(), AppliedEItems.EMC_IMPORT_BUS, 4, "gui.appliede.io_buses");
        Upgrades.add(AEItems.INVERTER_CARD.item(), AppliedEItems.EMC_IMPORT_BUS, 1, "gui.appliede.io_buses");
        Upgrades.add(AppliedEItems.LEARNING_CARD, AppliedEItems.EMC_INTERFACE, 1, "tile.appliede.emc_interface.name");
        Upgrades.add(AppliedEItems.LEARNING_CARD, AppliedEItems.CABLE_EMC_INTERFACE, 1, "tile.appliede.emc_interface.name");
        Upgrades.add(AppliedEItems.LEARNING_CARD, AppliedEItems.EMC_IMPORT_BUS, 1);
    }
}

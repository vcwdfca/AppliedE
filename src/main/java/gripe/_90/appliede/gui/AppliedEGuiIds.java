package gripe._90.appliede.gui;

public enum AppliedEGuiIds {
    TRANSMUTATION_TERMINAL,
    EMC_INTERFACE,
    EMC_SET_STOCK_AMOUNT,
    EMC_EXPORT_BUS,
    EMC_IMPORT_BUS,
    EMC_MODULE_PRIORITY,
    WIRELESS_TRANSMUTATION_TERMINAL;

    private static final int RETURNED_FROM_SUBSCREEN_FLAG = 1 << 30;

    public int id() {
        return ordinal();
    }

    public int id(boolean returnedFromSubScreen) {
        return returnedFromSubScreen ? id() | RETURNED_FROM_SUBSCREEN_FLAG : id();
    }

    public static boolean isReturnedFromSubScreen(int id) {
        return (id & RETURNED_FROM_SUBSCREEN_FLAG) != 0;
    }

    private static int baseId(int id) {
        return id & ~RETURNED_FROM_SUBSCREEN_FLAG;
    }

    public static AppliedEGuiIds fromId(int id) {
        AppliedEGuiIds[] values = values();
        id = baseId(id);
        if (id < 0 || id >= values.length) {
            return null;
        }
        return values[id];
    }
}

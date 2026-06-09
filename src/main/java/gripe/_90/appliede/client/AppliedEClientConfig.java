package gripe._90.appliede.client;

public class AppliedEClientConfig {
    public static final AppliedEClientConfig CONFIG = new AppliedEClientConfig();

    private static final int EMC_TIER_COLOURS = 10;

    private AppliedEClientConfig() {}

    public int getEmcTierColours() {
        return EMC_TIER_COLOURS;
    }
}

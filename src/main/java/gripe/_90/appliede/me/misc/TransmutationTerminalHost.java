package gripe._90.appliede.me.misc;

import ae2.api.networking.security.IActionHost;
import ae2.api.storage.ITerminalHost;

public interface TransmutationTerminalHost extends ITerminalHost, IActionHost {
    boolean getShiftToTransmute();

    void setShiftToTransmute(boolean shift);
}

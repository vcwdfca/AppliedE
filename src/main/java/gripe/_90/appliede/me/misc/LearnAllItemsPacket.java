package gripe._90.appliede.me.misc;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEItemKey;
import ae2.api.storage.MEStorage;
import gripe._90.appliede.me.service.KnowledgeService;
import gripe._90.appliede.menu.TransmutationTerminalMenu;
import io.netty.buffer.ByteBuf;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public class LearnAllItemsPacket implements IMessage {
    public static final LearnAllItemsPacket INSTANCE = new LearnAllItemsPacket();

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    private void handle(EntityPlayerMP sender) {
        if (!(sender.openContainer instanceof TransmutationTerminalMenu menu)) {
            return;
        }

        var node = menu.getHost().getActionableNode();
        if (node == null) {
            return;
        }

        var storage = node.grid().getStorageService();
        var knowledge = node.grid().getService(KnowledgeService.class);
        if (!knowledge.isTrackingPlayer(sender)) {
            return;
        }

        MEStorage me = storage.getInventory();
        for (var key : storage.getCachedInventory().keySet()) {
            if (!(key instanceof AEItemKey item)) {
                continue;
            }

            var stack = item.toStack();
            if (!ProjectEAPI.getEMCProxy().hasValue(stack)) {
                continue;
            }

            var providerSupplier = knowledge.getProviderFor(sender.getUniqueID());
            var provider = providerSupplier != null ? providerSupplier.get() : null;
            if (provider == null || provider.hasKnowledge(stack)) {
                continue;
            }

            var learned = knowledge.getStorage().insertItem(
                item,
                1,
                Actionable.MODULATE,
                IActionSource.ofPlayer(sender, menu.getHost()),
                true,
                true,
                menu::showLearned
            );

            if (learned == 0) {
                continue;
            }

            me.extract(item, learned, Actionable.MODULATE, IActionSource.ofMachine(menu.getHost()));
        }
    }

    public static final class Handler implements IMessageHandler<LearnAllItemsPacket, IMessage> {
        @Override
        public @Nullable IMessage onMessage(LearnAllItemsPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> message.handle(player));
            return null;
        }
    }
}

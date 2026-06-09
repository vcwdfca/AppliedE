package gripe._90.appliede.block;

import ae2.block.AEBaseTileBlock;
import ae2.util.InteractionUtil;
import gripe._90.appliede.gui.AppliedEGuiIds;
import gripe._90.appliede.gui.AppliedEGuiOpener;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EMCInterfaceBlock extends AEBaseTileBlock<EMCInterfaceBlockEntity> {
    public EMCInterfaceBlock() {
        super(Material.IRON);
        setHardness(2.2F);
        setResistance(11.0F);
        setTileEntity(EMCInterfaceBlockEntity.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }

        if (InteractionUtil.isInAlternateUseMode(player)) {
            return false;
        }

        EMCInterfaceBlockEntity tile = getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                AppliedEGuiOpener.openTileGui(player, AppliedEGuiIds.EMC_INTERFACE, tile);
            }
            return true;
        }

        return false;
    }
}

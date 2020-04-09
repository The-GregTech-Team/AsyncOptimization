package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.SideHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FMLCommonHandler.class, remap = false)
public class MixinFMLCommonHandler {
    public Side getEffectiveSide() {
        return SideHelper.side();
    }
}

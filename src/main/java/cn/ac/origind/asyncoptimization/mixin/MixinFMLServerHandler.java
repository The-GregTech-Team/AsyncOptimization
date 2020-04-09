package cn.ac.origind.asyncoptimization.mixin;

import cpw.mods.fml.server.FMLServerHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FMLServerHandler.class, remap = false)
public class MixinFMLServerHandler {
}

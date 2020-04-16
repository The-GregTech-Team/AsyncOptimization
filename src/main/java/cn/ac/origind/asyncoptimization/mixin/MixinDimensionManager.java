package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.ConcurrentArrayList;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(value = DimensionManager.class, remap = false)
public class MixinDimensionManager {
    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        ReflectionHelper.setPrivateValue(DimensionManager.class, null, new ConcurrentArrayList<Integer>(new ConcurrentLinkedQueue<>()), "unloadQueue");
    }
}

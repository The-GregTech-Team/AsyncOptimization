package cn.ac.origind.asyncoptimization.mixin;

import appeng.tile.AEBaseTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(targets = "appeng.hooks.TickHandler$HandlerRep", remap = false)
public abstract class MixinTickHandler {
    @Shadow
    private Queue<AEBaseTile> tiles;

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        tiles = new ConcurrentLinkedQueue<AEBaseTile>() {
            @Override
            public AEBaseTile poll() {
                AEBaseTile tile = super.poll();
                if (tile != null) return tile;
                else return new AEBaseTile() {
                    @Override
                    public boolean isInvalid() {
                        return true;
                    }
                };
            }
        };
    }
}

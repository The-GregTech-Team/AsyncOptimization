package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.IExtendedNetworkManager;
import cn.ac.origind.asyncoptimization.concurrent.IThreadListener;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager implements IExtendedNetworkManager {
    @Shadow
    public abstract INetHandler getNetHandler();

    @Shadow
    public abstract void processReceivedPackets();

    @Override
    public boolean isThreadRelated() {
        return getNetHandler() instanceof NetHandlerPlayServer;
    }

    @Nullable
    @Override
    public IThreadListener getThreadListener() {
        return Optional.ofNullable(getNetHandler())
                .filter(NetHandlerPlayServer.class::isInstance)
                .map(NetHandlerPlayServer.class::cast)
                .map(handler -> handler.playerEntity)
                .map(player -> player.worldObj)
                .filter(IThreadListener.class::isInstance)
                .map(IThreadListener.class::cast)
                .orElse(null);
    }

    @Override
    public void safeProcessReceivedPackets() {
        IThreadListener listener = getThreadListener();
        if (listener != null)
            listener.addScheduledTask(this::processReceivedPackets);
        else
            this.processReceivedPackets();
    }
}

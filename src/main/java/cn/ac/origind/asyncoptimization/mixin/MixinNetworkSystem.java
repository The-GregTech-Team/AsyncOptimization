package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.AsyncOptimization;
import cn.ac.origind.asyncoptimization.concurrent.IExtendedNetworkManager;
import cn.ac.origind.asyncoptimization.concurrent.IThreadListener;
import cn.ac.origind.asyncoptimization.concurrent.cache.ICache;
import cn.ac.origind.asyncoptimization.concurrent.cache.StdCache;
import com.google.common.collect.Lists;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ReportedException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(NetworkSystem.class)
public class MixinNetworkSystem {
    @Shadow
    @Final
    public List<NetworkManager> networkManagers;

    @Overwrite
    public void networkTick() {
        synchronized (networkManagers) {
            Iterator<NetworkManager> iterator = networkManagers.iterator();
            ICache<IThreadListener, List<NetworkManager>> cache = new StdCache<IThreadListener, List<NetworkManager>>()
                    .setOnMissGet(Lists::newLinkedList);
            while (iterator.hasNext()) {
                NetworkManager manager = iterator.next();
                IThreadListener listener = ((IExtendedNetworkManager) manager).getThreadListener();
                if (listener != null)
                    cache.get(listener).add(manager);
                else
                    doNetworkManagerTick(manager);
            }
            cache.getCacheMap().forEach(
                    (listener, list) -> listener.addScheduledTask(() -> list.forEach(this::doNetworkManagerTick)));
            networkManagers.removeIf(manager -> !manager.isChannelOpen());
        }
    }

    @SuppressWarnings("unchecked")
    public void doNetworkManagerTick(NetworkManager manager) {
        if (manager.isChannelOpen()) {
            try {
                manager.processReceivedPackets();
            } catch (Exception exception) {
                if (manager.isLocalChannel()) {
                    CrashReport report = CrashReport.makeCrashReport(exception, "Ticking memory connection");
                    CrashReportCategory category = report.makeCategory("Ticking connection");
                    category.addCrashSectionCallable("Connection", manager::toString);
                    throw new ReportedException(report);
                }
                AsyncOptimization.getLogger().warn("Failed to handle packet for {}",
                        manager.getRemoteAddress(), exception);
                ChatComponentText component = new ChatComponentText("Internal server error");
                manager.scheduleOutboundPacket(new S40PacketDisconnect(component), future -> manager.closeChannel(component));
                manager.disableAutoRead();
            }
        }
        else {
            // handleDisconnection
            if (manager.channel() != null && !manager.channel().isOpen())
            {

                if (manager.getExitMessage() != null)
                {
                    manager.getNetHandler().onDisconnect(manager.getExitMessage());
                }
                else if (manager.getNetHandler() != null)
                {
                    manager.getNetHandler().onDisconnect(new ChatComponentTranslation("multiplayer.disconnect.generic", new Object[0]));
                }
            }
        }
    }
}

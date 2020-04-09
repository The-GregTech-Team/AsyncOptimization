package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.IAsyncThreadListener;
import cn.ac.origind.asyncoptimization.concurrent.IThreadListener;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ICommandSender, Runnable, IPlayerUsage, IThreadListener {
    @Final
    @Shadow
    public Profiler theProfiler;

    @Shadow
    private int tickCounter;

    @Shadow
    public Hashtable<Integer, long[]> worldTickTimes;

    @Shadow
    public abstract boolean getAllowNether();

    @Shadow
    private ServerConfigurationManager serverConfigManager;

    @Shadow
    private final List playersOnline = new ArrayList();

    @Shadow
    public abstract NetworkSystem getNetworkSystem();

    @Overwrite
    public void updateTimeLightAndEntities() {
        theProfiler.startSection("levels");
        net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();
        for (int id : net.minecraftforge.common.DimensionManager.getIDs(tickCounter % 200 == 0)) {
            long nano = System.nanoTime();
            if (id == 0 || getAllowNether()) {
                WorldServer world = net.minecraftforge.common.DimensionManager.getWorld(id);
                ((IAsyncThreadListener) world).addScheduledTask(() -> {
                    if (tickCounter % 20 == 0) {
                        this.serverConfigManager.sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(world.getTotalWorldTime(), world.getWorldTime(), world.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), world.provider.dimensionId);
                    }
                    FMLCommonHandler.instance().onPreWorldTick(world);
                    try {
                        world.tick();
                        world.updateEntities();
                    } catch (Throwable t) {
                        CrashReport report = CrashReport.makeCrashReport(t, "Exception ticking world");
                        world.addWorldInfoToCrashReport(report);
                        throw new ReportedException(report);
                    }
                    FMLCommonHandler.instance().onPostWorldTick(world);
                    world.getEntityTracker().updateTrackedEntities();
                    long[] timings = worldTickTimes.get(id);
                    if (timings != null)
                        timings[tickCounter % 100] = System.nanoTime() - nano;
                });
            }
        }
        theProfiler.endStartSection("dim_unloading");
        net.minecraftforge.common.DimensionManager.unloadWorlds(worldTickTimes);
        theProfiler.endStartSection("connection");
        getNetworkSystem().networkTick();
        theProfiler.endStartSection("players");
        serverConfigManager.onTick();
        theProfiler.endStartSection("tickables");
        for (Object o : this.playersOnline) {
            ((IUpdatePlayerListBox) o).update();
        }
        theProfiler.endSection();
    }
}

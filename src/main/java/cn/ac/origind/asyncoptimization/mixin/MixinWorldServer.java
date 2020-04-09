package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.*;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Mixin({WorldServer.class})
public abstract class MixinWorldServer extends World implements IAsyncThreadListener {
    public MixinWorldServer(ISaveHandler p_i45369_1_, String p_i45369_2_, WorldSettings p_i45369_3_, WorldProvider p_i45369_4_, Profiler p_i45369_5_) {
        super(p_i45369_1_, p_i45369_2_, p_i45369_3_, p_i45369_4_, p_i45369_5_);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        startLoop();
    }

    @Overwrite
    public void saveAllChunks(boolean all, IProgressUpdate progressCallback) throws MinecraftException {
        if (isCallingFromMinecraftThread()) {
            saveAllChunksInternal(all, progressCallback);
            return;
        }
        MinecraftException[] pointer = {null};
        syncCall(() -> {
            try {
                saveAllChunksInternal(all, progressCallback);
            } catch (MinecraftException e) {
                pointer[0] = e;
            }
        });
        if (pointer[0] != null)
            throw pointer[0];
    }

    @Shadow
    protected abstract void saveLevel() throws MinecraftException;

    @Shadow
    public ChunkProviderServer theChunkProviderServer;

    @Shadow
    @Final
    public PlayerManager thePlayerManager;

    public void saveAllChunksInternal(boolean p_73044_1_, IProgressUpdate p_73044_2_) throws MinecraftException {
        if (this.chunkProvider.canSave())
        {
            if (p_73044_2_ != null)
            {
                p_73044_2_.displaySavingString("Saving level");
            }

            this.saveLevel();

            if (p_73044_2_ != null)
            {
                p_73044_2_.displayLoadingString("Saving chunks");
            }

            this.chunkProvider.saveChunks(p_73044_1_, p_73044_2_);
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Save(this));
            ArrayList arraylist = Lists.newArrayList(this.theChunkProviderServer.func_152380_a());
            Iterator iterator = arraylist.iterator();

            while (iterator.hasNext())
            {
                Chunk chunk = (Chunk)iterator.next();

                if (chunk != null && !this.thePlayerManager.func_152621_a(chunk.xPosition, chunk.zPosition))
                {
                    this.theChunkProviderServer.dropChunk(chunk.xPosition, chunk.zPosition);
                }
            }
        }
    }

    protected LinkedBlockingDeque<Runnable> runnables = new LinkedBlockingDeque<>();
    protected Thread asyncThread;
    protected IFieldContainer<Boolean> running = new ObserverWrapperFieldContainer<>(new FieldContainer<>(Boolean.TRUE),
            (old, _new) -> {
                if (asyncThread != Thread.currentThread() && asyncThread != null && asyncThread.isAlive())
                    if (old == Boolean.TRUE && _new == Boolean.FALSE)
                        asyncThread.interrupt();
            });

    @Override
    public void startLoop() {
        running.set(Boolean.TRUE);
        Thread result = asyncThread != null && asyncThread.isAlive()
                ? null
                : AlchemyThreadManager.runOnNewThread(() -> {
            while (running.get() || runnables.size() > 0) {
                try {
                    runnables.take().run();
                } catch (InterruptedException e) {
                    // IGNORED
                }
                WAITING_THREADS.executeAndClearIfPresent(asyncThread, new RunnableRunner());
            }
            asyncThread = null;
        }, "AsyncWorld[" + provider.getDimensionName() + "](" + provider.dimensionId + ")");
        if (result != null)
            asyncThread = result;
    }

    @Override
    public IFieldContainer<Boolean> running() {
        return running;
    }

    @Override
    public BlockingDeque<Runnable> runnables() {
        return runnables;
    }

    @Override
    public Thread asyncThread() {
        return asyncThread;
    }

    @Override
    public boolean isCallingFromMinecraftThread() {
        return asyncThread == null || !asyncThread.isAlive() || !running().get()
                || Thread.currentThread() == asyncThread;
    }

    @Nonnull
    @Override
    public ListenableFuture<Object> addScheduledTask(@Nonnull Runnable runnable) {
        return IAsyncThreadListener.super.addScheduledTask(runnable);
    }
}

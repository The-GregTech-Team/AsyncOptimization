package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.*;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import cpw.mods.fml.relauncher.ReflectionHelper;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Mixin({WorldServer.class})
public abstract class MixinWorldServer extends World implements IAsyncThreadListener {
    public MixinWorldServer(ISaveHandler p_i45369_1_, String p_i45369_2_, WorldSettings p_i45369_3_, WorldProvider p_i45369_4_, Profiler p_i45369_5_) {
        super(p_i45369_1_, p_i45369_2_, p_i45369_3_, p_i45369_4_, p_i45369_5_);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;ILnet/minecraft/world/WorldSettings;Lnet/minecraft/profiler/Profiler;)V", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        onInitInternal();
    }

    public void onInitInternal() {
        if (worldAccesses != null)
            worldAccesses = Collections.synchronizedList(worldAccesses);
        if (loadedEntityList != null)
            loadedEntityList = Collections.synchronizedList(loadedEntityList);
        if (unloadedEntityList != null)
            unloadedEntityList = Collections.synchronizedList(unloadedEntityList);
        if (loadedTileEntityList != null)
            loadedTileEntityList = Collections.synchronizedList(loadedTileEntityList);
        List<?> addedTileEntityList = ReflectionHelper.getPrivateValue(World.class, this, "field_147484_a", "addedTileEntityList");
        if (addedTileEntityList != null)
            ReflectionHelper.setPrivateValue(World.class, this, Collections.synchronizedList(addedTileEntityList), "field_147484_a", "addedTileEntityList");
        List<?> tileEntitiesToBeRemoved = ReflectionHelper.getPrivateValue(World.class, this, "field_147483_b", "tileEntitiesToBeRemoved");
        if (tileEntitiesToBeRemoved != null)
            ReflectionHelper.setPrivateValue(World.class, this, Collections.synchronizedList(tileEntitiesToBeRemoved), "field_147483_b", "tileEntitiesToBeRemoved");
        if (playerEntities != null)
            playerEntities = Collections.synchronizedList(playerEntities);
        if (weatherEffects != null)
            weatherEffects = Collections.synchronizedList(weatherEffects);
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

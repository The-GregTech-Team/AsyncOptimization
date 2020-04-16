package cn.ac.origind.asyncoptimization.mixin;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.NetworkMonitor;
import appeng.me.storage.ItemWatcher;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Mixin(value = NetworkMonitor.class, remap = false)
public abstract class MixinNetworkMonitor<T extends IAEStack<T>> {
    @Nonnull
    @Shadow
    @Final
    @Mutable
    private static Deque<NetworkMonitor<?>> GLOBAL_DEPTH;

    @Nonnull
    @Shadow
    @Final
    private GridStorageCache myGridCache;

    @Nonnull
    @Shadow
    @Final
    private StorageChannel myChannel;

    @Nonnull
    @Shadow
    @Final
    private IItemList<T> cachedList;

    @Nonnull
    @Shadow
    @Final
    private Map<IMEMonitorHandlerReceiver<T>, Object> listeners;

    @Shadow
    private boolean sendEvent;
    @Shadow
    private boolean hasChanged;
    @Nonnegative
    private int localDepthSemaphore = 0;

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        listeners = new ConcurrentHashMap<>();
        GLOBAL_DEPTH = new ConcurrentLinkedDeque<>();
    }

    /**
     * Fuck you IllegalStateException thread checking
     * @author Lasm_Gratel
     */
    @Overwrite
    protected void postChange(boolean add, Iterable<T> changes, BaseActionSource src) {
        if (this.localDepthSemaphore <= 0 && !GLOBAL_DEPTH.contains(NetworkMonitor.class.cast(this))) {
            GLOBAL_DEPTH.push(NetworkMonitor.class.cast(this));
            ++this.localDepthSemaphore;
            this.sendEvent = true;
            this.notifyListenersOfChange(changes, src);
            Iterator<T> var4 = changes.iterator();

            while(true) {
                T changedItem;
                T difference;
                Collection<ItemWatcher> list;
                do {
                    do {
                        if (!var4.hasNext()) {
                            NetworkMonitor<?> last = (NetworkMonitor)GLOBAL_DEPTH.pop();
                            --this.localDepthSemaphore;

                            return;
                        }

                        changedItem = var4.next();
                        difference = changedItem;
                        if (!add && changedItem != null) {
                            difference = changedItem.copy();
                            difference.setStackSize(-changedItem.getStackSize());
                        }
                    } while(!this.myGridCache.getInterestManager().containsKey(changedItem));

                    list = this.myGridCache.getInterestManager().get(changedItem);
                } while(list.isEmpty());

                IAEStack fullStack = this.getStorageList().findPrecise(changedItem);
                if (fullStack == null) {
                    fullStack = changedItem.copy();
                    fullStack.setStackSize(0L);
                }

                this.myGridCache.getInterestManager().enableTransactions();

                for (Object o : list) {
                    ItemWatcher iw = (ItemWatcher) o;
                    iw.getHost().onStackChange(this.getStorageList(), fullStack, difference, src, this.getChannel());
                }

                this.myGridCache.getInterestManager().disableTransactions();
            }
        }
    }

    @Shadow
    protected abstract StorageChannel getChannel();

    @Shadow
    protected abstract IItemList<T> getStorageList();

    @Shadow
    protected abstract void notifyListenersOfChange(Iterable<T> changes, BaseActionSource src);
}

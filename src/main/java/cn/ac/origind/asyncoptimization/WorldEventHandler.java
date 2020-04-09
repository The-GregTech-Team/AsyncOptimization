package cn.ac.origind.asyncoptimization;

import cn.ac.origind.asyncoptimization.concurrent.IAsyncThreadListener;
import cn.ac.origind.asyncoptimization.concurrent.SideHelper;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.event.world.WorldEvent;

public class WorldEventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.SERVER)
    public void onWorldUnload(WorldEvent.Unload event) {
        if (SideHelper.isServer())
            ((IAsyncThreadListener) event.world).running().set(Boolean.FALSE);
    }
}

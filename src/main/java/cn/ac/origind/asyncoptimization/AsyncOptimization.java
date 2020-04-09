package cn.ac.origind.asyncoptimization;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AsyncOptimization.MODID, acceptableRemoteVersions = "*")
public class AsyncOptimization {
    public static final String MODID = "asyncoptimization";
    public static final Logger LOGGER = LogManager.getLogger("AsyncOptimization");

    public static Logger getLogger() {
        return AsyncOptimization.LOGGER;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new WorldEventHandler());
    }
}

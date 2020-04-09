package cn.ac.origind.asyncoptimization.concurrent;

import cpw.mods.fml.relauncher.Side;

public class SideHelper {
    private static boolean isClient;
    static {
        try {
            Class.forName("net.minecraft.client.Minecraft");
            isClient = true;
        } catch (ClassNotFoundException e) {
            isClient = false;
        }
    }

    public static final boolean runOnClient() {
        return isClient;
    }

    public static final Side side() {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        return group instanceof SidedThreadGroup ? ((SidedThreadGroup) group).getSide() : (isClient ? Side.CLIENT : Side.SERVER);
    }

    public static final SidedThreadGroup sideThreadGroup() {
        return mapSideThreadGroup(side());
    }

    public static final SidedThreadGroup mapSideThreadGroup(Side side) {
        return side.isClient() ? SidedThreadGroups.CLIENT : SidedThreadGroups.SERVER;
    }

    public static final boolean isServer() {
        return side().isServer();
    }

    public static final boolean isClient() {
        return side().isClient();
    }
}

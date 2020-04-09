package cn.ac.origind.asyncoptimization.concurrent;

import cpw.mods.fml.relauncher.Side;

public final class SidedThreadGroups {
    public static final SidedThreadGroup CLIENT = new SidedThreadGroup(Side.CLIENT);
    public static final SidedThreadGroup SERVER = new SidedThreadGroup(Side.SERVER);

    private SidedThreadGroups() {
    }
}

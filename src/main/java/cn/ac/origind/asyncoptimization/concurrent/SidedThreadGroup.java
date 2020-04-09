package cn.ac.origind.asyncoptimization.concurrent;

import cpw.mods.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public final class SidedThreadGroup extends ThreadGroup implements ThreadFactory
{
    private final Side side;

    SidedThreadGroup(final Side side)
    {
        super(side.name());
        this.side = side;
    }

    /**
     * Gets the side this sided thread group belongs to.
     *
     * @return the side
     */
    public Side getSide()
    {
        return this.side;
    }

    @Override
    public Thread newThread(@Nonnull final Runnable runnable)
    {
        return new Thread(this, runnable);
    }
}
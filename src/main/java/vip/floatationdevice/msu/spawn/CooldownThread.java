package vip.floatationdevice.msu.spawn;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static vip.floatationdevice.msu.spawn.Spawn.cm;

public class CooldownThread extends Thread
{
    private final UUID u;
    private final ConcurrentHashMap<UUID, CooldownThread> targetMap;
    private long startTime;

    public CooldownThread(UUID u, ConcurrentHashMap<UUID, CooldownThread> targetMap)
    {
        this.u = u;
        this.targetMap = targetMap;
    }

    @Override
    public void run()
    {
        try
        {
            startTime = System.currentTimeMillis();
            Thread.sleep(1000L * cm.get(Integer.class, "cooldown.sec"));
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        targetMap.remove(u);
    }

    public long getCooldownRemaining()
    {
        long past = System.currentTimeMillis() - startTime;
        return 1000L * cm.get(Integer.class, "cooldown.sec") - past;
    }
}
package vip.floatationdevice.msu.spawn;

import java.util.HashMap;
import java.util.UUID;

public class CooldownThread extends Thread
{
    private final UUID u;
    private final HashMap<UUID, CooldownThread> targetMap;
    private long startTime;

    public CooldownThread(UUID u, HashMap<UUID, CooldownThread> targetMap)
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
            Thread.sleep(ConfigManager.getCooldownSec() * 1000L);
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
        return ConfigManager.getCooldownSec() * 1000L - past;
    }
}
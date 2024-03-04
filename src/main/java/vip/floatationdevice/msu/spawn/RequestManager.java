package vip.floatationdevice.msu.spawn;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static vip.floatationdevice.msu.spawn.Spawn.cm;
import static vip.floatationdevice.msu.spawn.Spawn.log;

public class RequestManager
{
    final ConcurrentHashMap<UUID, CooldownThread> cooldownPlayers = new ConcurrentHashMap<>();
    final ConcurrentHashMap<UUID, TeleportThread> warmupPlayers = new ConcurrentHashMap<>();

    void addCooldown(Player p)
    {
        if(cm.get(Integer.class, "cooldown.sec") < 1 || p.hasPermission("spawn.nocooldown"))
            return;

        if(cooldownPlayers.containsKey(p.getUniqueId()))
            log.warning("Trying to add cooldown for " + p.getName() + " but cooldown already exists, probably a bug of plugin");
        CooldownThread t = new CooldownThread(p.getUniqueId(), cooldownPlayers);
        cooldownPlayers.put(p.getUniqueId(), t);
        t.start();
    }

    long getCooldownRemaining(UUID u)
    {
        return cooldownPlayers.get(u).getCooldownRemaining();
    }

    boolean hasCooldown(UUID u)
    {
        return cooldownPlayers.containsKey(u);
    }

    boolean hasWarmup(UUID u)
    {
        return warmupPlayers.containsKey(u);
    }

    void startTeleport(Player p)
    {
        if(warmupPlayers.containsKey(p.getUniqueId()))
            log.warning("Trying to start teleport for " + p.getName() + " but warmup already exists, probably a bug of plugin");
        TeleportThread t = new TeleportThread(p.getUniqueId(), warmupPlayers);
        warmupPlayers.put(p.getUniqueId(), t);
        t.start();
    }
}

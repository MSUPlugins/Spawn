package vip.floatationdevice.msu.spawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;

import static vip.floatationdevice.msu.spawn.I18nUtil.translate;
import static vip.floatationdevice.msu.spawn.RequestManager.interruptors;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.isSpawnPointFileExist;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.readSpawnLocation;

public class WarmupThread extends Thread implements Listener
{
    private final UUID u;
    private final Player p;
    private final HashMap<UUID,WarmupThread> targetMap;

    public WarmupThread(UUID u, HashMap<UUID,WarmupThread> targetMap)
    {
        this.u=u;
        p=Bukkit.getPlayer(u);
        this.targetMap=targetMap;
    }

    @Override
    public void run()
    {
        try
        {
            if(ConfigManager.getWarmupSec()>=1 && !p.hasPermission("spawn.nowarmup"))
            {
                p.sendMessage(I18nUtil.translate("warmup").replace("{0}",String.valueOf(ConfigManager.getWarmupSec())));
                Thread.sleep(ConfigManager.getWarmupSec()*1000L);
            }

            if(!ConfigManager.useMinecraftSpawnPoint() && !isSpawnPointFileExist())
            {// server has no spawn file and not using mc spawn? teleport player to mc spawn anyway
                Spawn.log.warning(translate("warn-spawn-not-set"));
                p.teleport(Spawn.instance.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                RequestManager.addCooldown(u);
                p.sendMessage(translate("spawn-success"));
            }
            else
            {
                try
                {
                    p.teleport(readSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    RequestManager.addCooldown(u);
                    p.sendMessage(translate("spawn-success"));
                }
                catch(Exception e)
                {
                    p.sendMessage(translate("err-spawn-fail"));
                    Spawn.log.severe(translate("err-spawn-fail-console")
                            .replace("{0}",p.getName())
                            .replace("{1}",e.toString()));
                }
            }
        }
        catch (InterruptedException e)
        {
            Spawn.log.info("WarmupThread interrupted: "+e);
        }
        targetMap.remove(u);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        if(interruptors[0] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(I18nUtil.translate("err-warmup-interrupted"));
            targetMap.remove(u);
            interrupt();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        if(targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            targetMap.remove(u);
            interrupt();
        }
    }
}

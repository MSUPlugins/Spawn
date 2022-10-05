package vip.floatationdevice.msu.spawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import vip.floatationdevice.msu.I18nUtil;

import java.util.HashMap;
import java.util.UUID;

import static vip.floatationdevice.msu.I18nUtil.translate;
import static vip.floatationdevice.msu.spawn.RequestManager.interruptors;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.isSpawnPointFileExist;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.readSpawnLocation;

public class TeleportThread extends Thread implements Listener
{
    private final UUID u;
    private final Player p;
    private final HashMap<UUID, TeleportThread> targetMap;

    public TeleportThread(UUID u, HashMap<UUID, TeleportThread> targetMap)
    {
        this.u = u;
        p = Bukkit.getPlayer(u);
        this.targetMap = targetMap;
    }

    @Override
    public void run()
    {
        try
        {
            if(ConfigManager.getWarmupSec() >= 1)
            {
                Spawn.instance.getServer().getPluginManager().registerEvents(this, Spawn.instance);
                // wait if no permission
                if(!p.hasPermission("spawn.nowarmup"))
                {
                    p.sendMessage(I18nUtil.translate("warmup").replace("{0}", String.valueOf(ConfigManager.getWarmupSec())));
                    Thread.sleep(ConfigManager.getWarmupSec() * 1000L);
                }
                // ensure that player is in a normal state and then teleport
                if(p.isOnline() && !p.isDead())
                {
                    if(!ConfigManager.useMinecraftSpawnPoint() && !isSpawnPointFileExist())
                    {
                        // server has no spawn file and not using mc spawn? teleport player to mc spawn anyway
                        Bukkit.getScheduler().runTask(Spawn.instance, new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Spawn.log.warning(translate("warn-spawn-not-set"));
                                p.teleport(Spawn.instance.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                                RequestManager.addCooldown(u);
                                p.sendMessage(translate("spawn-success"));
                            }
                        });
                    }
                    else
                    {
                        Bukkit.getScheduler().runTask(Spawn.instance, new Runnable()
                        {
                            @Override
                            public void run()
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
                                            .replace("{0}", p.getName())
                                            .replace("{1}", e.toString()));
                                }
                            }
                        });
                    }
                }
            }
        }
        catch(InterruptedException e)
        {
            // do nothing
        }
        targetMap.remove(u);
        PlayerMoveEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        if(interruptors[0] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(I18nUtil.translate("err-warmup-interrupted"));
            targetMap.remove(u);
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e)
    {
        if(interruptors[1] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(I18nUtil.translate("err-warmup-interrupted"));
            targetMap.remove(u);
            interrupt();
        }
    }

    @EventHandler
    public void onPLayerInteract(PlayerInteractEvent e)
    {
        if(interruptors[2] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(I18nUtil.translate("err-warmup-interrupted"));
            targetMap.remove(u);
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if(interruptors[3] && e.getEntity() instanceof Player && targetMap.containsKey(e.getEntity().getUniqueId()))
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

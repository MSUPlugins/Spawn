package vip.floatationdevice.msu.spawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static vip.floatationdevice.msu.spawn.RequestManager.interruptors;
import static vip.floatationdevice.msu.spawn.Spawn.i18n;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.isSpawnPointFileExist;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.readSpawnLocation;

public class TeleportThread extends Thread implements Listener
{
    private final UUID u;
    private final Player p;
    private final ConcurrentHashMap<UUID, TeleportThread> targetMap;

    public TeleportThread(UUID u, ConcurrentHashMap<UUID, TeleportThread> targetMap)
    {
        setName("TeleportThread for " + Bukkit.getPlayer(u).getName());
        this.u = u;
        p = Bukkit.getPlayer(u);
        this.targetMap = targetMap;
    }

    @Override
    public void run()
    {
        try
        {
            if(ConfigManager.getWarmupSec() >= 1) // if warmup is enabled
            {
                Spawn.instance.getServer().getPluginManager().registerEvents(this, Spawn.instance);
                // wait if no permission
                if(!p.hasPermission("spawn.nowarmup"))
                {
                    p.sendMessage(i18n.translate("warmup").replace("{0}", String.valueOf(ConfigManager.getWarmupSec())));
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
                                Spawn.log.warning(i18n.translate("warn-spawn-not-set"));
                                p.teleport(Spawn.instance.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                                RequestManager.addCooldown(u);
                                p.sendMessage(i18n.translate("spawn-success"));
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
                                    p.sendMessage(i18n.translate("spawn-success"));
                                }
                                catch(Exception e)
                                {
                                    p.sendMessage(i18n.translate("err-spawn-fail"));
                                    Spawn.log.severe(i18n.translate("err-spawn-fail-console")
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
            Spawn.log.info(getName() + " gets interrupted");
        }
        targetMap.remove(u);
        PlayerMoveEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        if(interruptors[0] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e)
    {
        if(interruptors[1] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e)
    {
        if(interruptors[1] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPLayerInteract(PlayerInteractEvent e)
    {
        if(interruptors[2] && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if(interruptors[3] && e.getEntity() instanceof Player && targetMap.containsKey(e.getEntity().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        if(targetMap.containsKey(e.getPlayer().getUniqueId()))
            interrupt();
    }
}

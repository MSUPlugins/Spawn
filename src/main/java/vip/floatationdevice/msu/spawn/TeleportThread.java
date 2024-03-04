package vip.floatationdevice.msu.spawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static vip.floatationdevice.msu.spawn.Spawn.*;

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
            int warmupSec;
            // skip waiting if player has permission or warmup is disabled
            if(!p.hasPermission("spawn.nowarmup") && (warmupSec = cm.get(Integer.class, "warmup.sec")) >= 1)
            {
                instance.getServer().getPluginManager().registerEvents(this, instance);
                p.sendMessage(i18n.translate("warmup").replace("{0}", String.valueOf(warmupSec)));
                sleep(1000L * warmupSec);
            }

            // wait completed or skipped

            // don't teleport if player is dead or offline
            if(!p.isOnline() || p.isDead())
                return;

            // teleport
            Bukkit.getScheduler().runTask(instance, new Runnable()
            {
                @Override
                public void run()
                {
                    if(cm.get(Boolean.class, "useMinecraftSpawnPoint") || spm.isSpawnPointFileExist())
                    {
                        // normal teleport logic
                        try
                        {
                            p.teleport(spm.readSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                            rm.addCooldown(p);
                            p.sendMessage(i18n.translate("spawn-success"));
                        }
                        catch(Exception e)
                        {
                            p.sendMessage(i18n.translate("err-spawn-fail"));
                            log.severe(i18n.translate("err-spawn-fail-console")
                                    .replace("{0}", p.getName())
                                    .replace("{1}", e.toString()));
                        }
                    }
                    else
                    {
                        // server has no spawn file and not using mc spawn? teleport to mc spawn anyway
                        log.warning(i18n.translate("warn-spawn-not-set"));
                        p.teleport(instance.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        rm.addCooldown(p);
                        p.sendMessage(i18n.translate("spawn-success"));
                    }
                }
            });
        }
        catch(InterruptedException e)
        {
            log.info("Cancelled teleport for " + p.getName());
        }

        // cleanup
        targetMap.remove(u);
        PlayerMoveEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent e)
    {
        if(e.isCancelled() || !cm.get(Boolean.class, "warmup.interruptBy.move"))
            return;

        if(e.getPlayer() == p && rm.hasWarmup(u))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent e)
    {
        if(e.isCancelled() && !cm.get(Boolean.class, "warmup.interruptBy.message"))
            return;

        if(e.getPlayer() == p && rm.hasWarmup(u))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e)
    {
        if(e.isCancelled() && !cm.get(Boolean.class, "warmup.interruptBy.message"))
            return;

        if(e.getPlayer() == p && rm.hasWarmup(u))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPLayerInteract(PlayerInteractEvent e)
    {
        if(e.isCancelled() && !cm.get(Boolean.class, "warmup.interruptBy.interact"))
            return;

        if(e.getPlayer() == p && rm.hasWarmup(u))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPLayerInteractEntity(PlayerInteractEntityEvent e)
    {
        if(e.isCancelled() && !cm.get(Boolean.class, "warmup.interruptBy.interact"))
            return;

        if(e.getPlayer() == p && rm.hasWarmup(u))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent e)
    {
        if(e.isCancelled() || !cm.get(Boolean.class, "warmup.interruptBy.damage"))
            return;

        if((e.getEntity() == p || e.getDamager() == p) && rm.hasWarmup(u))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent e)
    {
        if(e.getPlayer() == p && rm.hasWarmup(u))
            interrupt();
    }
}

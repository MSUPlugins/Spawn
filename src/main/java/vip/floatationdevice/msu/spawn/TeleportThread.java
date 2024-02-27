package vip.floatationdevice.msu.spawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static vip.floatationdevice.msu.spawn.Spawn.*;
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
            if(cm.get(Integer.class, "warmup.sec") >= 1) // if warmup is enabled
            {
                instance.getServer().getPluginManager().registerEvents(this, instance);
                // wait if no permission
                if(!p.hasPermission("spawn.nowarmup"))
                {
                    p.sendMessage(i18n.translate("warmup").replace("{0}", String.valueOf(cm.get(Integer.class, "warmup.sec"))));
                    Thread.sleep(1000L * cm.get(Integer.class, "warmup.sec"));
                }
                // don't teleport if player is dead or offline
                if(!p.isOnline() || p.isDead())
                    return;

                if(!cm.get(Boolean.class, "useMinecraftSpawnPoint") && !isSpawnPointFileExist())
                {
                    // server has no spawn file and not using mc spawn? teleport player to mc spawn anyway
                    Bukkit.getScheduler().runTask(instance, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            log.warning(i18n.translate("warn-spawn-not-set"));
                            p.teleport(instance.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                            RequestManager.addCooldown(u);
                            p.sendMessage(i18n.translate("spawn-success"));
                        }
                    });
                }
                else
                {
                    Bukkit.getScheduler().runTask(instance, new Runnable()
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
                                log.severe(i18n.translate("err-spawn-fail-console")
                                        .replace("{0}", p.getName())
                                        .replace("{1}", e.toString()));
                            }
                        }
                    });
                }
            }
        }
        catch(InterruptedException e)
        {
            log.info(getName() + " gets interrupted");
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
        if(cm.get(Boolean.class, "warmup.interruptBy.move") && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e)
    {
        if(cm.get(Boolean.class, "warmup.interruptBy.message") && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e)
    {
        if(cm.get(Boolean.class, "warmup.interruptBy.message") && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPLayerInteract(PlayerInteractEvent e)
    {
        if(cm.get(Boolean.class, "warmup.interruptBy.interact") && targetMap.containsKey(e.getPlayer().getUniqueId()))
        {
            p.sendMessage(i18n.translate("err-warmup-interrupted"));
            interrupt();
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if(cm.get(Boolean.class, "warmup.interruptBy.damage") && e.getEntity() instanceof Player && targetMap.containsKey(e.getEntity().getUniqueId()))
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

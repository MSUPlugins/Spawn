package vip.floatationdevice.msu.spawn;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import vip.floatationdevice.msu.ConfigManager;
import vip.floatationdevice.msu.I18nManager;

import java.util.logging.Logger;

public final class Spawn extends JavaPlugin implements Listener
{
    static Spawn instance;
    static Logger log;
    static ConfigManager cm;
    static I18nManager i18n;
    static SpawnPointManager spm;
    static RequestManager rm;

    @Override
    public void onEnable()
    {
        instance = this;
        log = getLogger();
        cm = new ConfigManager(this, 1).initialize();
        i18n = new I18nManager(this).setLanguage(cm.get(String.class, "language"));
        spm = new SpawnPointManager();
        rm = new RequestManager();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("spawn").setExecutor(new SpawnCommandExecutor());
        getCommand("setspawn").setExecutor(new SetspawnCommandExecutor());

        log.info("Spawn has been loaded");
    }

    @Override
    public void onDisable()
    {
        log.info("Spawn is being unloaded");

        for(TeleportThread t : rm.warmupPlayers.values())
            t.interrupt();
        for(CooldownThread t : rm.cooldownPlayers.values())
            t.interrupt();
    }
}

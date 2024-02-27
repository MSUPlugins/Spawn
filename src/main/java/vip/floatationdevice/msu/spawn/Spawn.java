package vip.floatationdevice.msu.spawn;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import vip.floatationdevice.msu.I18nManager;

import java.util.logging.Logger;

public final class Spawn extends JavaPlugin implements Listener
{
    static Spawn instance;
    static Logger log;
    static I18nManager i18n;

    @Override
    public void onEnable()
    {
        instance = this;
        log = getLogger();
        getServer().getPluginManager().registerEvents(this, this);
        try
        {
            ConfigManager.initialize();
            i18n = new I18nManager(this).setLanguage(ConfigManager.getLanguage());
            getCommand("spawn").setExecutor(new SpawnCommandExecutor());
            getCommand("setspawn").setExecutor(new SetspawnCommandExecutor());
            log.info("Initialization complete");
        }
        catch(Exception e)
        {
            log.severe("Initialization failed");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        for(TeleportThread t : RequestManager.warmupPlayers.values())
            t.interrupt();
        RequestManager.cooldownPlayers.clear();
    }
}

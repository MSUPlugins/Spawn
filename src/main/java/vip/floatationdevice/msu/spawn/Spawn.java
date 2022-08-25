package vip.floatationdevice.msu.spawn;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static vip.floatationdevice.msu.spawn.I18nUtil.*;

public final class Spawn extends JavaPlugin implements Listener
{
    public static Spawn instance;
    public static Logger log;

    @Override
    public void onEnable()
    {
        instance=this;
        log=getLogger();
        getServer().getPluginManager().registerEvents(this,this);
        try
        {
            ConfigManager.initialize();
            setLanguage(ConfigManager.getLanguage());
            this.setEnabled(true);
            getCommand("spawn").setExecutor(new SpawnCommandExecutor());
            getCommand("setspawn").setExecutor(new SetspawnCommandExecutor());
            log.info("Initialization complete");
        }
        catch (Exception e)
        {
            log.severe("Initialization failed");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        //TODO
    }
}

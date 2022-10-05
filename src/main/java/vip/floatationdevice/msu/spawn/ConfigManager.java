package vip.floatationdevice.msu.spawn;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager
{
    public static final int CONFIG_VERSION = 1;
    static YamlConfiguration cfg;

    static void initialize() throws Exception
    {
        Spawn.log.info("Loading configurations");
        File cfgFile = new File(Spawn.instance.getDataFolder(), "config.yml");
        if(!cfgFile.exists()) Spawn.instance.saveResource("config.yml", false);
        cfg = YamlConfiguration.loadConfiguration(cfgFile);
        if(getConfigVersion() != CONFIG_VERSION)
            throw new InvalidConfigurationException("Incorrect configuration version");
        Spawn.log.info("Configurations loaded");
    }

    static int getConfigVersion(){return cfg.getInt("version");}

    static String getLanguage(){return cfg.getString("language", "en_US");}

    static boolean useMinecraftSpawnPoint(){return cfg.getBoolean("useMinecraftSpawnPoint");}

    static int getCooldownSec(){return cfg.getInt("cooldown.sec", 60);}

    static int getWarmupSec(){return cfg.getInt("warmup.sec", 5);}

    static boolean[] getInterruptors()
    {
        return new boolean[]{
                cfg.getBoolean("warmup.interruptBy.move", true),
                cfg.getBoolean("warmup.interruptBy.message", true),
                cfg.getBoolean("warmup.interruptBy.interact", true),
                cfg.getBoolean("warmup.interruptBy.damage", true),
        };
    }
}

package vip.floatationdevice.msu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Manages the config file for a plugin.
 * The config file is stored as "config.yml" in the plugin's data folder.
 * @author MCUmbrella
 */
public class ConfigManager
{
    private final int VERSION; // the value of key "version" with an integer value in "config.yml"
    private final JavaPlugin plugin; // which plugin is using ConfigManager?
    private final File f; // config.yml in the plugin's data folder
    private YamlConfiguration cfg; // YamlConfiguration representation of config.yml
    private boolean initialized = false; // has initialize() been called?

    /**
     * Create a config manager for a plugin.
     * @param plugin The plugin's instance.
     * @param version The config's version. Defined as key "version" with an integer value in "config.yml".
     */
    public ConfigManager(JavaPlugin plugin, int version)
    {
        this.VERSION = version;
        this.plugin = plugin;
        this.f = new File(plugin.getDataFolder(), "config.yml");
    }

    private void requireInitialized()
    {
        if(!initialized)
            throw new IllegalStateException("ConfigManager not initialized for plugin " + plugin.getName());
    }

    /**
     * Initialize the config manager and load "config.yml".
     * Call this function first before calling others.
     */
    public ConfigManager initialize()
    {
        // check if the config file exists. if not, create one
        if(!f.exists() || !f.isFile() || !f.canRead())
            saveDefaultConfig();
        load();
        initialized = true;
        return this;
    }

    /**
     * Write the default config file to the disk (will overwrite the existing file).
     */
    public void saveDefaultConfig()
    {
        plugin.getLogger().info("Saving default config file");
        if(f.exists())
            f.delete();
        plugin.saveResource("config.yml", true);
    }

    /**
     * Load the config file from disk.
     */
    public void load()
    {
        if(!f.exists() || !f.isFile() || !f.canRead())
            throw new RuntimeException("Failed to load config.yml");
        cfg = YamlConfiguration.loadConfiguration(f);
        Integer v = cfg.isInt("version") ? cfg.getInt("version") : null;
        if(v == null || v != VERSION)
            plugin.getLogger().warning("Config version mismatch: expected " + VERSION + " but got " + v);
        plugin.getLogger().info("Config loaded");
    }

    /**
     * Get the YamlConfiguration object of the loaded config file.
     */
    public YamlConfiguration getConfiguration()
    {
        requireInitialized();
        return cfg;
    }

    /**
     * Check if there is a field with the specified path.
     * This function doesn't check the type or content of the field.
     * @param path The path to check.
     * @return true if so, false otherwise.
     */
    public boolean has(String path)
    {
        requireInitialized();
        return cfg.contains(path);
    }

    /**
     * Check if a field at the specified path has the given type.
     * @param type The Class object of the expected type.
     * @param path The path to check.
     * @return true if so, false otherwise.
     * @throws IllegalArgumentException if the type of the field is not recognized by this function.
     */
    public boolean is(Class<?> type, String path) //TODO: optimize
    {
        requireInitialized();
        if(!cfg.contains(path))
            return false;
        if(type == Long.class)
            return cfg.isLong(path);
        else if(type == Integer.class || type == Short.class || type == Byte.class)
            return cfg.isInt(path);
        else if(type == Double.class || type == Float.class)
            return cfg.isDouble(path);
        else if(type == Boolean.class)
            return cfg.isBoolean(path);
        else if(type == List.class)
            return cfg.isList(path);
        else if(type == Set.class)
            return cfg.isSet(path);
        else if(type == ConfigurationSection.class)
            return cfg.isConfigurationSection(path);
        else if(type == String.class)
            return cfg.isString(path);
        else
            throw new IllegalArgumentException("Cannot detect if \"" + path + "\" is type \"" + type.getSimpleName() + "\" as the mapping is not implemented");
    }

    /**
     * Get the specified field's value as the given type.
     * @param type The Class object of the expected type.
     * @param path The path of the field to get value from.
     * @param <T> The class of the expected type.
     * @return The value of the field with the specified type.
     */
    public <T> T get(Class<T> type, String path)
    {
        requireInitialized();
        if(!cfg.contains(path))
            return null;

        Object o = cfg.get(path);

        if(type.isInstance(o))
            return type.cast(o);
        else
            throw new IllegalArgumentException("Value at \"" + path + "\" is not \"" + type.getSimpleName() + "\"");
    }

    /**
     * Same as get(Class, String), but a default value is used if there's error parsing the value.
     * @param type The Class object of the expected type.
     * @param path The path of the field to get value from.
     * @param <T> The class of the expected type.
     * @return The value of the field with the specified type, or `def` on error.
     */
    public <T> T get(Class<T> type, String path, T def)
    {
        requireInitialized();
        if(!cfg.contains(path))
            return def;

        Object o = cfg.get(path);

        if(type.isInstance(o))
            return type.cast(o);
        else
            return def;
    }
}

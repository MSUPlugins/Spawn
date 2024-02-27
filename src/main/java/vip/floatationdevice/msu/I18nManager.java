package vip.floatationdevice.msu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Manages the I18n (internationalization) messages.
 * The translated messages are stored in the 'lang_xx_XX.yml' files.
 * @author MCUmbrella
 */
public class I18nManager
{
    private final JavaPlugin plugin;
    private YamlConfiguration langCfg;
    private String code = null;

    /**
     * Create an I18nManager instance for a plugin.
     * @param plugin The plugin's instance.
     */
    public I18nManager(JavaPlugin plugin)
    {
        if(plugin == null)
            throw new IllegalArgumentException("Plugin instance cannot be null");
        this.plugin = plugin;
    }

    /**
     * Set the language of the plugin.
     * @param locale Locale code of the language to use (for example, "en_US" or "zh_CN").
     * This parameter is used to locate the language file. If localeCode is "xx_XX", the
     * plugin will try to find "lang_xx_XX.yml" under the plugin config folder. If not found,
     * the plugin will try to find it in the jar file and copy it to the config folder. If
     * the plugin still can't find it in the jar file, an exception will be thrown.
     * @return The locale code.
     * @throws IllegalArgumentException if locale is null or empty.
     * @throws RuntimeException if the 'language-file-version' key is illegal or not found.
     */
    public I18nManager setLanguage(String locale)
    {
        if(locale == null || locale.isEmpty())
            throw new IllegalArgumentException("Locale cannot be null or empty");

        code = locale;
        File langFile = new File(plugin.getDataFolder(), "lang_" + code + ".yml");
        if(!langFile.exists())
            plugin.saveResource("lang_" + code + ".yml", false);
        langCfg = YamlConfiguration.loadConfiguration(langFile);

        if(!langCfg.isInt("language-file-version"))
            throw new RuntimeException("Language file version is illegal or not found");

        return this;
    }

    /**
     * Translate a string.
     * @param key The key of the string.
     * @return The translated string. If the key does not exist, return "[NO TRANSLATION: key]"
     * @throws IllegalStateException If the language is not set.
     */
    public String translate(String key)
    {
        if(langCfg == null)
            throw new IllegalStateException("Translation engine not initialized");

        return langCfg.getString(key) == null ? "[NO TRANSLATION: " + key + "]" : langCfg.getString(key);
    }

    /**
     * Get the locale code used by I18nManager.
     * @return The locale code. If the language is not set, return null.
     */
    public String getLocaleCode()
    {
        return code;
    }

    /**
     * Get the language of the language file.
     * @return The value of the "language" key. If the key is not present, return "(unknown)".
     */
    public String getLanguage()
    {
        String s = langCfg.getString("language", "");
        return s.isEmpty() ? "(unknown)" : s;
    }

    /**
     * Get the version of the language file.
     * @return The value of the "language-file-version" key.
     */
    public int getLanguageFileVersion()
    {
        return langCfg.getInt("language-file-version");
    }

    /**
     * Get the contributor(s) of the language file.
     * @return The value of the "language-file-contributor" key. If the key is not present, return "(unknown)".
     */
    public String getLanguageFileContributor()
    {
        String s = langCfg.getString("language-file-contributor", "");
        return s.isEmpty() ? "(unknown)" : s;
    }
}
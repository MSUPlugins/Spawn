package vip.floatationdevice.msu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Manages the I18n (internationalization) messages.
 * The translated messages are stored in the 'lang_xx_XX.yml' files.
 * @author MCUmbrella
 */
public class I18nUtil
{
    private static FileConfiguration l;
    private static String code = null;

    /**
     * Set the language of the plugin.
     * @param plugin The class of the plugin.
     * @param locale Locale code of the language to use (for example, "en_US" or "zh_CN").
     * This parameter is used to locate the language file. If localeCode is "xx_XX", the
     * plugin will try to find "lang_xx_XX.yml" under the plugin config folder. If not found,
     * the plugin will try to find it in the jar file and copy it to the config folder. If
     * the plugin still can't find it in the jar file, an exception will be thrown.
     * @return The locale code.
     * @throws IllegalArgumentException if plugin or locale is null.
     * @throws RuntimeException if the 'language-file-version' key is illegal or not found.
     */
    public static String setLanguage(Class<? extends JavaPlugin> plugin, String locale)
    {
        if(plugin == null) throw new IllegalArgumentException("Plugin instance cannot be null");
        JavaPlugin p = JavaPlugin.getProvidingPlugin(plugin);
        if(locale == null || locale.isEmpty())
            throw new IllegalArgumentException("Locale cannot be null or empty");
        code = locale;
        File langFile = new File(p.getDataFolder(), "lang_" + code + ".yml");
        if(!langFile.exists()) p.saveResource("lang_" + code + ".yml", false);
        l = YamlConfiguration.loadConfiguration(langFile);
        if(!l.isInt("language-file-version"))
            throw new RuntimeException("Language file version is illegal or not found");
        return code;
    }

    /**
     * Translate a string.
     * @param key The key of the string.
     * @return The translated string. If the key does not exist, return "[NO TRANSLATION: key]"
     * @throws IllegalStateException If the language is not set.
     */
    public static String translate(String key)
    {
        if(l == null) throw new IllegalStateException("Translation engine not initialized");
        return l.getString(key) == null ? "[NO TRANSLATION: " + key + "]" : l.getString(key);
    }

    /**
     * Get the locale code used by I18nUtil.
     * @return The locale code. If the language is not set, return null.
     */
    public static String getLocaleCode()
    {
        return code;
    }

    /**
     * Get the language of the language file.
     * @return The value of the "language" key. If the key is not present, return "(unknown)".
     */
    public static String getLanguage()
    {
        String s = l.getString("language", "");
        return s.isEmpty() ? "(unknown)" : s;
    }

    /**
     * Get the version of the language file.
     * @return The value of the "language-file-version" key.
     */
    public static int getLanguageFileVersion()
    {
        return l.getInt("language-file-version");
    }

    /**
     * Get the contributor(s) of the language file.
     * @return The value of the "language-file-contributor" key. If the key is not present, return "(unknown)".
     */
    public static String getLanguageFileContributor()
    {
        String s = l.getString("language-file-contributor", "");
        return s.isEmpty() ? "(unknown)" : s;
    }
}
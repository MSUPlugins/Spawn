package vip.floatationdevice.msu.spawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static vip.floatationdevice.msu.spawn.Spawn.i18n;
import static vip.floatationdevice.msu.spawn.SpawnPointManager.writeSpawnLocation;

public class SetspawnCommandExecutor implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(i18n.translate("err-player-only"));
            return false;
        }
        else
        {
            if(sender.hasPermission("spawn.setspawn"))
            {
                Player p = (Player) sender;
                try
                {
                    writeSpawnLocation(p.getLocation());
                    p.sendMessage(i18n.translate("setspawn-success"));
                    return true;
                }
                catch(Exception e)
                {
                    p.sendMessage(i18n.translate("err-setspawn-fail"));
                    e.printStackTrace();
                    return false;
                }
            }
            else
            {
                sender.sendMessage(i18n.translate("err-permission-denied"));
                return false;
            }
        }
    }

}

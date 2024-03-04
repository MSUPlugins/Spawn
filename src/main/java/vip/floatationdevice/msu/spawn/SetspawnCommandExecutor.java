package vip.floatationdevice.msu.spawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static vip.floatationdevice.msu.spawn.Spawn.i18n;
import static vip.floatationdevice.msu.spawn.Spawn.spm;

public class SetspawnCommandExecutor implements CommandExecutor
{
    private static final String SETSPAWN_PERM = "spawn.setspawn";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(i18n.translate("err-player-only"));
            return false;
        }

        Player p = (Player) sender;

        if(!p.hasPermission(SETSPAWN_PERM))
        {
            p.sendMessage(i18n.translate("err-permission-denied"));
        }
        else
        {
            try
            {
                spm.writeSpawnLocation(p.getLocation());
                p.sendMessage(i18n.translate("setspawn-success"));
            }
            catch(Exception e)
            {
                p.sendMessage(i18n.translate("err-setspawn-fail"));
                e.printStackTrace();
            }
        }

        return true;
    }
}

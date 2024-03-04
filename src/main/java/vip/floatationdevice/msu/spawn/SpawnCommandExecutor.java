package vip.floatationdevice.msu.spawn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static vip.floatationdevice.msu.spawn.Spawn.i18n;
import static vip.floatationdevice.msu.spawn.Spawn.rm;

public class SpawnCommandExecutor implements CommandExecutor
{
    private static final String SPAWN_PERM = "spawn.spawn";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(i18n.translate("err-player-only"));
            return false;
        }

        Player p = (Player) sender;

        if(args.length != 0)
        {
            p.sendMessage(i18n.translate("usage"));
            return false;
        }

        if(!p.hasPermission(SPAWN_PERM))
            p.sendMessage(i18n.translate("err-permission-denied"));
        else
        {
            if(rm.hasCooldown(p.getUniqueId()))
                p.sendMessage(i18n.translate("err-cooldown").replace("{0}", String.valueOf(rm.getCooldownRemaining(p.getUniqueId()) / 1000L)));
            else if(rm.hasWarmup(p.getUniqueId()))
                p.sendMessage(i18n.translate("err-warmup"));
            else
                rm.startTeleport(p);
        }

        return true;
    }
}

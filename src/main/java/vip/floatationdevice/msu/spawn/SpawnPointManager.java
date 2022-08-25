package vip.floatationdevice.msu.spawn;

import org.bukkit.Location;

import java.io.*;

public class SpawnPointManager
{
    final static File SPAWN_POINT_FILE = new File(Spawn.instance.getDataFolder(), "spawn.txt");

    static boolean isSpawnPointFileExist()
    {
        return SPAWN_POINT_FILE.exists();
    }

    static Location readSpawnLocation() throws Exception
    {
        if(ConfigManager.useMinecraftSpawnPoint())
        {
            return Spawn.instance.getServer().getWorlds().get(0).getSpawnLocation();
        }
        else
        {
            BufferedReader br = new BufferedReader(new FileReader(SPAWN_POINT_FILE));
            String line = br.readLine();
            br.close();
            String[] data = line.split(" ");
            return new Location(
                    Spawn.instance.getServer().getWorld(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Double.parseDouble(data[3]),
                    Float.parseFloat(data[4]),
                    Float.parseFloat(data[5]));
        }
    }

    static void writeLocation(Location l) throws Exception
    {
        SPAWN_POINT_FILE.mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(SPAWN_POINT_FILE));
        bw.write(
                l.getWorld().getName() + " "
                        + l.getX() + " "
                        + l.getY() + " "
                        + l.getZ() + " "
                        + l.getYaw() + " "
                        + l.getPitch() + "\n"
        );
        bw.flush();
        bw.close();
    }
}

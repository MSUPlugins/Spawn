package vip.floatationdevice.msu.spawn;

import org.bukkit.Location;

import java.io.*;

import static vip.floatationdevice.msu.spawn.Spawn.cm;
import static vip.floatationdevice.msu.spawn.Spawn.instance;

public class SpawnPointManager
{
    private final File spawnFile = new File(instance.getDataFolder(), "spawn.txt");
    private Location cachedSpawnLocation;

    boolean isSpawnPointFileExist()
    {
        return spawnFile.exists() && spawnFile.canRead() && spawnFile.isFile();
    }

    Location readSpawnLocation() throws Exception
    {
        if(cachedSpawnLocation != null)
            return cachedSpawnLocation;

        if(cm.get(Boolean.class, "useMinecraftSpawnPoint"))
            return cachedSpawnLocation = instance.getServer().getWorlds().get(0).getSpawnLocation();
        else
        {
            BufferedReader br = new BufferedReader(new FileReader(spawnFile));
            String line = br.readLine();
            br.close();
            String[] data = line.split(" ");
            return cachedSpawnLocation = new Location(
                    instance.getServer().getWorld(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Double.parseDouble(data[3]),
                    Float.parseFloat(data[4]),
                    Float.parseFloat(data[5]));
        }
    }

    void writeSpawnLocation(Location l) throws Exception
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(spawnFile, false));
        bw.write(l.getWorld().getName());
        bw.write(' ');
        bw.write(String.valueOf(l.getX()));
        bw.write(' ');
        bw.write(String.valueOf(l.getY()));
        bw.write(' ');
        bw.write(String.valueOf(l.getZ()));
        bw.write(' ');
        bw.write(String.valueOf(l.getYaw()));
        bw.write(' ');
        bw.write(String.valueOf(l.getPitch()));
        bw.flush();
        bw.close();
    }
}

package dev.masa.masuitehomes.bukkit;

import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitehomes.core.models.Home;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Masa
 */
public class HomeMessageListener implements PluginMessageListener {

    private final MaSuiteHomes plugin;

    public HomeMessageListener(MaSuiteHomes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        String subchannel;
        try {
            subchannel = in.readUTF();
            if ("HomePlayer".equals(subchannel)) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));

                if (p == null) {
                    return;
                }

                Location loc = new Location().deserialize(in.readUTF());

                org.bukkit.Location bukkitLocation = BukkitAdapter.adapt(loc);
                if (bukkitLocation.getWorld() == null) {
                    System.out.println("[MaSuite] [Homes] [World=" + loc.getWorld() + "] World could not be found!");
                    return;
                }

                p.teleport(bukkitLocation);
            }
            if ("AddHome".equals(subchannel)) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
                if (p != null) {
                    if (!plugin.homes.containsKey(p.getUniqueId())) {
                        plugin.homes.put(p.getUniqueId(), new ArrayList<>());
                    }
                    Home home = new Home().deserialize(in.readUTF());
                    plugin.homes.get(p.getUniqueId()).add(home);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

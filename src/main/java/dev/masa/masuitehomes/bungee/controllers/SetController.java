package dev.masa.masuitehomes.bungee.controllers;

import dev.masa.masuitecore.bungee.chat.Formator;
import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.models.MaSuitePlayer;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitehomes.bungee.MaSuiteHomes;
import dev.masa.masuitehomes.core.models.Home;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.UUID;

/**
 * @author Masa
 */
public class SetController {

    private final MaSuiteHomes plugin;
    private final Formator formator = new Formator();
    private final BungeeConfiguration config = new BungeeConfiguration();
    public SetController(MaSuiteHomes plugin) {
        this.plugin = plugin;
    }

    public void set(ProxiedPlayer player, String home, Location loc, int maxGlobalHomes, int maxServerHomes) {
        setHome(player, home, loc, player.getUniqueId(), maxGlobalHomes, maxServerHomes);
    }

    public void set(ProxiedPlayer player, String name, String home, Location loc, int maxGlobalHomes, int maxServerHomes) {
        MaSuitePlayer msp = plugin.getApi().getPlayerService().getPlayer(name);
        if (msp == null) {
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("player-not-found"));
            return;
        }
        setHome(player, home, loc, msp.getUniqueId(), maxGlobalHomes, maxServerHomes);
    }

    private void setHome(ProxiedPlayer player, String homeName, Location loc, UUID uniqueId, int maxGlobalHomes, int maxServerHomes) {
        Home home = plugin.getHomeService().getHomeExact(uniqueId, homeName);
        List<Home> homes = plugin.getHomeService().getHomes(uniqueId);
        loc.setServer(player.getServer().getInfo().getName());
        long serverHomeCount = homes.stream().filter(filteredHome -> filteredHome.getLocation().getServer().equalsIgnoreCase(player.getServer().getInfo().getName())).count();
        if (home != null && (homes.size() <= maxGlobalHomes || maxGlobalHomes == -1) && (serverHomeCount <= maxServerHomes || maxServerHomes == -1)) {
            home.setLocation(loc);
            plugin.getHomeService().updateHome(home);
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("home.updated").replace("%home%", home.getName()));
            plugin.listHomes(player);
            return;
        } else if ((homes.size() < maxGlobalHomes || maxGlobalHomes == -1) && (serverHomeCount < maxServerHomes || maxServerHomes == -1)) {
            home = plugin.getHomeService().createHome(new Home(homeName, uniqueId, loc));
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("home.set").replace("%home%", home.getName()));
        } else {
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("home-limit-reached"));
        }
        plugin.listHomes(player);
    }
}

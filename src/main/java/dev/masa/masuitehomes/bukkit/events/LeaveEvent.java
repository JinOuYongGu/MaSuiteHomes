package dev.masa.masuitehomes.bukkit.events;

import dev.masa.masuitehomes.bukkit.MaSuiteHomes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Masa
 */
public class LeaveEvent implements Listener {

    private final MaSuiteHomes plugin;

    public LeaveEvent(MaSuiteHomes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerJoinEvent e) {
        plugin.homes.remove(e.getPlayer().getUniqueId());
    }
}

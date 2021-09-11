package dev.masa.masuitehomes.bukkit;

import dev.masa.masuitecore.core.api.MaSuiteCoreBukkitAPI;
import dev.masa.masuitecore.core.configuration.BukkitConfiguration;
import dev.masa.masuitehomes.bukkit.commands.MasuiteHomesCommand;
import dev.masa.masuitehomes.bukkit.events.JoinEvent;
import dev.masa.masuitehomes.bukkit.events.LeaveEvent;
import dev.masa.masuitehomes.core.models.Home;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Masa
 */
public class MaSuiteHomes extends JavaPlugin {
    @Getter
    private static MaSuiteHomes plugin;
    public HashMap<UUID, List<Home>> homes = new HashMap<>();

    public BukkitConfiguration config = new BukkitConfiguration();
    public MaSuiteCoreBukkitAPI api = new MaSuiteCoreBukkitAPI();

    @Override
    public void onEnable() {
        plugin = this;

        // Create configs and msg file
        config.create(this, "homes", "config.yml");
        config.addDefault("homes/config.yml", "warmup", 3);
        config.create(this, "homes", "messages.yml");

        // Register channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new HomeMessageListener(this));

        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new LeaveEvent(this), this);

        // Register commands
        MasuiteHomesCommand command = new MasuiteHomesCommand();
        for (final String commandName : getDescription().getCommands().keySet()) {
            Objects.requireNonNull(Bukkit.getPluginCommand(commandName)).setTabCompleter(command);
            Objects.requireNonNull(Bukkit.getPluginCommand(commandName)).setExecutor(command);
        }

        api.getCooldownService().addCooldownLength("homes", config.load("homes", "config.yml").getInt("cooldown"));
        api.getWarmupService().warmupTimes.put("homes", config.load("homes", "config.yml").getInt("warmup"));
    }
}

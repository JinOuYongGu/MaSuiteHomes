package fi.matiaspaavilainen.masuitehomes.bukkit;

import fi.matiaspaavilainen.masuitecore.acf.PaperCommandManager;
import fi.matiaspaavilainen.masuitecore.bukkit.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.core.Updator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitecore.core.utils.CommandManagerUtil;
import fi.matiaspaavilainen.masuitehomes.bukkit.commands.HomeCommand;
import fi.matiaspaavilainen.masuitehomes.bukkit.events.JoinEvent;
import fi.matiaspaavilainen.masuitehomes.bukkit.events.LeaveEvent;
import fi.matiaspaavilainen.masuitehomes.core.models.Home;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MaSuiteHomes extends JavaPlugin {
    public HashMap<UUID, List<Home>> homes = new HashMap<>();

    public BukkitConfiguration config = new BukkitConfiguration();

    @Override
    public void onEnable() {
        // Create configs
        config.create(this, "homes", "config.yml");
        config.create(this, "homes", "syntax.yml");
        config.create(this, "homes", "gui.yml");

        // Register channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new HomeMessageListener(this));

        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new LeaveEvent(this), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new HomeCommand(this));
        manager.getCommandCompletions().registerCompletion("homes", c -> {
            List<String> homeNames = new ArrayList<>();
            if (homes.containsKey(c.getPlayer().getUniqueId())) {
                for (Home home : homes.get(c.getPlayer().getUniqueId())) {
                    homeNames.add(home.getName());
                }
            }
            return homeNames;
        });

        CommandManagerUtil.registerMaSuitePlayerCommandCompletion(manager);
        CommandManagerUtil.registerCooldownCondition(manager);

        new Updator(getDescription().getVersion(), getDescription().getName(), "60632").checkUpdates();

        MaSuiteCore.cooldownService.addCooldownLength("homes", config.load("homes", "config.yml").getInt("cooldown"));
    }
}

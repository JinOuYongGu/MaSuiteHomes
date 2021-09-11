package dev.masa.masuitehomes.bungee;

import dev.masa.masuitecore.bungee.Utils;
import dev.masa.masuitecore.bungee.chat.Formator;
import dev.masa.masuitecore.core.Updator;
import dev.masa.masuitecore.core.api.MaSuiteCoreAPI;
import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitehomes.bungee.controllers.DeleteController;
import dev.masa.masuitehomes.bungee.controllers.ListController;
import dev.masa.masuitehomes.bungee.controllers.SetController;
import dev.masa.masuitehomes.bungee.controllers.TeleportController;
import dev.masa.masuitehomes.core.dataextensions.DataExtensionRegister;
import dev.masa.masuitehomes.core.models.Home;
import dev.masa.masuitehomes.core.services.HomeService;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Masa
 */
public class MaSuiteHomes extends Plugin implements Listener {

    @Getter
    private final MaSuiteCoreAPI api = new MaSuiteCoreAPI();
    public Utils utils = new Utils();
    public BungeeConfiguration config = new BungeeConfiguration();
    public Formator formator = new Formator();
    @Getter
    private HomeService homeService;

    @Override
    public void onEnable() {
        //Configs
        config.create(this, "homes", "messages.yml");
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerListener(this, this);
        // Check updates
        new Updator(getDescription().getVersion(), getDescription().getName(), "60632").checkUpdates();

        config.addDefault("homes/messages.yml", "homes.title-others", "&9%player%''s &7homes: ");
        config.addDefault("homes/messages.yml", "homes.server-name", "&9%server%&7: ");

        homeService = new HomeService(this);

        try {
            DataExtensionRegister.registerHomeExtension(this);
        } catch (NoClassDefFoundError | IllegalStateException | IllegalArgumentException ignored) {
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {
        if (!"BungeeCord".equals(e.getTag())) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        String subchannel = in.readUTF();
        if ("HomeCommand".equals(subchannel)) {
            TeleportController teleport = new TeleportController(this);
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                teleport.teleport(player, in.readUTF());
            }

        }
        if ("HomeOtherCommand".equals(subchannel)) {
            TeleportController teleport = new TeleportController(this);
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                teleport.teleport(player, in.readUTF(), in.readUTF());
            }
        }
        if ("SetHomeCommand".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                SetController set = new SetController(this);
                Location location = new Location().deserialize(in.readUTF());
                set.set(player, in.readUTF(), location, in.readInt(), in.readInt());
            }
        }

        if ("SetHomeOtherCommand".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            String owner = in.readUTF();
            if (utils.isOnline(player)) {
                SetController set = new SetController(this);
                Location location = new Location().deserialize(in.readUTF());
                set.set(player, owner, in.readUTF(), location, in.readInt(), in.readInt());
            }
        }

        if ("DelHomeCommand".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                DeleteController delete = new DeleteController(this);
                delete.delete(player, in.readUTF());
            }
        }

        if ("DelHomeOtherCommand".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                DeleteController delete = new DeleteController(this);
                delete.delete(player, in.readUTF(), in.readUTF());
            }
        }

        if ("ListHomeCommand".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                ListController list = new ListController(this);
                list.list(player);
            }
        }

        if ("ListHomeOtherCommand".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (utils.isOnline(player)) {
                ListController list = new ListController(this);
                list.list(player, in.readUTF());
            }
        }

        if ("ListHomes".equals(subchannel)) {
            listHomes(getProxy().getPlayer(in.readUTF()));
        }
    }

    public void listHomes(ProxiedPlayer p) {
        if (utils.isOnline(p)) {
            for (Home home : homeService.getHomes(p.getUniqueId())) {
                new BungeePluginChannel(this, p.getServer().getInfo(),
                        "AddHome",
                        p.getUniqueId().toString(),
                        home.serialize()
                ).send();
            }
        }
    }
}

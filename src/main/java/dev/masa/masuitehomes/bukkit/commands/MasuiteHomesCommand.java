package dev.masa.masuitehomes.bukkit.commands;

import dev.masa.masuitecore.bukkit.chat.Formator;
import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import dev.masa.masuitehomes.bukkit.MaSuiteHomes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Jin_ou
 */
public class MasuiteHomesCommand implements TabCompleter, CommandExecutor {
    private static final MaSuiteHomes PLUGIN = MaSuiteHomes.getPlugin();
    private static final Formator FORMATOR = new Formator();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if ("sethome".equalsIgnoreCase(label)) {
            return sethomeCmd(sender, args);
        }
        if ("home".equalsIgnoreCase(label)) {
            return homeCmd(sender, args);
        }
        if ("delhome".equalsIgnoreCase(label)) {
            return delhomeCmd(sender, args);
        }
        if ("homes".equalsIgnoreCase(label)) {
            return listHomeCmd(sender, args);
        }

        FORMATOR.sendMessage(sender, PLUGIN.config.load("homes", "messages.yml").getString("wrong-command-format"));
        return false;
    }

    private boolean listHomeCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            new BukkitPluginChannel(PLUGIN, player, "ListHomeCommand", player.getName()).send();
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("masuitehomes.home.list.other")) {
                FORMATOR.sendMessage(sender, PLUGIN.config.load("homes", "messages.yml").getString("no-permission"));
                return false;
            }
            String playerToList = args[0];
            new BukkitPluginChannel(PLUGIN, player, "ListHomeOtherCommand", player.getName(), playerToList).send();
            return true;
        }

        return false;
    }

    private boolean delhomeCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            String home = args[0];
            new BukkitPluginChannel(PLUGIN, player, "DelHomeCommand", player.getName(), home).send();
            return true;
        }

        if (args.length == 2) {
            if (!player.hasPermission("masuitehomes.home.delete.other")) {
                FORMATOR.sendMessage(sender, PLUGIN.config.load("homes", "messages.yml").getString("no-permission"));
                return false;
            }
            String home = args[0];
            String playerToDel = args[1];
            new BukkitPluginChannel(PLUGIN, player, "DelHomeOtherCommand", player.getName(), home, playerToDel).send();
            return true;
        }

        return false;
    }

    private boolean homeCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            PLUGIN.api.getWarmupService().applyWarmup(player, "masuitehomes.warmup.override", "homes", success -> {
                if (success) {
                    String home = args[0];
                    new BukkitPluginChannel(PLUGIN, player, "MaSuiteTeleports", "GetLocation", player.getName(), BukkitAdapter.adapt(player.getLocation()).serialize()).send();
                    new BukkitPluginChannel(PLUGIN, player, "HomeCommand", player.getName(), home).send();
                }
            });
            return true;
        }

        if (args.length == 2) {
            if (!player.hasPermission("masuitehomes.home.teleport.other")) {
                FORMATOR.sendMessage(sender, PLUGIN.config.load("homes", "messages.yml").getString("no-permission"));
                return false;
            }
            String home = args[0];
            String playerToTp = args[1];
            new BukkitPluginChannel(PLUGIN, player, "HomeOtherCommand", player.getName(), home, playerToTp).send();
            return true;
        }

        return false;
    }

    private boolean sethomeCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        String location = BukkitAdapter.adapt(player.getLocation()).serialize();

        if (args.length == 1) {
            String home = args[0];
            new BukkitPluginChannel(PLUGIN, player, "SetHomeCommand", player.getName(), location, home,
                    this.getMaxHomes(player, "global"),
                    this.getMaxHomes(player, "server")).send();
            return true;
        }

        if (args.length == 2) {
            if (!player.hasPermission("masuitehomes.home.set.other")) {
                FORMATOR.sendMessage(sender, PLUGIN.config.load("homes", "messages.yml").getString("no-permission"));
                return false;
            }
            String home = args[0];
            String playerToSet = args[1];
            new BukkitPluginChannel(PLUGIN, player, "SetHomeOtherCommand", player.getName(), home, location, playerToSet, -1, -1).send();
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    private int getMaxHomes(Player player, String type) {
        int max = 0;
        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String perm = permInfo.getPermission();
            if (perm.startsWith("masuitehomes.home.limit." + type)) {
                String amount = perm.replace("masuitehomes.home.limit." + type + ".", "");
                if ("*".equalsIgnoreCase(amount)) {
                    max = -1;
                    break;
                }
                try {
                    if (Integer.parseInt(amount) > max) {
                        max = Integer.parseInt(amount);
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("[MaSuite] [Homes] Please check your home limit permissions (Not an integer or *) ");
                }
            }
        }
        return max;
    }
}

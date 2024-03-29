package me.tehrainbowguy.XBank;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import static me.tehrainbowguy.XBank.MySql.getTop;
import static me.tehrainbowguy.XBank.Util.Message;
import static me.tehrainbowguy.XBank.Util.checkString;


public class XBank extends JavaPlugin {

    @Override
    public void onDisable() {
        try {
            MySql.closeConn();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info(this + " is now disabled!");
    }

    public static FileConfiguration config;

    void setupConfig() {
        config = getConfig();

        try {
            File XBank = new File("plugins" + File.separator + "XBank" + File.separator + "config.yml");
            XBank.mkdir();
            saveConfig();
        } catch (Exception e) {
            log.severe("[XBank] There was a error, you should poke rainbow!");
            e.printStackTrace();
        }
        if (!config.contains("xp.config.buyingprice")) {
            config.set("xp.config.buyingprice", 20.0);
        }
        if (!config.contains("xp.config.charge")) {
            config.set("xp.config.charge", true);
        }
        if (!config.contains("xp.config.chargeamt")) {
            config.set("xp.config.chargeamt", 0.50);
        }
        if (!config.contains("xp.config.minimumdeposit")) {
            config.set("xp.config.minimumdeposit", 1);
        }
        if (!config.contains("xp.config.database")) {
            config.set("xp.config.database", "jdbc:mysql://localhost:3306/minecraft");
        }
        if (!config.contains("xp.config.user")) {
            config.set("xp.config.user", "root");
        }
        if (!config.contains("xp.config.password")) {
            config.set("xp.config.password", "YourAwesomePassword");
        }
        saveConfig();

    }

    private static Permission permission = null;

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
    }

    private Logger log;
    @Override
    public void onEnable() {
        log = getServer().getLogger();
        setupPermissions();
        setupEconomy();
        setupConfig();
        try {
            MySql.initDB();
            MySql.createTables();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info(this + " is now enabled!");
    }

    boolean hasPerm(Player player, String perm) {
        if (permission == null) {
            return player.isOp();
        } else {
            return permission.has(player, perm);
        }
    }

    private static Economy economy = null;

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
    }

    private int getBal(Player p) {
        int bal = 0;
        try {
            bal = MySql.getBalance(p);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bal;
    }

    private void setBal(Player p, int newbal) {
        try {
            MySql.setBalance(p, newbal);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetBal(OfflinePlayer target) {
        try {
            MySql.setBalanceOffline(target, 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player || command.getName().equalsIgnoreCase("xbank")) {
            Player p = (Player) sender;

            if (args.length <= 0) {
                Message(p, "Please use balance, send, deposit, withdraw or top.", false);
                return true;
            }

            if (!hasPerm(p, "XBank.use")) {
                p.sendMessage("You do not have permission to use XBank.");
                return true;
            }
            try {
                MySql.createUser(p);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (hasPerm(p, "xbank.reload")) {
                    setupPermissions();
                    setupEconomy();
                    setupConfig();
                    try {
                        MySql.closeConn();
                        MySql.initDB();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Message(p, "Reloaded", true);
                    log.info("[XBank]:  Reloaded....");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("reset")) {
                if (args.length != 2 || !hasPerm(p, "xbank.reset")) {
                    Message(p, "derp?", false);
                    return true;
                }
                OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
                resetBal(target);
                Message((Player) sender, "The player " + target.getName() + "'s account has been reset!", true);
                log.info("[XBank] " + p.getName() + " has just reset " + target.getName() + "'s account!");
                return true;
            }

            if (args[0].equalsIgnoreCase("withdraw")) {
                if ((args.length != 2) || checkString(args[1])) {
                    Message(p, "derp?", false);
                    return true;
                }

                int currxp = p.getLevel();
                int oldbal = getBal(p);
                int wanttowith;
                if (args[1].equalsIgnoreCase("all")) {
                    wanttowith = oldbal;
                } else {
                    try {
                        wanttowith = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        Message(p, "Try using numbers", false);
                        return true;
                    }
                }
                if (oldbal < wanttowith) {
                    p.sendMessage("Not enough xp");
                    return true;
                } else {
                    int bal2set = oldbal - wanttowith;
                    setBal(p, bal2set);
                    int newbal = getBal(p);
                    p.setLevel(currxp + wanttowith);
                    Message(p, "New balance: " + newbal, true);
                    Message(p, "XP: " + p.getLevel(), true);
                    return true;
                }
            }


            if (args[0].equalsIgnoreCase("balance")) {
                if (args.length != 1) {
                    Message(p, "derp?", false);
                    return true;
                }
                int bal = getBal(p);
                Message(p, "Your balance is " + bal, true);
                //	p.sendMessage("XP: " + p.getLevel());
                return true;
            }

            if (args[0].equalsIgnoreCase("top")) {
                if (args.length != 1) {
                    Message(p, "derp?", false);
                    return true;
                }
                Message(p, "Top ten", true);
                try {
                    int i = 0;
                    // for (Map.Entry<String, Integer> e : MySql.getTop(10).entrySet()) {
                    for (Map.Entry<String, Integer> e : getTop(10)) {
                        i++;
                        Message(p, "#" + i + ": " + e.getKey() + " " + e.getValue(), true);

                    }

                    //}

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                //	p.sendMessage("XP: " + p.getLevel());
                return true;
            }

            if (args[0].equalsIgnoreCase("deposit")) {
                if (args.length != 2 || checkString(args[1])) {
                    Message(p, "derp?", false);
                    return true;
                }
                int currxp = p.getLevel();
                int currbal = getBal(p);

                int wanttodep;
                if (args[1].equalsIgnoreCase("all")) {
                    wanttodep = currxp;
                } else {
                    try {
                        wanttodep = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        Message(p, "Try using numbers", false);
                        return true;
                    }
                }

                if (config.getInt("xp.config.minimumdeposit") > wanttodep) {
                    Message(p, "You need to deposit more! The minimum is " + config.getInt("xp.config.minimumdeposit"), false);
                    return true;
                }
                if (currxp >= wanttodep) {
                    if (config.getBoolean("xp.config.charge")) {
                        if (!economy.has(p.getName(), wanttodep * config.getDouble("xp.config.chargeamt"))) {
                            Message(p, "You are too poor.", false);
                            return true;
                        } else {
                            economy.withdrawPlayer(p.getName(), wanttodep * config.getDouble("xp.config.chargeamt"));
                            Message(p, "You were charged " + wanttodep * config.getDouble("xp.config.chargeamt") + "!", true);
                        }
                    }
                    setBal(p, currbal + wanttodep);
                    int newbal = getBal(p);
                    p.setLevel(currxp - wanttodep);
                    Message(p, "New balance: " + newbal, true);
                    saveConfig();

                    return true;

                } else

                {
                    Message(p, "Not enough xp", false);
                    return true;
                }
            }


            if (args[0].equalsIgnoreCase("send")) {
                if (args.length != 3 || checkString(args[2])) {
                    Message(p, "derp?", false);
                    return true;
                }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) {
                    Message(p, "Could not find player.", false);
                    return true;
                } else if (target == p) {
                    Message(p, "You can't send to your self!.", false);
                    return true;
                } else {

                    int currxp = p.getLevel();
                    int targetxp = target.getLevel();
                    int wanttodep = 0;
                    if (args[2].equalsIgnoreCase("all")) {
                        wanttodep = currxp;
                    } else {
                        try {
                            wanttodep = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            Message(p, "Try using numbers", false);
                            return true;
                        }
                    }
                    if (currxp >= wanttodep) {
                        p.setLevel(currxp - wanttodep);
                        target.setLevel(targetxp + wanttodep);
                        Message(target, p.getDisplayName() + " sent you " + wanttodep + " levels.", true);
                        Message(p, "You sent " + wanttodep + " Levels to " + target.getDisplayName() + ".", true);
                    } else {
                        Message(p, "Sorry you need more XP", false);
                    }
                    return true;
                }


            }
            Message(p, "Wut?", false);
        }

        return false;
    }

}

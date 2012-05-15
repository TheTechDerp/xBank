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
import java.util.logging.Logger;


public class XBank extends JavaPlugin {
	

	
    public void onDisable() {

        log.info(this + " is now disabled!");
    }

//TODO: Replace System.out.println etc with logger (I think is almost done) #Rainbow is lazy
	public static FileConfiguration config;
	
	void setupConfig(){		
    	config = getConfig();
    	
    	try{
    	File XBank = new File("plugins" + File.separator + "XBank" + File.separator + "config.yml");
    	XBank.mkdir();
    	saveConfig();
    	}catch(Exception e){
		log.severe("[XBank] There was a error, please send this stacktrace to the XBank dev team on bukkitdev via a ticket.");
    		e.printStackTrace();
    	}
        if(!config.contains("xp.config.buyingprice")){
            config.set("xp.config.buyingprice", 20.0);
        }
    	if(!config.contains("xp.config.charge")){
        	config.set("xp.config.charge", true);
		}
    	if(!config.contains("xp.config.chargeamt")){
        	config.set("xp.config.chargeamt", 0.50);
		}
    	if(!config.contains("xp.config.minimumdeposit")){
        	config.set("xp.config.minimumdeposit", 1);
		}
		if(!config.contains("xp.config.usedatabase")){
        	config.set("xp.config.usedatabase", false);
		}
		if(!config.contains("xp.config.database")){
        	config.set("xp.config.database", "jdbc:mysql://localhost:3306/minecraft");
		}
		if(!config.contains("xp.config.user")){
        	config.set("xp.config.user", "root");
		}
		if(!config.contains("xp.config.password")){
        	config.set("xp.config.password", "YourAwesomePassword");
		}
    	saveConfig();

	}

    public static Permission permission = null;
    private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    public Logger log;
    public void onEnable() {
    	log = getServer().getLogger();
    	setupPermissions();
    	setupEconomy();
    	setupConfig();
    	if(config.getBoolean("xp.config.usedatabase")){
    		try {
				MySql.createTables();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if(config.contains("xp.user")){
    			MySql.convertYML(config);
			}
    	}
        log.info(this + " is now enabled!");
    }


public boolean hasPerm(Player player, String perm){
	if(permission == null){
		return player.isOp();
	}else{
		return permission.has(player, perm);
	}
}

public static Economy economy = null;

private boolean setupEconomy() {
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
        economy = economyProvider.getProvider();
    }

    return (economy != null);
}
    

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player || command.getName().equalsIgnoreCase("xbank")){
		
		if(args.length <= 0){
			sender.sendMessage("Please use balance, send, deposit, withdraw or top.");
			return true;
			
		}

        Player p = 	(Player) sender;
		if(!hasPerm(p, "XBank.use")){
			p.sendMessage("You do not have permission to use XBank.");
			return true;
		}
		if(config.getBoolean("xp.config.usedatabase")){
			try {
				MySql.createUser(p);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
                    if(!config.contains("xp.user." + p.getName())){
                        config.set("xp.user." + p.getName(), 0);
                        saveConfig();
                    }
                }

            if(args[0].equalsIgnoreCase("reset")){
                    if(hasPerm(p, "xbank.reset")){
                        if(args.length != 2){
                            Util.Message(p, "derp?", false);
                            return true;
                        }
                            OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
                            if(config.getBoolean("xp.config.usedatabase")){
                                try {
                                    MySql.setBalanceOffline(target, 0);
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }else {
                                config.set("xp.user." + target.getName(), 0);
                            }
                            Util.Message((Player) sender, "The player " + target.getName() + "'s account has been reset!", true);
                            log.info("[XBank] " + p.getName() + " has just reset " + target.getName() + "'s account!");

                            return true;

                    }
                    else
                    {
                        Util.Message(p, "You are not allowed to do this",false);
                        return true;
                    }
            }

            if(args[0].equalsIgnoreCase("withdraw")){
                if(args.length != 2){
                    Util.Message(p, "derp?", false);
                    return true;
                }
                    if(Util.checkString(args[1])){
                        Util.Message(p, "No cheating.",false);
                        return true;
                    }
                    int currxp = p.getLevel();
                    //p.sendMessage("" + currxp);
                    int oldbal = 0;
                    if(config.getBoolean("xp.config.usedatabase")){
                        try {
                            oldbal = MySql.getBalance(p);
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }else{
                        oldbal = config.getInt("xp.user." + p.getName().toString());
                    }
                    int wanttowith = Integer.parseInt(args[1]);
                    if(oldbal >= wanttowith ){
                        int newbal = 0;
                        if(config.getBoolean("xp.config.usedatabase")){
                            try {
                                MySql.setBalance(p, oldbal - wanttowith);

                                newbal = MySql.getBalance(p);
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else{
            	config.set("xp.user." + p.getName().toString(), oldbal - wanttowith);
            		newbal = config.getInt("xp.user." + p.getName().toString());
    			}
            	p.setLevel(currxp + wanttowith);

            	Util.Message(p, "New balance: " + newbal,true);
            	Util.Message(p, "XP: " + p.getLevel(),true);
            	saveConfig();

            	return true;
        }else{    			
    	p.sendMessage("Not enough xp");	
    	return true;
    	}
   }


     if(args[0].equalsIgnoreCase("balance")){
         if(args.length != 1){
             Util.Message(p, "derp?", false);
             return true;
         }
    		int bal = 0;
    		if(config.getBoolean("xp.config.usedatabase")){
    			try {
					bal = MySql.getBalance(p);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}else {
    			bal = config.getInt("xp.user." + p.getName().toString());
    		}
    		Util.Message(p, "Your balance is " + bal, true);
    	//	p.sendMessage("XP: " + p.getLevel());
    		return true;
    	}


	     if(args[0].equalsIgnoreCase("deposit")){
             if(args.length != 2){
                 Util.Message(p, "derp?", false);
                 return true;
             }
    		if(Util.checkString(args[1])){
    			Util.Message(p, "No cheating.", false);
    			return true;
    			
    		}
    		
    		String arg1 = args[1];
    		int currxp = p.getLevel();
    		//p.sendMessage("" + currxp);
    		int currbal = 0;
    		if(config.getBoolean("xp.config.usedatabase")){
    			try {
    				currbal = MySql.getBalance(p);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}else {
    			currbal = config.getInt("xp.user." + p.getName().toString());
    		}
    		int wanttodep = Integer.parseInt(arg1);
    		if(config.getInt("xp.config.minimumdeposit") > wanttodep){
    			Util.Message(p, "You need to deposit more! The minimum is " + config.getInt("xp.config.minimumdeposit"), false);
    			return true;
    		}
    		if(currxp >=  wanttodep ){
    			if(config.getBoolean("xp.config.charge")){
    			if(!economy.has(p.getName(), wanttodep * config.getDouble("xp.config.chargeamt"))){
    				Util.Message(p, "You are too poor.", false);
    				return true;
    			}
    			else{
    				economy.withdrawPlayer(p.getName(), wanttodep * config.getDouble("xp.config.chargeamt"));
    				Util.Message(p, "You were charged " + wanttodep * config.getDouble("xp.config.chargeamt") + "!", true);
    			}
    			}
    		
    			int newbal = 0;
        		if(config.getBoolean("xp.config.usedatabase")){
        			try {
    					MySql.setBalance(p, currbal + wanttodep);

        			newbal = MySql.getBalance(p);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		} else {
            	config.set("xp.user." + p.getName(), currbal + wanttodep);
        		newbal = config.getInt("xp.user." + p.getName().toString());
        		}
            	p.setLevel(currxp - wanttodep);

            	Util.Message(p, "New balance: " + newbal, true);
            	Util.Message(p, "XP: " + p.getLevel(), true);
            	saveConfig();
            		
            	return true;

    		}
    		
    		else
    		
    		{	
    	    Util.Message(p, "Not enough xp",false);	
        	return true;
    		}
    	}

            if(args[0].equalsIgnoreCase("sell")){
                if(args.length != 2){
                    Util.Message(p, "derp?", false);
                    return true;
                }
                if(Util.checkString(args[1])){
                    Util.Message(p, "No cheating.", false);
                    return true;

                }

                String arg1 = args[1];
                int currxp = p.getLevel();
                int wanttodep = Integer.parseInt(arg1);
                if(config.getInt("xp.config.minimumdeposit") > wanttodep){
                    Util.Message(p, "You need to deposit more! The minimum is " + config.getInt("xp.config.minimumdeposit"), false);
                    return true;
                }
                if(currxp >=  wanttodep ){
                    p.setLevel(currxp - wanttodep);
//                    log.info("Wanttodep = " + wanttodep);
//                    log.info("buyamt = " +  config.getDouble("xp.config.buyingprice"));

                    economy.depositPlayer(p.getName(), wanttodep * config.getDouble("xp.config.buyingprice"));
                    Util.Message(p,"Deposited " + wanttodep * config.getDouble("xp.config.buyingprice") , true);
                }
                else
                {
                    Util.Message(p, "Not enough xp",false);
                    return true;
                }
                return true;
            }

            if(args[0].equalsIgnoreCase("send")){
                if(args.length != 3){
                    Util.Message(p, "derp?", false);
                    return true;
                }
    		if(Util.checkString(args[2])){
    			Util.Message(p, "No cheating.", false);
    			return true;
    			
    		}
    		 Player target = getServer().getPlayer(args[1]);
    		 if(target == null){
    			 Util.Message(p, "Could not find player.", false);
    			 return true;
    		 } else if (target == p)
    		 {
    			 Util.Message(p, "No cheating.", false);
     			return true;
    		 }
             else if (p.getWorld() != target.getWorld()){
                   Util.Message(p,"You need to be in the same world as " + target.getName() + " to make a trade, sorry for the inconvenience", false);
                  return true;
             }
    		 else
    		 {
    			 
    	    int currxp = p.getLevel();
    		int targetxp = target.getLevel();
    		int wanttodep = Integer.parseInt(args[2]);
    		if(currxp >=  wanttodep ){
    			p.setLevel(currxp - wanttodep);
            	target.setLevel(targetxp + wanttodep );
            	Util.Message(target, p.getDisplayName() + " sent you " + wanttodep + " levels.", true);
            	Util.Message(p, "You sent " + wanttodep + " Levels to " + target.getDisplayName() + ".", true);
    		}else {
    		Util.Message(p, "Sorry you need more XP",false);	
    		}
    		return true;	
    		 }
    		
    	
    		}


            Util.Message(p, "Wut?" , false);

        }

    	return false;
 	}

}

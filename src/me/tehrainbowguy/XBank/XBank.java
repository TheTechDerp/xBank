package me.tehrainbowguy.XBank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class XBank extends JavaPlugin {
	

	
    public void onDisable() {

        System.out.println(this + " is now disabled!");
    }

//DONE: Finish MySql statements.
//TODO: Replace System.out.println etc with logger
	public static FileConfiguration config;

	void setupConfig(){		
    	config = getConfig();

    	try{
    	File XBank = new File("plugins" + File.separator + "XBank" + File.separator + "config.yml");
    	XBank.mkdir();
    	saveConfig();
    	}catch(Exception e){
		System.out.println("[XBank] There was a error, please send this stacktrace to the XBank dev team on bukkitdev via a ticket.");
    		e.printStackTrace();
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

    public void onEnable() {
    	setupPermissions();
    	setupConfig();
    	if(config.getBoolean("xp.config.usedatabase")){
    		try {
				MySql.createTables();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        System.out.println(this + " is now enabled!");
    }



    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player || command.getName().equalsIgnoreCase("xbank")){
		
		if(args.length <= 0){
			sender.sendMessage("Please use balance, send, deposit, withdraw or top.");
			return true;
			
		}
			Player p = 	(Player) sender;
		if(!permission.has(p, "XBank.use")){
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
	//		System.out.println("[XBANK] NOT READY YET REVERTING TO FILE.");
	
		
		}
		else{
			if(!config.contains("xp.user." + p.getName())){
	        	config.set("xp.user." + p.getName(), 0);
	        	saveConfig();
	        	}
		}
		if(args[0].equalsIgnoreCase("export")){
			if(!permission.has(p, "XBank.export")){
				p.sendMessage("You do not have permission to do this.");
				return true;
			}
			if(config.getBoolean("xp.config.usedatabase")){
				p.sendMessage("Not ready for databases yet. Sorry!");
				return true;
			}
			
    	    ConfigurationSection groupSection = config.getConfigurationSection("xp.user"); //saves the section we are in for re-use
    	    Set<String> list = groupSection.getKeys(false); //grabs all keys in the section
    	    Map<String, Integer> map = new LinkedHashMap<String, Integer>(); //this is the map we will store the keys and values in
         

    	    for (String key : list) { //iterate over all keys
    	    map.put(key, groupSection.getInt(key)); //save the values of the keys in our map, assuming that all values are integers
    	    }    	
    	    Map<String, Integer> sorted = Util.sortByValues(map);
    	    int i = 1;    	    
    	       try {
    	            FileWriter writer = new FileWriter("plugins/XBank/export.log", true);
    	            
    	            for( Entry<String, Integer> key : sorted.entrySet()){
      	    	    	writer.write("\n" + i + ". " + key.getKey() + " - " + key.getValue());
    	    	    	i++;
    	    	 
    	    	    }
    	            
    	            writer.close();
    	        } catch (IOException e) {
    	            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    	        }
			
		}
		
		
		
    	if(args[0].equalsIgnoreCase("top")){
    		if(config.getBoolean("xp.config.usedatabase")){
				p.sendMessage("Not ready for databases yet. Sorry!");
				return true;
			}
    	    ConfigurationSection groupSection = config.getConfigurationSection("xp.user"); //saves the section we are in for re-use
    	    Set<String> list = groupSection.getKeys(false); //grabs all keys in the section
    	    Map<String, Integer> map = new LinkedHashMap<String, Integer>(); //this is the map we will store the keys and values in
         

    	    for (String key : list) { //iterate over all keys
    	    map.put(key, groupSection.getInt(key)); //save the values of the keys in our map, assuming that all values are integers
    	    }    	
    	    Map<String, Integer> sorted = Util.sortByValues(map);
    	    int i = 1;
    	    
    	    for( Entry<String, Integer> key : sorted.entrySet()){
    	    if(i <= 10){
    	    	sender.sendMessage(i + ". " + key.getKey() +" - " + key.getValue());
    	    	i++;
    	    }
    	    }
    	    
    	    return true;
    	} else if(args[0].equalsIgnoreCase("reset")){
    	if(permission.has(p, "xbank.reset")){
    		if(args.length == 2){
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
        			sender.sendMessage("The player " + target.getName() + "'s account has been reset!");
        			System.out.println("[XBank] " + p.getName() + " has just reset " + target.getName() + "'s account!");
        		
    			return true;
    		}else {
    			sender.sendMessage("You need to supply a name!");
        		return true;
    		}
    	}
    	else
    	{
    		sender.sendMessage("You are not allowed to do this");
    		return true;
    	}
    	}else if(args[0].equalsIgnoreCase("withdraw")){
    		if(Util.checkString(args[1])){
    			sender.sendMessage("No cheating.");
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

            	p.sendMessage("New balance: " + newbal);
        		p.sendMessage("XP: " + p.getLevel());
            	saveConfig();

            	return true;
        }else{    			
    	p.sendMessage("Not enough xp");	
    	return true;
    	}
			
		}else if(args[0].equalsIgnoreCase("balance")){
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
    		p.sendMessage(ChatColor.GREEN + "[XBank] Your balance is " + bal );
    	//	p.sendMessage("XP: " + p.getLevel());
    		return true;
    	}
    	
    	if(args[0].equalsIgnoreCase("deposit")){
    		if(Util.checkString(args[1])){
    			sender.sendMessage("No cheating.");
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
    			p.sendMessage("You need to deposit more! The minimum is " + config.getInt("xp.config.minimumdeposit"));
    			return true;
    		}
    		if(currxp >=  wanttodep ){
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

            	p.sendMessage("New balance: " + newbal);
        		p.sendMessage("XP: " + p.getLevel());
            	saveConfig();

            	return true;

    		}
    		
    		else
    		
    		{	
    	p.sendMessage("Not enough xp");	
        	return true;
    		}
    	} else if(args[0].equalsIgnoreCase("send")){
    		if(Util.checkString(args[2])){
    			sender.sendMessage("No cheating.");
    			return true;
    			
    		}
    		 Player target = getServer().getPlayer(args[1]);
    		 if(target == null){
    			 p.sendMessage("Could not find player.");
    			 return true;
    		 } else if (target == p)
    		 {
    			 sender.sendMessage("No cheating.");
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
            	target.sendMessage(p.getDisplayName() + " sent you " + wanttodep + " levels.");
            	p.sendMessage("You sent " + wanttodep + " Levels to " + target.getDisplayName() + ".");
    		}else {
    			p.sendMessage("Sorry you need more XP");	
    		}
    		return true;	
    		 }
    		
    	
    		}
    	else {
    		p.sendMessage("Wut?");
    		return true;
    	}
    		
    		
		}    	
    	return false;	
 	}

}

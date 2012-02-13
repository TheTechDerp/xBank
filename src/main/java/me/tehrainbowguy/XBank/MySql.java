package me.tehrainbowguy.XBank;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;



public class MySql {
	
	static FileConfiguration config = XBank.config;
	static String url = config.getString("xp.config.database");
	static String user = config.getString("xp.config.user");
	static String pass = config.getString("xp.config.password");
	
	public static void createTables() throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		PreparedStatement Statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `XBank` (  `id` int(11) NOT NULL auto_increment,  `User` varchar(50) NOT NULL,  `Balance` int(11) NOT NULL,  PRIMARY KEY  (`id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;"); //Put your query in the quotes
		Statement.executeUpdate(); //Executes the query
		Statement.close(); //Closes the query
		conn.close(); //Closes the connection
	}
	public static void setBalance(Player p, int i) throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		PreparedStatement Statement = conn.prepareStatement("UPDATE `XBank` SET Balance='" + i + "' WHERE User='" + p.getName() + "';"); //Put your query in the quotes
		Statement.executeUpdate(); //Executes the query
		Statement.close(); //Closes the query
		conn.close(); //Closes the connection
	}
	public static int getBalance(Player p) throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		Statement state = conn.createStatement();
		ResultSet rs = state.executeQuery("SELECT * FROM `XBank` WHERE User='" + p.getName() + "';");
		ArrayList<Integer> array = new ArrayList<Integer>();
		while(rs.next()){
			array.add(rs.getInt("Balance"));

		}
		int result = Integer.parseInt(array.get(0).toString());
		state.close();
		conn.close();
		return result;
	}
	
	
	public static void setBalanceOffline(OfflinePlayer target, int i) throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		PreparedStatement Statement = conn.prepareStatement("UPDATE `XBank` SET Balance=" + i + " WHERE User='" + target.getName() + "';"); //Put your query in the quotes
		Statement.executeUpdate(); //Executes the query
		Statement.close(); //Closes the query
		conn.close(); //Closes the connection
	}
	
	public static void createUser(Player player) throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
	Statement state = conn.createStatement();
	final ResultSet rs = state.executeQuery("SELECT * FROM `XBank` WHERE User='"+ player.getName() + "';");
	
		
		if(rs.first())
		{
			conn.close();
			return;
		}
		else {
			PreparedStatement Statement1 = conn.prepareStatement("INSERT INTO `XBank` (`id`, `User`, `Balance`) VALUES (NULL, '"+ player.getName() + "', '0');"); //Put your query in the quotes 
			Statement1.executeUpdate();
			Statement1.close();
			//INSERT INTO `XBank` (`id`, `User`, `Balance`) VALUES (NULL, 'TehRainbowGuy', '0');
		}
		state.close();
		conn.close(); //Closes the connection
		return;

	}
	
}
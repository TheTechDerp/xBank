package me.tehrainbowguy.XBank;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

public class Util {

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
    	Comparator<K> valueComparator =  new Comparator<K>() {
    	    public int compare(K k1, K k2) {
    	        int compare = map.get(k2).compareTo(map.get(k1));
    	        if (compare == 0) return 1;
    	        else return compare;
    	    }
    	
 	};
	Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	sortedByValues.putAll(map);

		return sortedByValues;
    }
    
    
	public static boolean checkString(String s){
		Pattern p = Pattern.compile("[^a-zA-Z0-9]");
		boolean hasSpecialChar = p.matcher(s).find();

		return hasSpecialChar;
		
	}

	public static void sendXP(int currxp, int wanttodep, int targetxp, Player p, Player target){
		if(currxp >=  wanttodep ){
			p.setLevel(currxp - wanttodep);
        	target.setLevel(targetxp + wanttodep );
        	target.sendMessage(p.getDisplayName() + " sent you " + wanttodep + " levels.");
        	p.sendMessage("You sent " + wanttodep + " Levels to " + target.getDisplayName() + ".");
		}else {
			p.sendMessage("Sorry you need more XP");	
		}
	}
}

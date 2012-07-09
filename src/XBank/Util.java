package me.tehrainbowguy.XBank;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;


public class Util {

    public static void Message(Player p, String message, Boolean success) {
        if (success) {
            p.sendMessage(ChatColor.GREEN + "[XBank]: " + message);
        } else {
            p.sendMessage(ChatColor.RED + "[XBank]: " + message);
        }
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
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


    public static boolean checkString(String s) {
        Pattern p = Pattern.compile("[^a-z A-Z 0-9]");
        boolean hasSpecialChar = p.matcher(s).find();

        return hasSpecialChar;

    }
}

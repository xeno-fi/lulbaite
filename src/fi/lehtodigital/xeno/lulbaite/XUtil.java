package fi.lehtodigital.xeno.lulbaite;

import org.bukkit.ChatColor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class XUtil {
    
    public static final String INFO_PREFIX = ChatColor.translateAlternateColorCodes('&', "&2[&a❖&2] &7");
    
    public static boolean _writeFile(File file, String data) {
        
        try {
            Files.write(file.toPath(), data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
        
    }
    
    public static String _readFile(File file) {
        
        if (file.exists() && !file.isDirectory()) {
            try {
                return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return null;
        
    }
    
    public boolean writeFile(File file, String data) {
        return _writeFile(file, data);
    }
    
    public String readFile(File file) {
        return _readFile(file);
    }
            
    
}

package fi.lehtodigital.xeno.lulbaite.command;

import fi.lehtodigital.xeno.lulbaite.XUtil;
import fi.lehtodigital.xeno.lulbaite.XenoScriptPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class CommandXScript implements TabExecutor {
    
    private final XenoScriptPlugin plugin;
    
    public CommandXScript(XenoScriptPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission("xscript.admin")) {
            sender.sendMessage(XUtil.INFO_PREFIX + "Insufficient permissions.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(XUtil.INFO_PREFIX + "Usage:");
            sender.sendMessage("    §f/xscript eval [...] §7- evaluate Lua immediately");
            sender.sendMessage("    §f/xscript reload §7- reload scripts");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            
            case "eval":
                
                StringJoiner scriptText = new StringJoiner(" ");
                Arrays.stream(args).skip(1).forEach(scriptText::add);
                String inlineScript = "(function()\n" + scriptText.toString() + "\nend)()";
                
                plugin.getLogger().info("Evaluating: " + inlineScript);
                
                try {
                    plugin.eval(inlineScript);
                } catch (Exception e) {
                    sender.sendMessage(XUtil.INFO_PREFIX + "Errors occurred while evaluating your script:");
                    sender.sendMessage(e.getMessage());
                    e.printStackTrace();
                }

                break;
                
            case "reload":
                
                long timeStart = System.currentTimeMillis();
                
                if (!plugin.reloadScripts()) {
                    sender.sendMessage(XUtil.INFO_PREFIX + "§cErrors occurred while reloading scripts");
                } else {
                    sender.sendMessage(XUtil.INFO_PREFIX + "Scripts reloaded, time passed: " + (System.currentTimeMillis() - timeStart) + "ms");
                }
                
                break;
            
        }
        
        
        return true;
        
    }
    
    
    private static final List<String> TAB_BASE_ARGS = Arrays.asList(
            "reload",
            "eval"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 1) {
            return TAB_BASE_ARGS;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("eval")) {
            return Collections.singletonList("§b[lua]");
        }
        
        return null;
        
    }
    
}

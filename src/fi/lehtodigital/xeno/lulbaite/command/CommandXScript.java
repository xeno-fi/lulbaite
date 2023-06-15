package fi.lehtodigital.xeno.lulbaite.command;

import fi.lehtodigital.xeno.lulbaite.XUtil;
import fi.lehtodigital.xeno.lulbaite.XenoScriptPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
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
            sender.sendMessage(XUtil.INFO_PREFIX + "Oikeutesi eivät riitä.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(XUtil.INFO_PREFIX + "Käytä:");
            sender.sendMessage("    §f/xscript eval [...] §7- suorita JS-pätkä heti");
            sender.sendMessage("    §f/xscript reload §7- lataa komentosarjat uudelleen");
            sender.sendMessage("    §f/xscript location §7- tulosta kopioitava sijaintitieto");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            
            case "eval":
                
                StringJoiner scriptText = new StringJoiner(" ");
                Arrays.stream(args).skip(1).forEach(scriptText::add);
                String inlineScript = "(function(){" + scriptText.toString() + "})();";
                
                plugin.getLogger().info("Evaluating: " + inlineScript);
                
                try {
                    plugin.eval(inlineScript);
                } catch (Exception e) {
                    sender.sendMessage(XUtil.INFO_PREFIX + "Komentosarjan ajamisessa ilmeni virheitä:");
                    sender.sendMessage(e.getMessage());
                    e.printStackTrace();
                }

                break;
                
            case "reload":
                
                long timeStart = System.currentTimeMillis();
                
                if (!plugin.reloadScripts()) {
                    sender.sendMessage(XUtil.INFO_PREFIX + "§cUudelleenlatauksessa ilmeni virheitä");
                } else {
                    sender.sendMessage(XUtil.INFO_PREFIX + "Komentosarjat ladattiin uudelleen, aikaa kului " + (System.currentTimeMillis() - timeStart) + "ms");
                }
                
                break;
            
        }
        
        
        return true;
        
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
    
}

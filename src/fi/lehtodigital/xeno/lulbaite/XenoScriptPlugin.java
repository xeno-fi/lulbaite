package fi.lehtodigital.xeno.lulbaite;

import com.moandjiezana.toml.Toml;
import fi.lehtodigital.xeno.lulbaite.command.CommandXScript;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class XenoScriptPlugin extends JavaPlugin implements Listener {
    
    private static XenoScriptPlugin instance;
    public static XenoScriptPlugin getInstance() {
        return instance;
    }
    
    public final Logger logger = this.getLogger();
    private Toml config;
    private Toml scriptConfig;
    
    private File scriptsFile;
    private File scriptsFolder;
    
    private Globals globals = null;
    
    public void onEnable() {
        

        //System.setProperty("polyglot.lua.nashorn-compat", "true");
        //System.setProperty("js.ecmascript-version", "2021");
        
        instance = this;
        logger.info("Starting up Lulbaite...");
        
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();
        
        File configFile = new File(this.getDataFolder().getAbsolutePath() + "/config.toml");
        if (!configFile.exists()) {
            try {
                logger.info("Saving default config...");
                XUtil._writeFile(configFile, getResourceFileAsString("config.toml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        scriptsFile = new File(this.getDataFolder().getAbsolutePath() + "/scripts.toml");
        if (!scriptsFile.exists()) {
            try {
                logger.info("Saving script config...");
                XUtil._writeFile(scriptsFile, getResourceFileAsString("scripts.toml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        scriptsFolder = new File(this.getDataFolder().getAbsolutePath() + "/scripts/");
        if (!scriptsFolder.exists() || !scriptsFolder.isDirectory()) {
            logger.info("Creating scripts folder...");
            scriptsFolder.mkdirs();
        }

        logger.info("Loading configuration...");
        config = (new Toml()).read(configFile);
        scriptConfig = (new Toml()).read(scriptsFile);

        CommandXScript cmdScript = new CommandXScript(this);
        this.getCommand("xscript").setExecutor(cmdScript);
        this.getCommand("xscript").setTabCompleter(cmdScript);
        
        reloadScripts();

    }
    
    public void onDisable() {
        
        try {
            logger.info("Attempting to disable scripts...");
            globals.load("__runOnDisable()").call();
        } catch (Exception e) {
            logger.warning("Error while disabling scripts: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("Lulbaite has been disabled :c");
        
    }
    
    public void eval(String code) {
        try {
            LuaValue chunk = globals.load(code);
            chunk.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean reloadScripts() {
        
        HandlerList.unregisterAll((Plugin)this);
        Bukkit.getScheduler().cancelTasks(this);
        
        //ClassLoader cl = this.getClass().getClassLoader();
        //Thread.currentThread().setContextClassLoader(Context.class.getClassLoader());
        
        scriptConfig = (new Toml()).read(scriptsFile);

        if (globals != null) {
            try {
                logger.info("Attempting to disable scripts...");
                LuaValue chunk = globals.load("__runOnDisable()");
                chunk.call();
            } catch (Exception e) {
                logger.warning("Error while disabling scripts: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        globals = JsePlatform.standardGlobals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new Bit32Lib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new CoroutineLib());
        globals.load(new JseMathLib());
        globals.load(new JseIoLib());
        globals.load(new JseOsLib());
        globals.load(new FixedLuajavaLib());
        
        try {
            LuaValue baseChunk = globals.load(getResourceFileAsString("base.lua"));
            baseChunk.call();
        } catch (Exception e) {
            throw new RuntimeException("Unable to run base lua", e);
        }
        
        // init scripts
        List<String> enabledScripts = scriptConfig.getList("enabled_scripts", new ArrayList<String>());
        boolean hadErrors = false;
        
        for (String fileName:enabledScripts) {
            
            File scriptFile = new File(scriptsFolder + "/" + fileName);
            logger.info("Reading Lua: " + fileName);
            
            if (!fileName.toLowerCase(Locale.ROOT).endsWith(".lua")) {
                logger.warning("Unsupported file extension: '" + fileName + "'");
                hadErrors = true;
                continue;
            }
            
            if (!scriptFile.exists()) {
                logger.warning("File doesn't exist: '" + fileName + "'");
                hadErrors = true;
                continue;
            }
            
            String scriptData = XUtil._readFile(scriptFile);
            
            if (scriptData == null) {
                logger.warning("File '" + fileName + "' could not be read");
                hadErrors = true;
                continue;
            }
            
            try {
                LuaValue scriptChunk = globals.load(scriptData);
                scriptChunk.call();
            } catch (Exception e) {
                logger.warning("Exception(s) while running '" + fileName + "':");
                e.printStackTrace();
                logger.warning("\n\n");
                hadErrors = true;
            }

        }
        
        return !hadErrors;
        
    }

    public String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = this.getResource(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
    
    
    private Map<String, Command> commandCache = new ConcurrentHashMap<>();

    public void registerCommand(String commandName, Command command) {

        if (!commandCache.containsKey(commandName)) {

            try {

                Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);

                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
                commandMap.register(commandName, new Command(commandName) {
                    @Override
                    public boolean execute(CommandSender commandSender, String label, String[] args) {
                        commandCache.get(commandName).execute(commandSender, label, args);
                        return true;
                    }
                });

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            
        }
        
        commandCache.put(commandName, command);

    }

    public XUtil getUtil() {
        return new XUtil();
    }
    
    public void registerEvent(String eventPath, LuaEventHandler callback) {
        try {
            Class<? extends Event> eventClass = (Class<? extends Event>)Class.forName(eventPath);
            Bukkit.getPluginManager().registerEvent(eventClass, new Listener(){}, EventPriority.NORMAL, (listener, event) -> {
                callback.run(CoerceJavaToLua.coerce(event));
            }, this);
        } catch (Exception e) {
            logger.warning("Trying to register event " + eventPath + " but was not found");
        }
    }
    
    public void runLater(LuaRunnable callback, int time) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            callback.run();
        }, time);
    }
    
    public void runTimer(LuaRunnable callback, int delay, int time) {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            callback.run();
        }, delay, time);
    }

}

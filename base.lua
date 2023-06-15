System = luajava.bindClass('java.lang.System')

Bukkit = luajava.bindClass('org.bukkit.Bukkit')
Server = Bukkit:getServer()
PluginManager = Bukkit:getPluginManager()

File = luajava.bindClass('java.io.File')
Material = luajava.bindClass('org.bukkit.Material')
Particle = luajava.bindClass('org.bukkit.Particle')
Sound = luajava.bindClass('org.bukkit.Sound')

plugin = PluginManager:getPlugin('Lulbaite')
logger = plugin:getLogger()
Util = plugin:getUtil()

local __onDisable = {}

function __registerOnDisable(fn)
    table.insert(__onDisable, fn)
end

function __runOnDisable()
    for k,fn in pairs(__onDisable) do
        fn()
    end
end

function registerEvent(eventName, callback)
    local luaEventHandler = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.LuaEventHandler', {
        run = callback
    })
    plugin:registerEvent(eventName, luaEventHandler)
end

function runLater(callback, time)
    local luaRunnable = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.LuaRunnable', {
        run = callback
    })
    plugin:runLater(luaRunnable, time)
end

function runTimer(callback, delay, time)
    local luaRunnable = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.LuaRunnable', {
        run = callback
    })
    plugin:runTimer(luaRunnable, delay, time)
end

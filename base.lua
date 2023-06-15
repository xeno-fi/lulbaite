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

function size(T)
    local count = 0
    for _ in pairs(T) do count = count + 1 end
    return count
end


function string.starts(str, start)
    return string.sub(str, 1, string.len(start)) == start
end

function string.split(inputstr, sep)
    if sep == nil then
        sep = "%s"
    end
    local t={}
    for str in string.gmatch(inputstr, "([^"..sep.."]+)") do
        table.insert(t, str)
    end
    return t
end

local __onDisable = {}

function __registerOnDisable(fn)
    table.insert(__onDisable, fn)
end

function __runOnDisable()
    for k,fn in pairs(__onDisable) do
        fn()
    end
end


function registerCommand(commandLabel, callback)
    local luaCommandCallback = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.utils.LuaCommandCallback', {
        run = callback
    })
    plugin:registerCommand(commandLabel, luaCommandCallback)
end

function registerEvent(eventName, callback)
    local luaEventHandler = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.utils.LuaEventHandler', {
        run = callback
    })
    plugin:registerEvent(eventName, luaEventHandler)
end

function runLater(callback, time)
    local luaRunnable = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.utils.LuaRunnable', {
        run = callback
    })
    plugin:runLater(luaRunnable, time)
end

function runTimer(callback, delay, time)
    local luaRunnable = luajava.createProxy('fi.lehtodigital.xeno.lulbaite.utils.LuaRunnable', {
        run = callback
    })
    plugin:runTimer(luaRunnable, delay, time)
end

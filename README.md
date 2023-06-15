<p align="center">

![](https://lehtodigital.fi/f/JDidg)

</p>

**Lulbaite** is a Spigot/Paper scripting plugin.
Using Lulbaite, you can write Lua scripts to add functionality to your server.

**Disclaimer: Java and Spigot API knowledge is required.** This plugin is **not** a beginner-friendly scripting plugin.
Basically Lulbaite just lets you write regular Java plugin functionality,
except that you write Lua with LuaJ.

If you need something easy and fun, check out
[Skript](https://github.com/SkriptLang/Skript),
[Denizen](https://github.com/DenizenScript/Denizen)
or even [data packs](https://minecraft.fandom.com/wiki/Data_pack).

## Features
- ⚡️ Lightning fast [LuaJ](https://github.com/luaj/luaj)
- ⛏️ Convenience methods for fast and useful scripting
- ♻️ Fast reloading - no need to restart your server when programming

## Installation
1. Download the latest release
2. Place the `.jar` file into your `plugins` folder
3. Restart your server
4. Now you will be ready for some scripting!

## Writing a script
**Creating scripts with Lulbaite requires Java knowledge.**
Using LuaJ, you can use and manipulate
all normal Spigot and Java classes and objects.

### 1. Create a script file
First, you need to create a script file under the `scripts` folder
inside the Lulbaite plugin data folder.
For example, you could name it `test.lua`.

### 2. Add script file to scripts.toml
Lulbaite will only load script files that are listed in `scripts.toml`.
Thus, we will add our newly created file to it:

```toml
enabled_scripts = [
  "test.lua"
]
```

### 3. Start coding!
Now all the fun starts!
We will write our first Lua script.

Let's write an event handler
that will tell the player the material id of any block they place.

For this, we need to go check the full class path of the `BlockPlaceEvent` on the [Javadocs](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/BlockPlaceEvent.html).
In this case, it is `org.bukkit.event.block.BlockPlaceEvent`.

Now we can write our script:

```lua
-- test.lua

registerEvent('org.bukkit.event.block.BlockPlaceEvent', function(event)

    local player = event:getPlayer()
    local placedMaterial = event:getBlockPlaced():getType():name()

    player:sendMessage('Placing ' .. tostring(placedMaterial))

end)
```

### 4. Save and reload!
Save your Lua file and use the `/xscript reload` command to reload your changes.
If your script contains syntax errors,
you will be notified, and the Lua error stack will be printed
to your console.

### 5. Try it out!
Ta-da! You have succesfully created your first Lua script!

![](https://lehtodigital.fi/f/kDeFF)


## Basic ideas
Here's the documentation for the basics.

### Binding Java classes
Use the `luajava.bindClass` function to bind any Java class.
Use `:` to call methods on Java objects and classes. 
```lua
local System = luajava.bindClass('java.lang.System')
print('The time is ' .. tostring(System:currentTimeMillis()))
```

### Creating instances of Java classes
Create new instances of Java classes using `:new()`
```lua
local EulerAngle = luajava.bindClass('org.bukkit.util.EulerAngle')
local angle = EulerAngle:new(math.pi, 0, 0)
```

### Using instanceof
We also provide a simple `instanceof` function for checking if a coerced object in Lua
is an instance of a specific Java class.

You can use it with class names:
```lua
if (instanceof('org.bukkit.entity.Player', object)) then
    -- do the magic
end
```

Or with coerced/bound Java classes:
```lua
local Player = luajava.bindClass('org.bukkit.entity.Player')
if (instanceof(Player, object)) then
    -- do the magic
end
```

### Registering events
Use `registerEvent(class, handler)` to register event handlers.

```lua
registerEvent('org.bukkit.event.block.BlockPlaceEvent', function(event)

    local player = event:getPlayer()
    local placedMaterial = event:getBlockPlaced():getType():name()

    player:sendMessage('Placing ' .. tostring(placedMaterial))

end)
```

### Registering commands
Use `registerCommand(label, handler)` to register commands.
For example, the command below
only works for players,
and it will make the player jump on execution.

```lua
registerCommand('luatest', function(sender, label, args)

    if (not instanceof(Player, sender)) then
        sender:sendMessage('This command only works for players.')
        return
    end

    local player = sender

    -- make them jump! >:3
    local vel = player:getVelocity():clone()
    vel:setY(1)

    player:sendMessage('Yaaay! You are a player!')
    player:setVelocity(vel)

end)
```

### Using the scheduler
```lua
-- Run after 5 ticks
runLater(function()
    print('5 ticks passed!')
end, 5)

-- Run after 20 ticks, repeat every 10 ticks
runTimer(function()
    print('Wooo!')
end, 20, 10)
```

### Disable hooks
```lua
-- The function will be called when the scripts are disabled/reloaded
__registerOnDisable(function()
    print('Noooooo, they are killing me!')
end)
```

### Base classes
Lulbaite binds some classes to the global scope by default:

```lua
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
```

So, for example, you can use the logger...
```lua
logger:info('Hello world!')
```

...or `Material` (also `Sound`, `Particle`)
```lua
location:getBlock():setType(Material.IRON_BLOCK)
```

### Utility functions
Since Lua does not have all the basic functionality one could wish for,
we have included some utility functions in our base code.

#### size(T)
```lua
local arr = {'a', 'b', 'c'}
local s = size(arr) -- = 3
```

#### string.starts(str, start)
Check if a string starts with a substring
```lua
if (string.starts('hello world', 'hell')) then
    print('It does!')
end 
```

#### string.split(str, separator)
Split a string into parts
```lua
local parts = string.split('kissa koira apina', ' ')
print(parts[2]) -- prints 'koira'
```

### ...and more to come!
We use Lulbaite ourselves at [Xeno](https://xeno.fi/).
Thus, we add features when we need them.
Feel free to suggest new features,
or to add pull requests if you come up with something awesome!


---

## License
MIT

---

## Contributing
Pull requests are welcome.
However, the purpose of this plugin is to act as a quick and dirty way
to script a simple feature or to automate something,
instead of trying to be a full-blown plugin framework.

If that's what you need,
please check out [Lukkit](https://github.com/Lukkit/Lukkit) instead!
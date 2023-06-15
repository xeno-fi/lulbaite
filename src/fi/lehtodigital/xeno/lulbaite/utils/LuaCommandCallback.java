package fi.lehtodigital.xeno.lulbaite.utils;

import org.luaj.vm2.LuaValue;

public interface LuaCommandCallback {
    LuaValue run(LuaValue sender, LuaValue label, LuaValue args);
}

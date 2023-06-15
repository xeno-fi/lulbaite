package fi.lehtodigital.xeno.lulbaite.utils;

import org.luaj.vm2.LuaValue;

public interface LuaEventHandler {
    LuaValue run(LuaValue e);
}

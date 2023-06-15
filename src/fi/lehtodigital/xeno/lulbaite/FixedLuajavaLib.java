package fi.lehtodigital.xeno.lulbaite;

import org.luaj.vm2.lib.jse.LuajavaLib;

public class FixedLuajavaLib extends LuajavaLib {
    
    @Override
    protected Class classForName(String name) {
        ClassLoader loader = XenoScriptPlugin.class.getClassLoader();
        try {
            Class<?> clazz = Class.forName(name, true, loader);
            return clazz;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load class " + name);
        }
    }
    
}

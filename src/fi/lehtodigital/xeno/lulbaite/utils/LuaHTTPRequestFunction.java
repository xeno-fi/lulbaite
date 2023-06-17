package fi.lehtodigital.xeno.lulbaite.utils;

import fi.lehtodigital.xeno.lulbaite.XenoScriptPlugin;
import org.bukkit.Bukkit;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LuaHTTPRequestFunction extends ThreeArgFunction {
    
    @Override
    public LuaValue call(LuaValue url, LuaValue rawOptions, LuaValue callback) {
        
        if (!url.isstring()) {
            throw new IllegalArgumentException("httpRequest: First argument should be a string (the URL).");
        }
        
        LuaValue options = rawOptions.isnil() || !rawOptions.istable()
                ? new LuaTable()
                : rawOptions;
        
        if (callback.isnil()) {
            try {
                
                HttpResponse<String> response = httpRequest(url.checkjstring(), options.isnil() ? new LuaTable() : options.checktable());
                
                LuaTable tableOut = new LuaTable();
                tableOut.set(CoerceJavaToLua.coerce("error"), CoerceJavaToLua.coerce(false));
                tableOut.set(CoerceJavaToLua.coerce("statusCode"), CoerceJavaToLua.coerce(response.statusCode()));
                tableOut.set(CoerceJavaToLua.coerce("body"), CoerceJavaToLua.coerce(response.body()));
                
                return tableOut;
                
            } catch (RuntimeException | IOException | InterruptedException e) {

                LuaTable tableOut = new LuaTable();
                tableOut.set(CoerceJavaToLua.coerce("error"), CoerceJavaToLua.coerce(true));
                tableOut.set(CoerceJavaToLua.coerce("statusCode"), CoerceJavaToLua.coerce(-1));
                tableOut.set(CoerceJavaToLua.coerce("body"), CoerceJavaToLua.coerce(e.getMessage()));

                return tableOut;
                
            }
        } else {
            
            Bukkit.getScheduler().runTaskAsynchronously(XenoScriptPlugin.getInstance(), () -> {
                try {
                    HttpResponse<String> response = httpRequest(url.checkjstring(), options.isnil() ? new LuaTable() : options.checktable());
                    Bukkit.getScheduler().runTask(XenoScriptPlugin.getInstance(), () -> {
                        callback.call(
                                CoerceJavaToLua.coerce(false),
                                CoerceJavaToLua.coerce(response.statusCode()),
                                CoerceJavaToLua.coerce(response.body())
                        );
                    });
                } catch (RuntimeException | IOException | InterruptedException e) {
                    Bukkit.getScheduler().runTask(XenoScriptPlugin.getInstance(), () -> {
                        callback.call(
                                CoerceJavaToLua.coerce(true),
                                CoerceJavaToLua.coerce(-1),
                                CoerceJavaToLua.coerce(e.getMessage())
                        );
                    });
                }
            });
            
            return LuaValue.NIL;
            
        }
        
    }
    
    private HttpResponse<String> httpRequest(String url, LuaTable options) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        
        requestBuilder.uri(URI.create(url));
        
        switch (options.get("method").optjstring("GET")) {
            case "GET": {
                requestBuilder.GET();
                break;
            }
            case "POST": {
                if (options.get("body").isnil()) {
                    requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                } else {
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(options.get("body").checkjstring()));
                }
                break;
            }
            case "DELETE": {
                requestBuilder.DELETE();
                break;
            }
            case "PUT": {
                if (options.get("body").isnil()) {
                    requestBuilder.PUT(HttpRequest.BodyPublishers.noBody());
                } else {
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(options.get("body").checkjstring()));
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("httpRequest: Method " + options.optjstring("method") + " is not supported.");
            }
        }
        
        if (options.get("headers").istable()) {
            
            LuaTable headers = options.get("headers").checktable();
            
            for (LuaValue key:headers.keys()) {
                
                if (!headers.get(key).isstring() || !key.isstring()) {
                    throw new IllegalArgumentException("httpRequest: Keys and values in 'headers' should be strings.");
                }
                
                requestBuilder.setHeader(key.checkjstring(), headers.get(key).checkjstring());
                
            }
            
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response;

    }
    
}

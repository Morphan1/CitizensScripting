package net.Morphan1.citizensscripting;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.Script;
import net.citizensnpcs.api.scripting.ScriptFactory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CitizensScripting extends JavaPlugin {

    private final File rootFolder = new File(getDataFolder(), "plugins/CitizensScripting");
    private ArrayList<Future<ScriptFactory>> scripts = new ArrayList<Future<ScriptFactory>>();
    
    @Override
    public void onEnable() {;
        if (!rootFolder.exists())
            rootFolder.mkdirs();
        
        reload();
    }
    
    public void reload() {
        getServer().getLogger().info("[CitizensScripts] Reloading scripts.");
        clearScripts();
        for (File file : rootFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".js")) return true;
                return false;
            }
        })) {
            scripts.add(CitizensAPI.getScriptCompiler().compile(file).withCallback(new BehaviourCallback()).beginWithFuture());
        }
    }
    
    public void clearScripts() {
        if (scripts.isEmpty()) return;
        for (Future<ScriptFactory> future : scripts) {
            future.cancel(true);
        }
        scripts.clear();
    }

    public class BehaviourCallback implements CompileCallback {

        @Override
        public void onScriptCompiled(String file, ScriptFactory script) {
            final Script instance = script.newInstance();
            Bukkit.getScheduler().callSyncMethod(CitizensScripting.this, new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    synchronized (CitizensScripting.this) {
                        instance.invoke("startScript", CitizensScripting.this);
                    }
                    return null;
                }
            });
        }
        
    }

}

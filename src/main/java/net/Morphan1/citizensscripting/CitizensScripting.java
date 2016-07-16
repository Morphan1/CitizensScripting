package net.Morphan1.citizensscripting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.Script;
import net.citizensnpcs.api.scripting.ScriptFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class CitizensScripting extends JavaPlugin {

    private List<Future<ScriptFactory>> scripts = new ArrayList<Future<ScriptFactory>>();

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        reload();
    }

    public void reload() {
        getServer().getLogger().info("[CitizensScripts] Reloading scripts.");
        clearScripts();
        for (File file : getDataFolder().listFiles(jsFileFilter)) {
            scripts.add(CitizensAPI.getScriptCompiler().compile(file).withCallback(new BehaviourCallback()).beginWithFuture());
        }
    }

    public void clearScripts() {
        if (scripts.isEmpty()) {
            return;
        }
        for (Future<ScriptFactory> future : scripts) {
            future.cancel(true);
        }
        scripts.clear();
    }

    public class BehaviourCallback implements CompileCallback {
        @Override
        public void onScriptCompiled(String file, ScriptFactory script) {
            final Script instance = script.newInstance();
            Bukkit.getScheduler().runTask(CitizensScripting.this, new Runnable() {
                @Override
                public void run() {
                    instance.invoke("startScript", CitizensScripting.this);
                }
            });
        }
    }

    private static final FilenameFilter jsFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".js");
        }
    };
}

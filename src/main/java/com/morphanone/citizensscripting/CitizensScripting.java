package com.morphanone.citizensscripting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.Script;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.scripting.ScriptFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        File[] files = getDataFolder().listFiles(jsFileFilter);
        if (files == null) {
            return;
        }
        ScriptCompiler compiler = CitizensAPI.getScriptCompiler();
        for (File file : files) {
            scripts.add(compiler.compile(file).withCallback(new BehaviourCallback()).beginWithFuture());
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("csreload")) {
            reload();
            return true;
        }
        return false;
    }

    private static final FilenameFilter jsFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".js");
        }
    };
}

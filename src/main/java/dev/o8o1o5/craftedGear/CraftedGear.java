package dev.o8o1o5.craftedGear;

import org.bukkit.plugin.java.JavaPlugin;

public final class CraftedGear extends JavaPlugin {

    private static CraftedGear instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        getLogger().info("CraftedGear 플러그인이 활성화되었습니다!");

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("CraftedGear 플러그인이 비활성화되었습니다!");
    }

    public static CraftedGear getInstance() {
        return instance;
    }
}

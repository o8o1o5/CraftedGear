package dev.o8o1o5.craftedGear.generators;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ResourcePackGenerator {

    private final JavaPlugin plugin;
    private final File dataFolder; // plugins/CraftedGear/

    public ResourcePackGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder= plugin.getDataFolder();
    }

    public File generateAndZipResourcePack() throws IOException, NoSuchAlgorithmException {
        plugin.getLogger().info("리소스팩 생성 시작...");

        File buildFolder = new File(dataFolder, "resourcepack_build");
        File outputZipFile = new File(dataFolder, "crafted_gear_pack.zip");

        if (buildFolder.exists()) {

        }
        buildFolder.mkdirs();
    }
    
    private void deleteFoler(File folder) {
        // 삭제 로직
    }
    
    private void createPackMcMeta(File buildFolder) throws IOException {
        // pack.mcmeta 생성 로직
    }
}

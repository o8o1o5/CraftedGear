package dev.o8o1o5.craftedGear.generators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.o8o1o5.craftedGear.items.CustomItemData;
import dev.o8o1o5.craftedGear.managers.ItemManager;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

public class ResourcePackGenerator {

    private final JavaPlugin plugin;
    private final ItemManager itemManager;
    private final File dataFolder; // plugins/CraftedGear/

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final int PACK_FORMAT = 55;
    private static final String PACK_DESCRIPTION = "CraftedGear Custom Resource Pack for 1.21+";

    private final File customTextureSourceFolder;

    public ResourcePackGenerator(JavaPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.dataFolder= plugin.getDataFolder();
        this.customTextureSourceFolder = new File(dataFolder, "textures");
    }

    public File generateAndZipResourcePack() throws IOException, NoSuchAlgorithmException {
        plugin.getLogger().info("리소스팩 생성 시작...");

        File buildFolder = new File(dataFolder, "resourcepack_build");
        File outputZipFile = new File(dataFolder, "crafted_gear_pack.zip");

        deleteFolder(buildFolder);
        buildFolder.mkdirs();

        if (!customTextureSourceFolder.exists()) {
            customTextureSourceFolder.mkdirs();
            plugin.getLogger().warning("커스텀 텍스처 폴더가 비어있습니다: " + customTextureSourceFolder.getAbsolutePath());
            plugin.getLogger().warning("텍스처 파일을 여기에 넣어주세요! (예: crafted_gear.png)");
        }

        File minecraftItemFolder = new File(buildFolder, "assets/minecraft/items");
        minecraftItemFolder.mkdirs();
        File commonModelTargetFolder = new File(buildFolder, "assets/minecraft/models/item");
        commonModelTargetFolder.mkdirs();
        File commonTextureTargetFolder = new File(buildFolder, "assets/minecraft/textures/item");
        commonTextureTargetFolder.mkdirs();

        createPackMcMeta(buildFolder);
        plugin.getLogger().info("pack.mcmeta 생성 중...");

        copyCustomTextures(buildFolder)
        plugin.getLogger().info("텍스쳐 파일 불러오기...");

        plugin.getLogger().info("커스텀 모델 JSON 생성 중...");
        Map<Material, List<CustomItemData>> itemsByMaterial = new HashMap<>();
        for (CustomItemData itemData : itemManager.getCustomDatas()) {
            createCustomModelJson(
                    new File(commonModelTargetFolder, itemData.getId().toLowerCase() + ".json"),
                    "item/" + itemData.getId().toLowerCase()
            );
        }

        plugin.getLogger().info("Material 모델 오버라이드 생성 중...");
        for (Map.Entry<Material, List<CustomItemData>> entry : itemsByMaterial.entrySet()) {
            Material material = entry.getKey();
            List<CustomItemData> itemsForMaterial = entry.getValue();

            File materialModelFile = new File(minecraftItemFolder, material.name().toLowerCase() + ".json");
            createSelectModelJson(materialModelFile, material, itemsForMaterial);
        }

        zipFolder(buildFolder, outputZipFile);
        plugin.getLogger().info("리소스팩 ZIP 파일 생성하기...");

        String sha1 = calculateSha1(outputZipFile);
        plugin.getLogger().info("리소스팩 SHA1 해시 생성하기...");

        deleteFolder(buildFolder);

        return outputZipFile;
    }

    private void deleteFolder(File folder) throws IOException {
        if (!folder.exists()) {
            return;
        }
        try (Stream<Path> walk = Files.walk(folder.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        plugin.getLogger().info("폴더 삭제 완료: " + folder.getAbsolutePath());
    }

    private void createPackMcMeta(File buildFolder) throws IOException {
        File mcmetaFile = new File(buildFolder, "pack.mcmeta");
        JsonObject packObject = new JsonObject();
        JsonObject packInner = new JsonObject();
        packInner.addProperty("pack_format", PACK_FORMAT);
        packInner.addProperty("description", PACK_DESCRIPTION);
        packObject.add("pack", packInner);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(mcmetaFile), StandardCharsets.UTF_8)) {
            GSON.toJson(packObject, writer);
        }
    }

    private void copyCustomTextures(File buildFolder) throws IOException {
        File targetTextureFolder = new File(buildFolder, "assets/minecraft/textures/item");
        if (!targetTextureFolder.exists()) {
            targetTextureFolder.mkdirs();
        }

        Set<String> requiredTextureFileNames = new HashSet<>();
        for (CustomItemData itemData : itemManager.getCustomDatas()) {
            requiredTextureFileNames.add(itemData.getId().toLowerCase() + ".png");
        }

        if (customTextureSourceFolder.exists() && customTextureSourceFolder.isDirectory()) {
            File[] files = customTextureSourceFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") && requiredTextureFileNames.contains(name.toLowerCase()));
            if (files != null) {
                for (File sourceFile : files) {
                    File targetFile = new File(targetTextureFolder, sourceFile.getName());
                    Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("텍스처 복사됨: " + sourceFile.getName());
                }
            }
        }
    }

    private void createCustomModelJson(File buildFolder, String modelName, String texturePath) throws IOException {
        // 커스텀 모델 JSON 파일 생성 로직
    }

    private void createVanillaModelOverrideJson(File buildFolder, String vanillaItemName, List<Object> overrides) throws IOException {
        // 바닐라 아이템 모델 오버라이드 JSON 생성 로직
    }

    private void zipFolder(File sourceFolder, File outputZipFile) throws IOException {
        // 폴더를 ZIP 파일로 압축하는 로직
    }

    private String calculateSha1(File file) throws IOException, NoSuchAlgorithmException {
        // SHA1 해시 계산 로직
        return ""; // 실제 해시 값 반환
    }
}

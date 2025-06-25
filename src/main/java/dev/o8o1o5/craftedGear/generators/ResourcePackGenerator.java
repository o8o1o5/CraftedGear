package dev.o8o1o5.craftedGear.generators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        File minecraftItemFolder = new File(buildFolder, "assets/minecraft/items"); // <--- 문제의 줄
        minecraftItemFolder.mkdirs();

        File commonModelTargetFolder = new File(buildFolder, "assets/minecraft/models/item");
        commonModelTargetFolder.mkdirs();

        File commonTextureTargetFolder = new File(buildFolder, "assets/minecraft/textures/item");
        commonTextureTargetFolder.mkdirs();

        createPackMcMeta(buildFolder);
        plugin.getLogger().info("pack.mcmeta 생성 중...");

        copyCustomTextures(buildFolder);
        plugin.getLogger().info("텍스쳐 파일 불러오기...");

        plugin.getLogger().info("커스텀 모델 JSON 생성 중...");
        Map<Material, List<CustomItemData>> itemsByMaterial = new HashMap<>();
        for (CustomItemData itemData : itemManager.getCustomDatas()) {
            createCustomModelJson(
                    new File(commonModelTargetFolder, itemData.getId().toLowerCase() + ".json"),
                    "item/" + itemData.getId().toLowerCase()
            );
            itemsByMaterial.computeIfAbsent(itemData.getMaterial(), k -> new ArrayList<>()).add(itemData);
            plugin.getLogger().info("커스텀 모델 JSON 파일 생성: " + itemData.getId().toLowerCase() + ".json");
        }

        plugin.getLogger().info("Material 모델 오버라이드 생성 중...");
        for (Map.Entry<Material, List<CustomItemData>> entry : itemsByMaterial.entrySet()) {
            Material material = entry.getKey();
            List<CustomItemData> itemsForMaterial = entry.getValue();

            File materialModelFile = new File(minecraftItemFolder, material.name().toLowerCase() + ".json");
            createMaterialSelectModelJson(materialModelFile, material, itemsForMaterial);
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

    private void createCustomModelJson(File outputFile, String texturePath) throws IOException {
        JsonObject modelObject = new JsonObject();
        modelObject.addProperty("parent", "item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texturePath);
        modelObject.add("textures", textures);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            GSON.toJson(modelObject, writer);
        }
    }

    private void createMaterialSelectModelJson(File outputFile, Material baseMaterial, List<CustomItemData> customItemsForMaterial) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "select");
        model.addProperty("property", "custom_model_data");

        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "model");
        fallback.addProperty("model", "item/" + baseMaterial.name().toLowerCase());
        model.add("fallback", fallback);

        JsonArray cases = new JsonArray();
        for (CustomItemData itemData : customItemsForMaterial) {
            JsonObject caseEntry = new JsonObject();
            caseEntry.addProperty("when", itemData.getId());
            JsonObject caseModel = new JsonObject();
            caseModel.addProperty("type", "model");
            caseModel.addProperty("model", "item/" + itemData.getId().toLowerCase());
            caseEntry.add("model", caseModel);
            cases.add(caseEntry);
        }

        model.add("cases", cases);
        root.add("model", model);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    private void zipFolder(File sourceFolder, File outputZipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputZipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path sourcePath = sourceFolder.toPath();
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString().replace("\\", "/"));
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            plugin.getLogger().severe("ZIP 압축 중 오류 발생! " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    public String calculateSha1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file);
             DigestInputStream dis = new DigestInputStream(fis, md)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {

            }
        }
        byte[] hashBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

package dev.o8o1o5.craftedGear.manager;

import dev.o8o1o5.craftedGear.CraftedGear;
import dev.o8o1o5.craftedGear.items.CustomItemData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ItemManager {

    private final CraftedGear plugin;
    private final Map<String, CustomItemData> loadedItems;
    private final NamespacedKey customItemIdKey;

    public ItemManager(CraftedGear plugin) {
        this.plugin = plugin;
        this.loadedItems = new HashMap<>();
        this.customItemIdKey = new NamespacedKey(plugin, "custom_item_id");
    }

    public void loadItems() {
        loadedItems.clear();
        plugin.getLogger().info(plugin.getMessagePrefix() + "커스텀 아이템 데이터를 로드 중...");

        File itemsFolder = new File(plugin.getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
            plugin.getLogger().info(plugin.getMessagePrefix() + "items 폴더가 생성되었습니다. 아이템 파일을 넣어주세요.");
            return;
        }

        File[] itemFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (itemFiles == null || itemFiles.length == 0) {
            plugin.getLogger().warning(plugin.getMessagePrefix() + "items 폴더에 커스텀 아이템 파일이 없습니다.");
            return;
        }

        for (File file : itemFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                String id = config.getString("id");
                String materialName = config.getString("material");
                int customModelData = config.getInt("custom_model_data", -1);

                if (id == null || id.isEmpty()) {
                    plugin.getLogger().warning(plugin.getMessagePrefix() + "파일 " + file.getName() + "에서 'id'를 찾을 수 없거나 비어있습니다. 이 아이템은 건너뜁니다.");
                    continue;
                }

                if (loadedItems.containsKey(id)) {
                    plugin.getLogger().warning(plugin.getMessagePrefix() + "중복된 아이템 ID '" + id + "'가 발견되었습니다. 파일 " + file.getName() + "은 건너뜁니다.");
                    continue;
                }

                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    plugin.getLogger().warning(plugin.getMessagePrefix() + "파일 " + file.getName() + "에서 유효하지 않은 Material '" + materialName + "'입니다. 이 아이템은 건너뜁니다.");
                    continue;
                }

                if (customModelData < 0) {
                    plugin.getLogger().warning(plugin.getMessagePrefix() + "파일 " + file.getName() + "에서 'custom_model_data'가 유효하지 않습니다 (0 이상이어야 합니다). 이 아이템은 건너뜁니다.");
                    continue;
                }

                CustomItemData itemData = new CustomItemData(id, material, customModelData);

                // 선택적 속성 설정
                itemData.setDisplayName(config.getString("display_name"));
                itemData.setLore(config.getStringList("lore")); // getStringList는 리스트가 없으면 빈 리스트 반환
                itemData.setUnbreakable(config.getBoolean("unbreakable", false)); // 기본값 false
                itemData.setGlowing(config.getBoolean("glowing", false)); // 기본값 false

                loadedItems.put(id, itemData);
                if (plugin.isDebugMode()) {
                    plugin.getLogger().info(plugin.getMessagePrefix() + "아이템 로드 완료: " + itemData.getId() + " (" + file.getName() + ")");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, plugin.getMessagePrefix() + "파일 " + file.getName() + "을(를) 로드하는 중 오류 발생: " + e.getMessage(), e);
            }
        }
        plugin.getLogger().info(plugin.getMessagePrefix() + loadedItems.size() + "개의 커스텀 아이템 데이터를 로드했습니다.");
    }

    public CustomItemData getCustomData(String id) {
        return loadedItems.get(id);
    }

    public Set<String> getIds() {
        return loadedItems.keySet();
    }

    public ItemStack createItem(CustomItemData itemData) {
        if (itemData == null) {
            plugin.getLogger().warning(plugin.getMessagePrefix() + "생성할 CustomItemData가 null입니다.");
            return null;
        }

        ItemStack itemStack = new ItemStack(itemData.getMaterial());
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            plugin.getLogger().severe(plugin.getMessagePrefix() + "아이템 메타데이터를 가져올 수 없습니다: " + itemData.getId());
            return null;
        }

        if (itemData.getDisplayName() != null && !itemData.getDisplayName().isEmpty()) {
            meta.setItemName(itemData.getDisplayName());
        }

        if (itemData.getLore() != null && !itemData.getLore().isEmpty()) {
            List<String> coloredLore = itemData.getLore().stream()
                    .map(line -> plugin.getDefaultItemLoreColor() + line)
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
        }

        meta.setCustomModelData(itemData.getCustomModelData());

        meta.setUnbreakable(itemData.isUnbreakable());

        if (itemData.isGlowing()) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(customItemIdKey, PersistentDataType.STRING, itemData.getId());

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public boolean isCraftedGearItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.has(customItemIdKey, PersistentDataType.STRING);
    }

    public String getId(ItemStack itemStack) {
        if (!isCraftedGearItem(itemStack)) {
            return null;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.get(customItemIdKey, PersistentDataType.STRING);
    }
}

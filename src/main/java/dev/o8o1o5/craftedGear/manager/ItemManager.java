package dev.o8o1o5.craftedGear.manager;

import dev.o8o1o5.craftedGear.CraftedGear; // 메인 플러그인 임포트
import dev.o8o1o5.craftedGear.items.CustomItemData; // CustomItemData 클래스 임포트

import org.bukkit.Material; // Bukkit Material 임포트
import org.bukkit.configuration.file.YamlConfiguration; // YAML 파일 파싱을 위해

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; // keySet() 사용을 위해
import java.util.logging.Level;

// 아이템 스택 생성을 위한 추가 임포트
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;                   // PDC에 사용할 NamespacedKey 임포트
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer; // PDC 임포트
import org.bukkit.persistence.PersistentDataType;    // PDC 데이터 타입 임포트
import net.md_5.bungee.api.ChatColor;              // 색상 코드 변환을 위해
import java.util.stream.Collectors; // List 조작을 위해

/**
 * CraftedGear 플러그인의 커스텀 아이템 데이터를 관리하고 로드하는 클래스입니다.
 * 또한, 로드된 데이터를 기반으로 실제 ItemStack를 생성하는 역할도 수행합니다.
 */
public class ItemManager {

    private final CraftedGear plugin; // 메인 플러그인 인스턴스 (의존성 주입)
    private final Map<String, CustomItemData> loadedItems; // 아이템 ID를 키로 CustomItemData 객체를 저장
    private final NamespacedKey customItemIdKey; // 아이템의 고유 ID를 저장할 NamespacedKey

    public ItemManager(CraftedGear plugin) {
        this.plugin = plugin;
        this.loadedItems = new HashMap<>(); // 맵 초기화
        this.customItemIdKey = new NamespacedKey(plugin, "custom_item_id"); // NamespacedKey 초기화
    }

    /**
     * plugins/CraftedGear/items 폴더의 모든 YAML 파일을 읽어 커스텀 아이템 데이터를 로드합니다.
     */
    public void loadItems() {
        loadedItems.clear(); // 기존에 로드된 아이템이 있다면 모두 지웁니다 (리로드 시 유용)
        plugin.getLogger().info(plugin.getMessagePrefix() + "커스텀 아이템 데이터를 로드 중...");

        File itemsFolder = new File(plugin.getDataFolder(), "items"); // plugins/CraftedGear/items 폴더 참조
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs(); // 폴더가 없으면 생성
            plugin.getLogger().info(plugin.getMessagePrefix() + "items 폴더가 생성되었습니다. 아이템 파일을 넣어주세요.");
            return;
        }

        File[] itemFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml")); // .yml 파일만 필터링
        if (itemFiles == null || itemFiles.length == 0) {
            plugin.getLogger().warning(plugin.getMessagePrefix() + "items 폴더에 커스텀 아이템 파일이 없습니다.");
            return;
        }

        for (File file : itemFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                // YAML 파일에서 필수 속성 추출 및 유효성 검사
                String id = config.getString("id");
                String materialName = config.getString("material");

                if (id == null || id.isEmpty()) {
                    plugin.getLogger().warning(plugin.getMessagePrefix() + "파일 " + file.getName() + "에서 'id'를 찾을 수 없거나 비어 있습니다. 이 아이템은 건너뜁니다.");
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

                // CustomItemData 객체 생성
                CustomItemData itemData = new CustomItemData(id, material);

                // 선택적 속성 설정
                itemData.setDisplayName(config.getString("display_name"));
                itemData.setLore(config.getStringList("lore")); // getStringList는 리스트가 없으면 빈 리스트 반환
                itemData.setUnbreakable(config.getBoolean("unbreakable", false)); // 기본값 false
                itemData.setGlowing(config.getBoolean("glowing", false)); // 기본값 false

                // 로드된 아이템을 맵에 추가
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

    /**
     * 특정 ID를 가진 CustomItemData 객체를 반환합니다.
     * @param id 조회할 아이템의 고유 ID
     * @return 해당 CustomItemData 객체, 없으면 null
     */
    public CustomItemData getCustomData(String id) {
        return loadedItems.get(id);
    }

    /**
     * 로드된 모든 커스텀 아이템 ID 목록을 반환합니다.
     * @return 아이템 ID 목록
     */
    public Set<String> getIds() {
        return loadedItems.keySet();
    }

    /**
     * CustomItemData 객체를 기반으로 실제 CraftedGear 커스텀 아이템 ItemStack를 생성합니다.
     * 이 메서드는 NBTAPI를 사용하지 않고 Bukkit의 PDC(Persistent Data Container)만을 사용합니다.
     *
     * @param itemData 생성할 커스텀 아이템 데이터
     * @return 생성된 커스텀 아이템 ItemStack
     */
    public ItemStack createItem(CustomItemData itemData) {
        if (itemData == null) {
            plugin.getLogger().warning(plugin.getMessagePrefix() + "생성할 CustomItemData가 null입니다.");
            return null;
        }

        ItemStack itemStack = new ItemStack(itemData.getMaterial());
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) { // ItemMeta가 null일 수 있는 경우 대비
            plugin.getLogger().severe(plugin.getMessagePrefix() + "아이템 메타데이터를 가져올 수 없습니다: " + itemData.getId());
            return null;
        }

        // 1. 표시 이름 설정 (기본적으로 흰색)
        if (itemData.getDisplayName() != null && !itemData.getDisplayName().isEmpty()) {
            meta.setDisplayName(ChatColor.WHITE + itemData.getDisplayName());
        }

        // 2. 로어(Lore) 설정
        if (itemData.getLore() != null && !itemData.getLore().isEmpty()) {
            List<String> translatedLore = itemData.getLore().stream()
                    .map(line -> plugin.getDefaultLoreColor() + line)
                    .collect(Collectors.toList());
            meta.setLore(translatedLore);
        }

        // 3. Custom Model Data 설정
        CustomModelDataComponent customModelDataComponent = meta.getCustomModelDataComponent();
        customModelDataComponent.setStrings(List.of(itemData.getId()));
        meta.setCustomModelDataComponent(customModelDataComponent);

        // 4. 파괴 불가 설정
        meta.setUnbreakable(itemData.isUnbreakable());

        // 5. 발광 효과 설정 (인챈트 숨김 ItemFlag 사용)
        if (itemData.isGlowing()) {
            // meta.addEnchant();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // --- 핵심: Persistent Data Container (PDC)에 고유 ID 저장 ---
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(customItemIdKey, PersistentDataType.STRING, itemData.getId());

        itemStack.setItemMeta(meta); // 변경된 ItemMeta 적용

        return itemStack;
    }

    /**
     * 주어진 ItemStack가 CraftedGear 커스텀 아이템인지 확인합니다.
     *
     * @param itemStack 확인할 ItemStack
     * @return CraftedGear 커스텀 아이템이면 true, 아니면 false
     */
    public boolean isCraftedGearItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.has(customItemIdKey, PersistentDataType.STRING);
    }

    /**
     * 주어진 CraftedGear 커스텀 아이템의 고유 ID를 반환합니다.
     *
     * @param itemStack ID를 조회할 CraftedGear 아이템 ItemStack
     * @return 아이템의 고유 ID, CraftedGear 아이템이 아니면 null
     */
    public String getId(ItemStack itemStack) { // API 가이드에 따라 'get' 대신 'getId'로 변경
        if (!isCraftedGearItem(itemStack)) {
            return null;
        }
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.get(customItemIdKey, PersistentDataType.STRING);
    }
}
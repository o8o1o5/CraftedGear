package dev.o8o1o5.craftedGear.items;

import org.bukkit.Material; // Bukkit Material enum 임포트
import java.util.List;
import java.util.Objects; // Objects.requireNonNull() 사용을 위해

/**
 * CraftedGear 플러그인의 커스텀 아이템 데이터를 나타내는 클래스입니다.
 * 이 클래스는 YAML 설정 파일에서 아이템 정보를 로드하는 데 사용됩니다.
 */
public class CustomItemData {

    // 필수 속성
    private final String id; // 플러그인 내에서 아이템을 식별하는 고유 ID (예: legendary_sword_01)
    private final Material material; // 아이템의 기반이 되는 바닐라 마인크래프트 Material (예: DIAMOND_SWORD)

    // 선택적 속성 (null 또는 기본값 가능)
    private String displayName; // 아이템의 표시 이름 (색상 코드 지원)
    private List<String> lore; // 아이템의 설명 줄 (여러 줄, 색상 코드 지원)
    private boolean unbreakable; // 아이템 파괴 불가 여부
    private boolean glowing; // 아이템에 발광 효과 적용 여부

    /**
     * CustomItemData 객체의 생성자입니다.
     * @param id 아이템의 고유 ID
     * @param material 아이템의 기반 Material
     */
    public CustomItemData(String id, Material material) {
        // 필수 속성은 null이 될 수 없도록 검증합니다.
        this.id = Objects.requireNonNull(id, "Item ID cannot be null.");
        this.material = Objects.requireNonNull(material, "Item Material cannot be null.");
    }

    // --- Getter 메서드 ---
    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public boolean isGlowing() {
        return glowing;
    }

    // --- Setter 메서드 (선택적 속성만) ---
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    // 디버깅 및 로깅을 위한 toString() 메서드 오버라이드
    @Override
    public String toString() {
        return "CustomItemData{" +
                "id='" + id + '\'' +
                ", material=" + material +
                ", customModelData=" + id +
                ", displayName='" + displayName + '\'' +
                ", lore=" + lore +
                ", unbreakable=" + unbreakable +
                ", glowing=" + glowing +
                '}';
    }
}
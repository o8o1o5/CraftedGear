package dev.o8o1o5.craftedGear.items;

import org.bukkit.Material;

import java.util.List;
import java.util.Objects;

public class CustomItemData {

    private final String id;
    private final Material material;
    private final int customModelData;

    private String displayName;
    private List<String> lore;
    private boolean unbreakable;
    private boolean glowing;

    public CustomItemData(String id, Material material, int customModelData) {
        this.id = Objects.requireNonNull(id, "Item ID cannot be null.");
        this.material = Objects.requireNonNull(material, "Item Material cannot be null.");
        this.customModelData = customModelData;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
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

    @Override
    public String toString() {
        return "CustomItemData{" +
                "id='" + id + '\'' +
                ", material=" + material +
                ", customModelData=" + customModelData +
                ", displayName='" + displayName + '\'' +
                ", lore=" + lore +
                ", unbreakable=" + unbreakable +
                ", glowing=" + glowing +
                '}';
    }
}

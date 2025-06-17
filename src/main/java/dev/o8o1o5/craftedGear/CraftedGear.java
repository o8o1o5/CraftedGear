package dev.o8o1o5.craftedGear;

import dev.o8o1o5.craftedGear.commands.CraftedGearCommand;
import dev.o8o1o5.craftedGear.listeners.ResourcePackListener;
import dev.o8o1o5.craftedGear.manager.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class CraftedGear extends JavaPlugin {

    private static CraftedGear instance;

    private ItemManager itemManager;

    private boolean debugMode;
    private String resourcePackUrl;
    private String resourcePackHash;
    private byte[] resourcePackHashBytes;
    private String messagePrefix;
    private String defaultItemLoreColor;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("CraftedGear 플러그인이 활성화되고 있습니다...");

        // config.yml 파일이 없으면 생성하고, 있으면 로드합니다.
        saveDefaultConfig();
        // 이미 존재하는 config.yml을 강제로 다시 로드하려면 relaodConfig();를 사용합니다.

        // config.yml에서 설정 값들을 읽어옵니다.
        loadConfigValues();

        this.itemManager = new ItemManager(this);
        this.itemManager.loadItems();

        getServer().getPluginManager().registerEvents(new ResourcePackListener(this), this);

        if (getServer().isResourcePackRequired()) {
            for (Player player : getServer().getOnlinePlayers()) {
                player.setResourcePack(resourcePackUrl, resourcePackHashBytes,
                        "CraftedGear 커스텀 아이템을 위한 리소스팩을 다운로드 중...", false);
                if (debugMode) {
                    getLogger().info(messagePrefix + player.getName() + " 에게 플러그인 활성화 시 리소스팩 요청을 보냈습니다.");
                }
            }
        } else {
            getLogger().warning(messagePrefix + "config.yml에 유효한 리소스팩 URL 또는 해시가 설정되지 않았습니다. 커스텀 아이템 모델이 표시되지 않을 수 있습니다.");
        }

        Objects.requireNonNull(getCommand("cg")).setExecutor(new CraftedGearCommand(this));

        getLogger().info(messagePrefix + "플러그인 활성화 완료!");
        if (debugMode) {
            getLogger().info(messagePrefix + "디버그 모드가 활성화되었습니다! 디버그 모드를 해제하려면 config.yml를 참고해주세요.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(messagePrefix + "플러그인이 비활성화되었습니다!");
    }

    public static CraftedGear getInstance() {
        return instance;
    }

    private void loadConfigValues() {
        this.debugMode = getConfig().getBoolean("debug_mode", false);
        this.resourcePackUrl = getConfig().getString("resource_pack_url", "");
        this.resourcePackHash = getConfig().getString("resource_pack_hash", "");
        this.messagePrefix = Objects.requireNonNull(getConfig().getString("message_prefix", "&8[&aCraftedGear&8] &r"));
        this.defaultItemLoreColor = Objects.requireNonNull(getConfig().getString("default_item_lore_color", "&7"));

        if (debugMode) {
            getLogger().info("Config Loaded:");
            getLogger().info("  debug_mode: " + debugMode);
            getLogger().info("  resource_pack_url: " + resourcePackUrl);
            getLogger().info("  resource_pack_hash: " + resourcePackHash);
            getLogger().info("  message_prefix: " + messagePrefix);
            getLogger().info("  default_item_lore_color: " + defaultItemLoreColor);
        }

        if (!resourcePackUrl.isEmpty() && !resourcePackHash.isEmpty()) {
            try {
                this.resourcePackHashBytes = hexStringToByteArray(resourcePackHash);
                if (this.resourcePackHashBytes == null || this.resourcePackHashBytes.length == 0) {
                    getLogger().warning(messagePrefix + "제공된 리소스팩 해시 문자열이 유효하지 않습니다. 리소스팩이 적용되지 않을 수 있습니다.");
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "리소스팩 해시를 변환하는 중 오류 발생: " + e.getMessage());
                this.resourcePackUrl = null;
            }
        } else {
            getLogger().warning(messagePrefix + "config.yml에 resource_pack_url 또는 resource_pack_hash가 설정되지 않아 리소스팩이 자동으로 적용되지 않습니다.");
            this.resourcePackHashBytes = null;
        }
    }

    public boolean isResourcePackConfigured() {
        return !resourcePackUrl.isEmpty() && resourcePackHashBytes != null && resourcePackHashBytes.length > 0;
    }


    public byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        hex = hex.replace(" ", "");
        int len = hex.length();
        if (len % 2 != 0) {
            getLogger().warning("유효하지 않은 헥스 문자열 길이 (홀수): " + hex);
            return new byte[0];
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i+= 2) {
            try {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i + 1), 16));
            } catch (NumberFormatException e) {
                getLogger().warning("유효하지 않은 헥스 문자가 포함된 문자열: " + hex);
                return new byte[0];
            }
        }
        return data;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getDefaultItemLoreColor() {
        return defaultItemLoreColor;
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public byte[] getResourcePackHashBytes() {
        return resourcePackHashBytes;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
}

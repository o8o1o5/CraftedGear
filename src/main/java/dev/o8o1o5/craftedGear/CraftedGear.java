package dev.o8o1o5.craftedGear;

import dev.o8o1o5.craftedGear.commands.CraftedGearCommand;
import dev.o8o1o5.craftedGear.managers.ItemManager;
import dev.o8o1o5.craftedGear.listeners.ResourcePackListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
// import javax.xml.bind.DatatypeConverter; // Java 8 또는 필요 시 사용 (의존성 추가 필요)

/**
 * CraftedGear 플러그인의 메인 클래스입니다.
 * 서버 활성화/비활성화 시 로직을 처리하고, 핵심 관리자 및 리스너를 초기화합니다.
 */
public final class CraftedGear extends JavaPlugin {

    private static CraftedGear plugin;
    private ItemManager itemManager;
    private String messagePrefix;
    private String defaultLoreColor;
    private boolean debugMode;

    /**
     * 플러그인 활성화 시 호출됩니다.
     * 설정 로드, 관리자 초기화, 명령어 및 이벤트 리스너 등록을 처리합니다.
     */
    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("CraftedGear 플러그인 활성화 중...");

        saveDefaultConfig();
        reloadConfig();

        loadConfigSettings(); // config.yml에서 설정 값들을 로드

        itemManager = new ItemManager(this);
        itemManager.loadItems(); // 커스텀 아이템 로드

        getCommand("cg").setExecutor(new CraftedGearCommand(this));

        // 리소스팩 적용을 위한 리스너 등록 (플레이어 접속 시 리소스팩 요청)
        Bukkit.getPluginManager().registerEvents(new ResourcePackListener(this), this);

        getLogger().info("CraftedGear 플러그인 활성화 완료.");
    }

    /**
     * 플러그인 비활성화 시 호출됩니다.
     * 리소스 정리 및 플러그인 인스턴스를 해제합니다.
     */
    @Override
    public void onDisable() {
        getLogger().info("CraftedGear 플러그인 비활성화 중...");
        plugin = null;
        getLogger().info("CraftedGear 플러그인 비활성화 완료.");
    }

    /**
     * config.yml 파일에서 플러그인 설정 값들을 로드합니다.
     * 특히 리소스팩 해시 문자열을 byte[]로 변환하는 로직을 포함합니다.
     */
    private void loadConfigSettings() {
        this.debugMode = getConfig().getBoolean("debug_mode", false);

        this.messagePrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message_prefix", "&8[&aCraftedGear&8] &r"));
        String loreColorCode = getConfig().getString("default_item_lore_color", "&7");
        // ChatColor.valueOf()는 enum 이름을 기대하므로, translateAlternateColorCodes 결과의 name()을 사용
        this.defaultLoreColor = ChatColor.translateAlternateColorCodes('&', loreColorCode);

        if (debugMode) {
            getLogger().info("디버그 모드 활성화됨.");
        }
    }

    /**
     * 16진수 문자열을 바이트 배열로 변환합니다.
     * @param s 16진수 문자열 (예: "c668b14ed66afb679bd3277e672f72252817a39b")
     * @return 변환된 바이트 배열
     * @throws IllegalArgumentException 문자열이 올바른 16진수 형식이 아닐 경우
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len % 2 != 0) { // 길이가 홀수면 유효하지 않음
            throw new IllegalArgumentException("16진수 문자열의 길이가 홀수입니다.");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * 플러그인 인스턴스를 반환합니다.
     * @return CraftedGear 플러그인 인스턴스
     */
    public static CraftedGear getPlugin() {
        return plugin;
    }

    /**
     * ItemManager 인스턴스를 반환합니다.
     * @return ItemManager 인스턴스
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * 플러그인 메시지 접두사를 반환합니다.
     * @return 메시지 접두사 문자열
     */
    public String getMessagePrefix() {
        return messagePrefix;
    }

    /**
     * 기본 로어 색상을 반환합니다.
     * @return 기본 로어 색상 ChatColor
     */
    public String getDefaultLoreColor() {
        return defaultLoreColor;
    }

    /**
     * 디버그 모드 활성화 여부를 반환합니다.
     * @return 디버그 모드 활성화 여부
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}
package dev.o8o1o5.craftedGear;

import dev.o8o1o5.craftedGear.commands.CraftedGearCommand;
import dev.o8o1o5.craftedGear.generators.ResourcePackGenerator;
import dev.o8o1o5.craftedGear.managers.ItemManager;
import dev.o8o1o5.craftedGear.listeners.ResourcePackListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File; // File 클래스 임포트
import java.io.IOException; // IOException 임포트
import java.security.NoSuchAlgorithmException; // NoSuchAlgorithmException 임포트

/**
 * CraftedGear 플러그인의 메인 클래스입니다.
 * 서버 활성화/비활성화 시 로직을 처리하고, 핵심 관리자 및 리스너를 초기화합니다.
 */
public final class CraftedGear extends JavaPlugin {

    private static CraftedGear plugin;
    private ItemManager itemManager;
    private ResourcePackGenerator resourcePackGenerator; // ResourcePackGenerator 필드 추가

    private String messagePrefix;
    private String defaultLoreColor;
    private boolean debugMode;

    // 리소스팩 관련 필드
    private String resourcePackDownloadUrl; // config.yml에서 로드할 URL
    private String generatedResourcePackHash; // 생성 후 저장할 SHA-1 해시

    /**
     * 플러그인 활성화 시 호출됩니다.
     * 설정 로드, 관리자 초기화, 명령어 및 이벤트 리스너 등록을 처리합니다.
     */
    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("CraftedGear 플러그인 활성화 중...");

        saveDefaultConfig(); // config.yml이 없으면 기본값으로 생성
        reloadConfig(); // 변경사항이 있다면 다시 로드

        loadConfigSettings(); // config.yml에서 설정 값들을 로드

        itemManager = new ItemManager(this);
        itemManager.loadItems(); // 커스텀 아이템 로드

        // ResourcePackGenerator 초기화 시 ItemManager 주입
        this.resourcePackGenerator = new ResourcePackGenerator(this, itemManager);

        try {
            // 리소스팩 생성 및 해시 계산
            File generatedZipFile = resourcePackGenerator.generateAndZipResourcePack();
            this.generatedResourcePackHash = resourcePackGenerator.calculateSha1(generatedZipFile);

            getLogger().info(messagePrefix + "리소스팩 생성이 완료되었습니다: " + generatedZipFile.getName());
            getLogger().info(messagePrefix + "리소스팩 다운로드 URL: " + resourcePackDownloadUrl);
            getLogger().info(messagePrefix + "리소스팩 SHA1 해시: " + generatedResourcePackHash);

            // TODO: 중요!
            // 1. resource-pack.url을 실제 웹 서버에 리소스팩 (crafted_gear_rp.zip)을 업로드한 URL로 변경해야 합니다.
            // 2. 플레이어에게 리소스팩을 적용하려면 ResourcePackListener에서 이 URL과 해시를 사용해야 합니다.
            //    server.properties에 resource-pack: [URL] 및 resource-pack-sha1: [HASH]를 수동으로 설정할 수도 있습니다.

        } catch (IOException | NoSuchAlgorithmException e) {
            getLogger().severe(messagePrefix + "리소스팩 생성 중 치명적인 오류 발생: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this); // 오류 발생 시 플러그인 비활성화
            return; // 추가 실행 방지
        }

        // 명령어 등록
        getCommand("cg").setExecutor(new CraftedGearCommand(this));

        // 리소스팩 적용을 위한 리스너 등록 (플레이어 접속 시 리소스팩 요청)
        // 이 리스너는 getResourcePackUrl() 및 getResourcePackHash()를 사용하여 리소스팩을 적용합니다.
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
     * 리소스팩 URL 로직을 포함합니다.
     */
    private void loadConfigSettings() {
        this.debugMode = getConfig().getBoolean("debug_mode", false);

        this.messagePrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message_prefix", "&8[&aCraftedGear&8] &r"));
        String loreColorCode = getConfig().getString("default_item_lore_color", "&7");
        // ChatColor.valueOf()는 enum 이름을 기대하므로, translateAlternateColorCodes 결과의 name()을 사용
        this.defaultLoreColor = ChatColor.translateAlternateColorCodes('&', loreColorCode);

        // 리소스팩 다운로드 URL 로드
        // 사용자가 config.yml에 설정하지 않으면 기본값을 사용합니다.
        // 실제 운영 시에는 반드시 이 값을 실제 리소스팩이 호스팅되는 URL로 변경해야 합니다.
        this.resourcePackDownloadUrl = getConfig().getString("resource-pack.url", "http://your_web_server.com/crafted_gear_rp.zip");


        if (debugMode) {
            getLogger().info("디버그 모드 활성화됨.");
            getLogger().info("메시지 접두사: " + messagePrefix + ChatColor.RESET); // ChatColor.RESET 추가하여 다음 로그에 영향 안 주도록
            getLogger().info("기본 로어 색상 코드: " + loreColorCode + "(예시) " + defaultLoreColor + "Test Lore" + ChatColor.RESET);
            getLogger().info("리소스팩 URL 설정: " + resourcePackDownloadUrl);
        }
    }

    // hexStringToByteArray 메서드는 리소스팩 해시 계산에서 직접 사용하지 않으므로 필요 없습니다.
    // ResourcePackGenerator.calculateSha1()이 바이트 배열이 아닌 16진수 문자열로 반환합니다.
    /*
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("16진수 문자열의 길이가 홀수입니다.");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    */

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

    /**
     * 리소스팩 다운로드 URL을 반환합니다.
     * @return 리소스팩 다운로드 URL
     */
    public String getResourcePackUrl() {
        return resourcePackDownloadUrl;
    }

    /**
     * 생성된 리소스팩의 SHA-1 해시를 반환합니다.
     * @return 리소스팩 SHA-1 해시 문자열
     */
    public String getGeneratedResourcePackHash() {
        return generatedResourcePackHash;
    }
}
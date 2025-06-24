package dev.o8o1o5.craftedGear.listeners;

import dev.o8o1o5.craftedGear.CraftedGear; // CraftedGear 메인 클래스 임포트

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackListener implements Listener {

    private final CraftedGear plugin; // 메인 플러그인 인스턴스 (주입된 의존성)

    public ResourcePackListener(CraftedGear plugin) {
        this.plugin = plugin;
    }

    // 플레이어가 서버에 접속했을 때 호출되는 이벤트 핸들러
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // CraftedGear 메인 클래스에서 설정된 리소스팩 정보를 가져옵니다.

    }
}
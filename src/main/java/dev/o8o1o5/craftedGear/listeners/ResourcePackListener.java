package dev.o8o1o5.craftedGear.listeners;

import dev.o8o1o5.craftedGear.CraftedGear;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackListener implements Listener {

    private final CraftedGear plugin;

    public ResourcePackListener(CraftedGear plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String resourcePackUrl = plugin.getResourcePackUrl();
        byte[] resourcePackHashBytes = plugin.getResourcePackHashBytes();

        if (plugin.isResourcePackConfigured()) {
            player.setResourcePack(resourcePackUrl, resourcePackHashBytes,
                    "CraftedGear 커스텀 아이템을 위한 리소스팩을 다운로드 중...", false);

            if (plugin.isDebugMode()) {
                plugin.getLogger().info(plugin.getMessagePrefix() + player.getName() + " 에게 리소스팩 요청을 보냈습니다.");
            }
        } else if (plugin.isDebugMode()) {
            plugin.getLogger().warning(plugin.getMessagePrefix() + player.getName() + " 에게 보낼 유요한 리소스팩 URL 또는 해시가 없습니다.");
        }
    }
}

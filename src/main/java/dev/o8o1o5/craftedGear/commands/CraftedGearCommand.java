package dev.o8o1o5.craftedGear.commands;

import dev.o8o1o5.craftedGear.CraftedGear;
import dev.o8o1o5.craftedGear.items.CustomItemData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class CraftedGearCommand implements CommandExecutor {

    private final CraftedGear plugin;

    public CraftedGearCommand(CraftedGear plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("craftedgear.command.admin")) {
            sender.sendMessage(plugin.getMessagePrefix() + "§c권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                return handleGiveCommand(sender, args);
            case "info":
                // TODO: 여기에 info 명령어를 처리할 메서드 호출 예정
                sender.sendMessage(plugin.getMessagePrefix() + "§7'/cg info' 명령어는 아직 구현되지 않았습니다.");
                return true;
            case "reload":
                // TODO: 여기에 reload 명령어를 처리할 메서드 호출 예정
                sender.sendMessage(plugin.getMessagePrefix() + "§7'/cg reload' 명령어는 아직 구현되지 않았습니다.");
                return true;
            default:
                sender.sendMessage(plugin.getMessagePrefix() + "§c알 수 없는 명령어입니다. 사용법: /cg help");
                sendHelpMessage(sender); // 알 수 없는 명령어도 도움말 메시지 전송
                return true;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        // /cg give <플레이어> <아이템_ID> [수량]
        if (args.length < 3) {
            sender.sendMessage(plugin.getMessagePrefix() + "§c사용법: /cg give <플레이어> <아이템_ID> [수량]");
            return true;
        }

        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getMessagePrefix() + "§c플레이어 '" + targetPlayerName + "'를 찾을 수 없습니다.");
            return true;
        }

        String itemId = args[2];
        CustomItemData itemData = plugin.getItemManager().getCustomData(itemId);

        if (itemData == null) {
            sender.sendMessage(plugin.getMessagePrefix() + "§c아이템 ID '" + itemId + "'는 존재하지 않습니다.");
            String availableItems = plugin.getItemManager().getIds().stream()
                    .collect(Collectors.joining(", "));
            sender.sendMessage(plugin.getMessagePrefix() + "§7사용 가능한 아이템: " + availableItems);
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) {
                    sender.sendMessage(plugin.getMessagePrefix() + "§c수량은 1 이상이어야 합니다.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessagePrefix() + "§c유효하지 않은 수량입니다. 숫자를 입력해주세요.");
                return true;
            }
        }

        // ItemManager를 통해 아이템 생성
        ItemStack customItem = plugin.getItemManager().createItem(itemData);
        if (customItem == null) {
            sender.sendMessage(plugin.getMessagePrefix() + "§c아이템 생성 중 알 수 없는 오류가 발생했습니다.");
            return true;
        }

        customItem.setAmount(amount);
        targetPlayer.getInventory().addItem(customItem);

        sender.sendMessage(plugin.getMessagePrefix() + "§a" + targetPlayer.getName() + "님에게 '" + itemId + "' 아이템 " + amount + "개를 지급했습니다.");
        if (sender instanceof Player && !sender.equals(targetPlayer)) {
            targetPlayer.sendMessage(plugin.getMessagePrefix() + "§a" + sender.getName() + "님이 당신에게 CraftedGear 아이템을 지급했습니다.");
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.getMessagePrefix() + "§6--- CraftedGear 명령어 도움말 ---");
        sender.sendMessage(plugin.getMessagePrefix() + "§e/cg give <플레이어> <아이템_ID> [수량] §7- 커스텀 아이템을 지급합니다.");
        sender.sendMessage(plugin.getMessagePrefix() + "§e/cg info [아이템_ID] §7- 커스텀 아이템 정보를 확인합니다. (미구현)");
        sender.sendMessage(plugin.getMessagePrefix() + "§e/cg reload §7- 플러그인 설정을 리로드합니다. (미구현)");
        sender.sendMessage(plugin.getMessagePrefix() + "§6---------------------------------");
    }
}

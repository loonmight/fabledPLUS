package studio.loonmight.emeraldglow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Location;

public class GlowWhileOnEmerald implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();

        if (blockBelow.getType() == Material.EMERALD_BLOCK) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
        }
    }
}

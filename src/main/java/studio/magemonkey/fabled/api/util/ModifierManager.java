package studio.magemonkey.fabled.api.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages temporary stat modifiers for entities (Flag-style expiry)
 */
public class ModifierManager {
    public static final Map<Integer, ModifierData> data = new HashMap<>();

    public static ModifierData getModifierData(LivingEntity entity) {
        return getModifierData(entity, true);
    }

    public static ModifierData getModifierData(LivingEntity entity, boolean create) {
        if (entity == null) return null;
        if (!data.containsKey(entity.getEntityId()) && create) {
            data.put(entity.getEntityId(), new ModifierData(entity));
        }
        return data.get(entity.getEntityId());
    }

    public static void addModifier(LivingEntity entity, String stat, double amount, int ticks, String skillId) {
        ModifierData modData = getModifierData(entity);
        if (modData != null) modData.addModifier(stat, amount, ticks, skillId);
    }

    public static void removeModifier(LivingEntity entity, String stat, String skillId) {
        ModifierData modData = getModifierData(entity, false);
        if (modData != null) modData.removeModifier(stat, skillId);
    }

	public static double getTotalModifier(LivingEntity entity, String stat) {
		ModifierData modData = getModifierData(entity, false);
		return modData != null ? modData.getTotal(stat) : 0.0; // default is 0 now
	}

    public static long getMillisLeft(LivingEntity entity, String stat, String skillId) {
        ModifierData modData = getModifierData(entity, false);
        if (modData == null) return 0;
        ModifierData.ModifierRecord record = modData.getRecord(stat, skillId);
        return record != null ? record.getMillisLeft() : 0;
    }

    public static void clearModifiers(LivingEntity entity) {
        if (entity == null) return;
        ModifierData removed = data.remove(entity.getEntityId());
        if (removed != null) removed.clear();
    }

    public static Map<String, Map<String, ModifierData.ModifierRecord>> getAllModifiers(LivingEntity entity) {
        ModifierData modData = getModifierData(entity, false);
        return modData != null ? modData.getAll() : new HashMap<>();
    }

    // ---------------- Scheduled Cleanup Task ----------------

    /**
     * Starts automatic cleanup of expired modifiers.
     * Call this from your plugin's onEnable().
     */
	public static void startCleanup(JavaPlugin plugin) {
		new BukkitRunnable() {
			@Override
			public void run() {
				data.values().forEach(modData -> {
					modData.modifiers.forEach((stat, bySkill) -> {
						bySkill.forEach((skillId, record) -> {
							if (record.isExpired()) {
								String entityName = (modData.entity instanceof Player)
										? ((Player) modData.entity).getName()
										: modData.entity.getType().name();

								String message = ChatColor.RED + "[ModifierManager] Expired modifier removed: "
										+ ChatColor.AQUA + entityName
										+ ChatColor.GRAY + " | Stat: " + ChatColor.GOLD + stat
										+ ChatColor.GRAY + " | Skill: " + ChatColor.AQUA + skillId
										+ ChatColor.GRAY + " | Amount: " + ChatColor.GREEN + record.amount;

								// Send to all online players
								Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
							}
						});
					});

					// Now remove expired entries
					modData.getAll();
				});

				// Remove empty ModifierData entries
				data.entrySet().removeIf(entry -> entry.getValue().getAll().isEmpty());
			}
		}.runTaskTimer(plugin, 20L, 20L); // every 1 second
	}

    // ---------------- ModifierData Class ----------------

    public static class ModifierData {
        private final LivingEntity entity;
        private final Map<String, Map<String, ModifierRecord>> modifiers = new HashMap<>();

        public ModifierData(LivingEntity entity) {
            this.entity = entity;
        }

        public void addModifier(String stat, double amount, int ticks, String skillId) {
            Map<String, ModifierRecord> bySkill = modifiers.computeIfAbsent(stat, k -> new HashMap<>());
            bySkill.put(skillId, new ModifierRecord(amount, ticks));
        }

        public void removeModifier(String stat, String skillId) {
            Map<String, ModifierRecord> bySkill = modifiers.get(stat);
            if (bySkill != null) {
                bySkill.remove(skillId);
                if (bySkill.isEmpty()) modifiers.remove(stat);
            }
        }

		public double getTotal(String stat) {
			Map<String, ModifierRecord> bySkill = modifiers.get(stat);
			if (bySkill == null) return 0.0; // no modifiers, default sum is 0

			bySkill.values().removeIf(ModifierRecord::isExpired);
			if (bySkill.isEmpty()) {
				modifiers.remove(stat);
				return 0.0;
			}

			// Return only the sum of modifier amounts (no +1.0 here anymore)
			return bySkill.values().stream().mapToDouble(r -> r.amount).sum();
		}

        public ModifierRecord getRecord(String stat, String skillId) {
            Map<String, ModifierRecord> bySkill = modifiers.get(stat);
            return bySkill != null ? bySkill.get(skillId) : null;
        }

        public void clear() {
            modifiers.clear();
        }

        public Map<String, Map<String, ModifierRecord>> getAll() {
            // Remove expired modifiers
            modifiers.forEach((stat, bySkill) -> bySkill.values().removeIf(ModifierRecord::isExpired));
            modifiers.entrySet().removeIf(e -> e.getValue().isEmpty());
            return modifiers;
        }

        public static class ModifierRecord {
            private final double amount;
            private final int ticks; // in server ticks
            private final long startTime;

            public ModifierRecord(double amount, int ticks) {
                this.amount = amount;
                this.ticks = ticks;
                this.startTime = System.currentTimeMillis();
            }

            public boolean isExpired() {
                return getMillisLeft() <= 0;
            }

            public long getMillisLeft() {
                long elapsed = System.currentTimeMillis() - startTime;
                return Math.max(ticks * 50L - elapsed, 0);
            }
        }
    }

    // ---------------- DebugListener Class ----------------

    public static class DebugListener implements Listener {
        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            if (!event.getMessage().equalsIgnoreCase("checkmod")) return;

            event.setCancelled(true);

            if (ModifierManager.data.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No active modifiers on any entities.");
                return;
            }

            player.sendMessage(ChatColor.GREEN + "=== All Active Modifiers ===");

            for (ModifierManager.ModifierData modData : ModifierManager.data.values()) {
                LivingEntity entity = modData.entity;
                String name = (entity instanceof Player) ? ((Player) entity).getName() : entity.getType().name();

                Map<String, Map<String, ModifierManager.ModifierData.ModifierRecord>> allMods = modData.getAll();
                if (allMods.isEmpty()) continue;

                player.sendMessage(ChatColor.AQUA + "--- " + name + " ---");

                for (String stat : allMods.keySet()) {
                    Map<String, ModifierManager.ModifierData.ModifierRecord> bySkill = allMods.get(stat);

                    bySkill.forEach((skillId, record) -> {
                        long millisLeft = record.getMillisLeft();
                        player.sendMessage(ChatColor.YELLOW + stat + ChatColor.GRAY +
                                " | Skill: " + ChatColor.AQUA + skillId +
                                ChatColor.GRAY + " | Amount: " + ChatColor.GREEN + record.amount +
                                ChatColor.GRAY + " | Time Left: " + ChatColor.RED + millisLeft + "ms");
                    });

                    double total = ModifierManager.getTotalModifier(entity, stat);
                    player.sendMessage(ChatColor.GOLD + "Total " + stat + ": " + ChatColor.GREEN + total);
                }
            }
        }
    }
}
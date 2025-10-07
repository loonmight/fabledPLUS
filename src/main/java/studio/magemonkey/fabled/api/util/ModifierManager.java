package studio.magemonkey.fabled.api.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages temporary stat modifiers for entities (Flag-style expiry)
 */
public class ModifierManager {

    public static final Map<Integer, ModifierData> data = new HashMap<>();

    // ------------------- Core API -------------------

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

    public static void addModifier(LivingEntity entity, String stat, double amount, int ticks, String skillId, UUID source) {
        ModifierData modData = getModifierData(entity);
        if (modData != null) modData.addModifier(stat, amount, ticks, skillId, source);
    }

    public static void removeModifier(LivingEntity entity, String stat, String skillId) {
        ModifierData modData = getModifierData(entity, false);
        if (modData != null) modData.removeModifier(stat, skillId);
    }

    public static double getTotalModifier(LivingEntity entity, String stat) {
        ModifierData modData = getModifierData(entity, false);
        return modData != null ? modData.getTotal(stat) : 0.0;
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

    /**
     * Returns all active ModifierRecords for a stat as a list (for contribution tracking)
     */
    public static List<ModifierData.ModifierRecord> getModifierRecords(LivingEntity entity, String stat) {
        ModifierData modData = getModifierData(entity, false);
        if (modData == null) return Collections.emptyList();
        Map<String, ModifierData.ModifierRecord> bySkill = modData.getAll().get(stat);
        if (bySkill == null) return Collections.emptyList();
        return new ArrayList<>(bySkill.values());
    }

    // ------------------- Scheduled Cleanup -------------------

    public static void startCleanup(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                data.values().forEach(modData -> {
                    modData.getAll();
                });
                data.entrySet().removeIf(entry -> entry.getValue().getAll().isEmpty());
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // ------------------- ModifierData -------------------

    public static class ModifierData {
        private final LivingEntity entity;
        private final Map<String, Map<String, ModifierRecord>> modifiers = new HashMap<>();

        public ModifierData(LivingEntity entity) {
            this.entity = entity;
        }

        public void addModifier(String stat, double amount, int ticks, String skillId, UUID source) {
            Map<String, ModifierRecord> bySkill = modifiers.computeIfAbsent(stat, k -> new HashMap<>());
            bySkill.put(skillId, new ModifierRecord(amount, ticks, source, skillId));
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
            if (bySkill == null) return 0.0;
            bySkill.values().removeIf(ModifierRecord::isExpired);
            if (bySkill.isEmpty()) {
                modifiers.remove(stat);
                return 0.0;
            }
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
            modifiers.forEach((stat, bySkill) -> bySkill.values().removeIf(ModifierRecord::isExpired));
            modifiers.entrySet().removeIf(e -> e.getValue().isEmpty());
            return modifiers;
        }

        public static class ModifierRecord {
            private final double amount;
            private final int ticks;
            private final long startTime;
            private final UUID source;
            private final String skillId;

            public ModifierRecord(double amount, int ticks, UUID source, String skillId) {
                this.amount = amount;
                this.ticks = ticks;
                this.startTime = System.currentTimeMillis();
                this.source = source;
                this.skillId = skillId;
            }

            public boolean isExpired() {
                return getMillisLeft() <= 0;
            }

            public long getMillisLeft() {
                long elapsed = System.currentTimeMillis() - startTime;
                return Math.max(ticks * 50L - elapsed, 0);
            }

            public UUID getSource() {
                return source;
            }

            public double getAmount() {
                return amount;
            }

            public String getSkillId() {
                return skillId;
            }
        }
    }
}

package studio.magemonkey.fabled.dynamic.mechanic;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.event.CDmgDEvent;
import studio.magemonkey.fabled.api.event.CDmgREvent;
import studio.magemonkey.fabled.api.event.CDeathEvent;
import studio.magemonkey.fabled.api.event.CFinalHitEvent;
import studio.magemonkey.fabled.api.event.CKillEvent;
import studio.magemonkey.fabled.api.util.FlagManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CDmgMechanic extends MechanicComponent {

    private static final String AMOUNT     = "amount";
    private static final String AMOUNTTYPE = "amounttype";
    private static final String SKILLID    = "skillid";
    private static final String SKILLTYPE  = "skilltype";

    // --- CKill tracker: target UUID -> (attacker UUID -> HitInfo) ---
    private static final Map<UUID, Map<UUID, HitInfo>> ckills = new HashMap<>();

    private static class HitInfo {
        final LivingEntity attacker;
        final double damage;
        final String skillid;
        final String skilltype;
        final long timestamp;

        HitInfo(LivingEntity attacker, double damage, String skillid, String skilltype) {
            this.attacker = attacker;
            this.damage = damage;
            this.skillid = skillid;
            this.skilltype = skilltype;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @Override
    public String getKey() {
        return "CDmg";
    }

    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force,
                           String amountOverride,
                           String amountTypeOverride,
                           String skillIdOverride,
                           String skillTypeOverride) {

        if ((targets == null || targets.isEmpty()) && amountOverride == null && !settings.has(AMOUNT)) {
            return false;
        }

        for (LivingEntity target : targets) {
            // --- Resolve amount ---
            String amountStr = (amountOverride != null)
                    ? filter(caster, target, amountOverride)
                    : filter(caster, target, settings.getString(AMOUNT));

            double baseAmount;
            try {
                baseAmount = Math.max(0, Double.parseDouble(amountStr));
            } catch (NumberFormatException e) {
                continue;
            }

            // --- Resolve amount type ---
            String amountType = (amountTypeOverride != null)
                    ? amountTypeOverride
                    : settings.has(AMOUNTTYPE)
                        ? settings.getString(AMOUNTTYPE)
                        : "flat";

            // --- Resolve skill ID ---
            String skillId = (skillIdOverride != null)
                    ? filter(caster, target, skillIdOverride)
                    : settings.has(SKILLID)
                        ? filter(caster, target, settings.getString(SKILLID))
                        : "skillid";

            // --- Resolve skill type ---
            String skillType = (skillTypeOverride != null)
                    ? filter(caster, target, skillTypeOverride)
                    : settings.has(SKILLTYPE)
                        ? filter(caster, target, settings.getString(SKILLTYPE))
                        : "skilltype";

            // --- Calculate final damage based on amount type ---
            double finalDamage = 0;
            if (target != null) {
                switch (amountType) {
                    case "percentmax":
                        finalDamage = target.getMaxHealth() * baseAmount;
                        break;
                    case "percentcurrent":
                        finalDamage = target.getHealth() * baseAmount;
                        break;
                    case "percentmissing":
                        finalDamage = (target.getMaxHealth() - target.getHealth()) * baseAmount;
                        break;
                    default: // flat
                        finalDamage = baseAmount;
                        break;
                }
            } else {
                // If target is null, fallback to flat damage
                finalDamage = baseAmount;
            }

            double actualDamage;
            boolean lethal = target != null && finalDamage >= target.getHealth();

            // --- CKill tracking if both caster and target exist ---
            if (caster != null && target != null) {
                ckills.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>())
                      .put(caster.getUniqueId(), new HitInfo(caster, finalDamage, skillId, skillType));
            }

            // --- Apply health safely ---
            if (target != null) {
                if (lethal) {
                    actualDamage = target.getHealth();
                    target.setHealth(0.01); // leave target barely alive
                } else {
                    actualDamage = finalDamage;
                    target.setHealth(Math.max(0, target.getHealth() - actualDamage));
                }
            } else {
                // Target is null: still count damage
                actualDamage = finalDamage;
            }

            // --- Fire lethal events if applicable ---
            if (lethal && target != null && !FlagManager.hasFlag(target, "cannotdie")) {
                if (caster != null) {
                    Bukkit.getPluginManager().callEvent(
                            new CFinalHitEvent(caster, target, actualDamage, skillId, skillType)
                    );
                }
                Bukkit.getPluginManager().callEvent(
                        new CDeathEvent(target, caster, actualDamage, skillId, skillType)
                );

                // Fire CKillEvent for all recent attackers
                Map<UUID, HitInfo> targetMarks = ckills.get(target.getUniqueId());
                if (targetMarks != null) {
                    long now = System.currentTimeMillis();
                    targetMarks.entrySet().removeIf(entry -> {
                        HitInfo mark = entry.getValue();
                        if (now - mark.timestamp <= 5000) {
                            Bukkit.getPluginManager().callEvent(
                                    new CKillEvent(mark.attacker, target, mark.damage, mark.skillid, mark.skilltype)
                            );
                            return true; // remove after firing
                        }
                        return now - mark.timestamp > 5000; // remove expired marks
                    });
                    if (targetMarks.isEmpty()) {
                        ckills.remove(target.getUniqueId());
                    }
                }
            }

            // --- Fire standard damage events regardless of null ---
            Bukkit.getPluginManager().callEvent(
                    new CDmgDEvent(caster, target, actualDamage, skillId, skillType)
            );
            Bukkit.getPluginManager().callEvent(
                    new CDmgREvent(target, caster, actualDamage, skillId, skillType)
            );
        }

        return true;
    }

    // --- Convenience override using Double amount ---
    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force,
                           Double amountOverride,
                           String amountTypeOverride) {
        return execute(caster, level, targets, force,
                (amountOverride != null) ? String.valueOf(amountOverride) : null,
                amountTypeOverride,
                null,
                null);
    }

    // --- Default execution ---
    @Override
    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force) {
        return execute(caster, level, targets, force, null, null, null, null);
    }
}

package studio.magemonkey.fabled.dynamic.mechanic;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import studio.magemonkey.fabled.api.event.*;
import studio.magemonkey.fabled.api.util.FlagManager;
import studio.magemonkey.fabled.api.util.ModifierManager;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDmgMechanic extends MechanicComponent {

    private static final String AMOUNT = "amount";
    private static final String AMOUNTTYPE = "amounttype";
    private static final String SKILLID = "skillid";
    private static final String SKILLTYPE = "skilltype";

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

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets, boolean force) {
        return execute(caster, level, targets, force, null, null, null, null);
    }

    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets, boolean force,
                           String amountOverride, String amountTypeOverride,
                           String skillIdOverride, String skillTypeOverride) {

        if ((targets == null || targets.isEmpty()) && amountOverride == null && !settings.has(AMOUNT)) {
            return false;
        }

        for (LivingEntity target : targets) {
            // Filter fields
            String rawAmount = filter(caster, target,
                    amountOverride != null ? amountOverride :
                            settings.has(AMOUNT) ? settings.getString(AMOUNT) : "0");

            String skillId = filter(caster, target,
                    skillIdOverride != null ? skillIdOverride :
                            settings.has(SKILLID) ? settings.getString(SKILLID) : "skillid");

            String skillType = filter(caster, target,
                    skillTypeOverride != null ? skillTypeOverride :
                            settings.has(SKILLTYPE) ? settings.getString(SKILLTYPE) : "skilltype");

            // Parse damage
            DamageParseResult parsed = parseDamageBlock(caster, target, rawAmount);
            double finalDamage = parsed.finalValue;

            // Apply flat/percent type
            String amountType = filter(caster, target,
                    amountTypeOverride != null ? amountTypeOverride :
                            settings.has(AMOUNTTYPE) ? settings.getString(AMOUNTTYPE) : "flat");

            if (target != null) {
                switch (amountType) {
                    case "percentmax" -> finalDamage = target.getMaxHealth() * finalDamage;
                    case "percentcurrent" -> finalDamage = target.getHealth() * finalDamage;
                    case "percentmissing" -> finalDamage = (target.getMaxHealth() - target.getHealth()) * finalDamage;
                }
            }

            // Determine lethal and apply health
            double actualDamage;
            boolean lethal = target != null && finalDamage >= target.getHealth();

            if (target != null) {
                actualDamage = Math.max(0, finalDamage);

                if (lethal) {
                    actualDamage = target.getHealth(); // clamp to remaining health
                    target.setHealth(0.01); // barely alive
                } else {
                    if (actualDamage > 0) {
                        double newHealth = target.getHealth() - actualDamage;
                        target.setHealth(Math.max(0, newHealth));
                    }
                }
            } else {
                actualDamage = Math.max(0, finalDamage);
            }

            // --- Contribution credit broadcast (unified call) ---
            if (caster != null && target != null) {
                processContributions(caster, target, parsed, actualDamage, lethal);
            }

            // Track CKill info
            if (caster != null && target != null) {
                ckills.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>())
                        .put(caster.getUniqueId(), new HitInfo(caster, actualDamage, skillId, skillType));
            }

            // Fire lethal events
            if (lethal && target != null && !FlagManager.hasFlag(target, "cannotdie")) {
                if (caster != null) {
                    Bukkit.getPluginManager().callEvent(
                            new CFinalHitEvent(caster, target, actualDamage, skillId, skillType));
                }
                Bukkit.getPluginManager().callEvent(
                        new CDeathEvent(target, caster, actualDamage, skillId, skillType)
                );

                Map<UUID, HitInfo> targetMarks = ckills.get(target.getUniqueId());
                if (targetMarks != null) {
                    long now = System.currentTimeMillis();
                    targetMarks.entrySet().removeIf(entry -> {
                        HitInfo mark = entry.getValue();
                        if (now - mark.timestamp <= 5000) {
                            Bukkit.getPluginManager().callEvent(
                                    new CKillEvent(mark.attacker, target, mark.damage, mark.skillid, mark.skilltype)
                            );
                            return true;
                        }
                        return now - mark.timestamp > 5000;
                    });
                    if (targetMarks.isEmpty()) ckills.remove(target.getUniqueId());
                }
            }

            // Fire standard damage events
            Bukkit.getPluginManager().callEvent(
                    new CDmgDEvent(caster, target, actualDamage, skillId, skillType)
            );
            Bukkit.getPluginManager().callEvent(
                    new CDmgREvent(target, caster, actualDamage, skillId, skillType)
            );
        }

        return true;
    }

    // ===================== Contribution Helpers =====================

    private static class ModifierWithStat {
        final ModifierManager.ModifierData.ModifierRecord rec;
        final String stat;

        ModifierWithStat(ModifierManager.ModifierData.ModifierRecord rec, String stat) {
            this.rec = rec;
            this.stat = stat;
        }
    }

	private void processContributions(LivingEntity caster, LivingEntity target,
									  DamageParseResult parsed, double actualDamage, boolean lethal) {

		List<ModifierWithStat> allModifiers = new ArrayList<>();

		BiConsumer<Map<String, Double>, LivingEntity> collectMods = (contribMap, entity) -> {
			for (Map.Entry<String, Double> entry : contribMap.entrySet()) {
				String stat = entry.getKey();
				List<ModifierManager.ModifierData.ModifierRecord> records =
						ModifierManager.getModifierRecords(entity, stat);
				for (ModifierManager.ModifierData.ModifierRecord rec : records) {
					if (!rec.isExpired() && rec.getSource() != null) {
						allModifiers.add(new ModifierWithStat(rec, stat));
					}
				}
			}
		};

		collectMods.accept(parsed.casterMultContributors, caster);
		collectMods.accept(parsed.casterAddContributors, caster);
		collectMods.accept(parsed.targetMultContributors, target);
		collectMods.accept(parsed.targetAddContributors, target);

		if (allModifiers.isEmpty()) return;

		double base = parsed.base;
		double casterMultSum = parsed.casterMultContributors.values().stream().mapToDouble(Double::doubleValue).sum();
		double casterAddSum = parsed.casterAddContributors.values().stream().mapToDouble(Double::doubleValue).sum();
		double targetMultSum = parsed.targetMultContributors.values().stream().mapToDouble(Double::doubleValue).sum();
		double targetAddSum = parsed.targetAddContributors.values().stream().mapToDouble(Double::doubleValue).sum();

		double innerBeforeTarget = base * (1 + casterMultSum) + casterAddSum;

		// Build raw contributions per modifier (positive or negative)
		Map<ModifierWithStat, Double> rawByMod = new LinkedHashMap<>();
		for (ModifierWithStat mws : allModifiers) {
			double rawContribution;
			if (parsed.casterMultContributors.containsKey(mws.stat)) {
				rawContribution = base * mws.rec.getAmount();
			} else if (parsed.casterAddContributors.containsKey(mws.stat)) {
				rawContribution = mws.rec.getAmount();
			} else if (parsed.targetMultContributors.containsKey(mws.stat)) {
				rawContribution = innerBeforeTarget * mws.rec.getAmount();
			} else {
				rawContribution = mws.rec.getAmount();
			}
			rawByMod.put(mws, rawContribution);
		}

		double sumPos = rawByMod.values().stream().filter(v -> v > 0).mapToDouble(Double::doubleValue).sum();
		double sumNegAbs = rawByMod.values().stream().filter(v -> v < 0).mapToDouble(v -> Math.abs(v)).sum();

		// Damage if negative modifiers weren't present
		double potentialDamage = base + sumPos;
		// Damage after negatives are applied (should equal base + sumPos - sumNegAbs)
		double parsedFinal = potentialDamage - sumNegAbs;

		// How much damage was prevented by negatives (but cannot exceed total negative raw)
		double prevented = Math.max(0, potentialDamage - actualDamage);
		if (prevented > sumNegAbs) prevented = sumNegAbs;

		if (!lethal) {

			// --- Positive contributions: scale to what was actually dealt ---
			if (parsedFinal != 0 && sumPos > 0) {
				double scale = actualDamage / parsedFinal;
				for (Map.Entry<ModifierWithStat, Double> e : rawByMod.entrySet()) {
					if (e.getValue() <= 0) continue;
					double scaled = e.getValue() * scale;

					boolean isCasterMod = parsed.casterMultContributors.containsKey(e.getKey().stat)
							|| parsed.casterAddContributors.containsKey(e.getKey().stat);

					applyContributionEvent(e.getKey().rec, e.getKey().stat, caster, target, scaled, isCasterMod);
				}
			}
			// If parsedFinal == 0, positives are effectively fully cancelled; we leave them uncredited (matches previous behavior).

			// --- Negative contributions: scale to prevented damage ---
			if (prevented > 0 && sumNegAbs > 0) {
				for (Map.Entry<ModifierWithStat, Double> e : rawByMod.entrySet()) {
					double raw = e.getValue();
					if (raw >= 0) continue;
					double portion = Math.abs(raw) / sumNegAbs;
					double scaled = portion * prevented;

					boolean isCasterMod = parsed.casterMultContributors.containsKey(e.getKey().stat)
							|| parsed.casterAddContributors.containsKey(e.getKey().stat);

					// negative contribution -> pass as negative (applyContributionEvent treats <=0 as reduction)
					applyContributionEvent(e.getKey().rec, e.getKey().stat, caster, target, -scaled, isCasterMod);
				}
			}

		} else {
			// Lethal: keep your existing proportional distribution of extra damage among positive modifiers
			Map<ModifierWithStat, Double> positiveRaw = new HashMap<>();
			double rawTotal = 0;

			for (ModifierWithStat mws : allModifiers) {
				double rawContribution = rawByMod.get(mws);

				if (rawContribution > 0) {
					positiveRaw.put(mws, rawContribution);
					rawTotal += rawContribution;
				}
			}

			if (rawTotal == 0) return;

			double actualExtra = actualDamage - base;
			if (actualExtra <= 0) return;

			for (Map.Entry<ModifierWithStat, Double> e : positiveRaw.entrySet()) {
				double portion = e.getValue() / rawTotal;
				double distributed = portion * actualExtra;

				boolean isCasterMod = parsed.casterMultContributors.containsKey(e.getKey().stat)
						|| parsed.casterAddContributors.containsKey(e.getKey().stat);

				applyContributionEvent(e.getKey().rec, e.getKey().stat, caster, target, distributed, isCasterMod);
			}
		}
	}

    private void applyContributionEvent(ModifierManager.ModifierData.ModifierRecord rec, String stat,
                                        LivingEntity caster, LivingEntity target,
                                        double extraDamage, boolean isCaster) {


        Player playerSource = Bukkit.getPlayer(rec.getSource());
        if (playerSource == null) return;

        String skillName = rec.getSkillId();

        if (extraDamage > 0) {
            Bukkit.getPluginManager().callEvent(
                    new CDmgAEvent(playerSource, caster, target, extraDamage, skillName)
            );
            String msg = isCaster
                    ? "§a" + playerSource.getName() + "'s " + skillName + " " + stat +
                    " buff helped " + caster.getName() + " deal §e" + extraDamage + "§a extra damage."
                    : "§a" + playerSource.getName() + "'s " + skillName + " " + stat +
                    " debuff on " + target.getName() + " helped " + caster.getName() +
                    " deal §e" + extraDamage + "§a extra damage.";
            Bukkit.broadcastMessage(msg);
        } else {
            Bukkit.getPluginManager().callEvent(
                    new CDmgBEvent(playerSource, target, caster, Math.abs(extraDamage), skillName)
            );
            String msg = isCaster
                    ? "§c" + playerSource.getName() + "'s " + skillName + " " + stat +
                    " debuff reduced " + caster.getName() + "'s damage by §e" + Math.abs(extraDamage) + "§c."
                    : "§c" + playerSource.getName() + "'s " + skillName + " " + stat +
                    " buff on " + target.getName() + " reduced " + caster.getName() +
                    "'s damage by §e" + Math.abs(extraDamage) + "§c.";
            Bukkit.broadcastMessage(msg);
        }
    }

    // ===================== Damage Parsing =====================

    private static class DamageParseResult {
        double base;
        double finalValue;
        Map<String, Double> casterMultContributors = new HashMap<>();
        Map<String, Double> casterAddContributors = new HashMap<>();
        Map<String, Double> targetMultContributors = new HashMap<>();
        Map<String, Double> targetAddContributors = new HashMap<>();

        DamageParseResult(double base) {
            this.base = base;
        }
    }

    private DamageParseResult parseDamageBlock(LivingEntity caster, LivingEntity target, String block) {
        Pattern modPattern = Pattern.compile("~([0-9.]+)(?::([^:~]*))?(?::([^:~]*))?(?::([^:~]*))?(?::([^:~]*))?~");
        Matcher modMatcher = modPattern.matcher(block);

        if (modMatcher.matches()) {
            double base;
            try { base = Double.parseDouble(modMatcher.group(1)); }
            catch (NumberFormatException e) { base = 0.0; }

            String casterMultsStr = modMatcher.group(2);
            String casterAddsStr = modMatcher.group(3);
            String targetMultsStr = modMatcher.group(4);
            String targetAddsStr = modMatcher.group(5);

            DamageParseResult result = new DamageParseResult(base);

            result.casterMultContributors = parseStatList(caster, casterMultsStr);
            result.casterAddContributors = parseStatList(caster, casterAddsStr);
            result.targetMultContributors = parseStatList(target, targetMultsStr);
            result.targetAddContributors = parseStatList(target, targetAddsStr);

            double casterMultSum = result.casterMultContributors.values().stream().mapToDouble(Double::doubleValue).sum();
            double casterAddSum = result.casterAddContributors.values().stream().mapToDouble(Double::doubleValue).sum();
            double targetMultSum = result.targetMultContributors.values().stream().mapToDouble(Double::doubleValue).sum();
            double targetAddSum = result.targetAddContributors.values().stream().mapToDouble(Double::doubleValue).sum();

            result.finalValue = (base * (1 + casterMultSum) + casterAddSum) * (1 + targetMultSum) + targetAddSum;
            return result;
        }

        return new DamageParseResult(0.0);
    }

    private Map<String, Double> parseStatList(LivingEntity entity, String list) {
        Map<String, Double> result = new HashMap<>();
        if (list == null || list.isEmpty()) return result;
        for (String stat : list.split(",")) {
            stat = stat.trim();
            if (!stat.isEmpty()) {
                result.put(stat, ModifierManager.getTotalModifier(entity, stat));
            }
        }
        return result;
    }
}

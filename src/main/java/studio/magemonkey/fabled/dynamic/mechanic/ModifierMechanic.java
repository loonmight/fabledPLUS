package studio.magemonkey.fabled.dynamic.mechanic;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.util.ModifierManager;

import java.util.List;

public class ModifierMechanic extends MechanicComponent {

    private static final String SKILL_ID = "skillid";
    private static final String STAT     = "stat";
    private static final String AMOUNT   = "amount";
    private static final String DURATION = "duration";

    @Override
    public String getKey() {
        return "modifier";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets, boolean force) {
        if (targets.isEmpty() || !settings.has(STAT) || !settings.has(SKILL_ID)) {
            return false;
        }

        return executeInternal(caster, targets,
                settings.getString(SKILL_ID),
                settings.getString(STAT),
                settings.getString(AMOUNT, "0"),
                settings.getString(DURATION, "3"));
    }

    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force,
                           String skillIdOverride,
                           String statOverride,
                           String amountOverride,
                           String durationOverride) {
        if (targets.isEmpty()) return false;

        return executeInternal(caster, targets, skillIdOverride, statOverride, amountOverride, durationOverride);
    }

    private boolean executeInternal(LivingEntity caster,
                                    List<LivingEntity> targets,
                                    String skillIdStr,
                                    String statStr,
                                    String amountStr,
                                    String durationStr) {

        for (LivingEntity target : targets) {
            String skillId = filter(caster, target, skillIdStr);
            String stat    = filter(caster, target, statStr);

            double amount = parseDoubleOrSkip(filter(caster, target, amountStr));
            if (Double.isNaN(amount)) continue;

            double duration = parseDoubleOrDefault(filter(caster, target, durationStr), 3.0);
            int ticks = (int) (duration * 20);

            ModifierManager.addModifier(target, stat, amount, ticks, skillId);
        }

        return true;
    }

    private double parseDoubleOrDefault(String s, double defaultValue) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleOrSkip(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
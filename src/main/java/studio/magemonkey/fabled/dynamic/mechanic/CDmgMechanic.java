package studio.magemonkey.fabled.dynamic.mechanic;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.event.CDmgDEvent;

import java.util.List;

public class CDmgMechanic extends MechanicComponent {

    private static final String AMOUNT = "amount";

    @Override
    public String getKey() {
        return "CDmg";
    }

    /**
     * Execute with a string override for damage.
     * This always runs the value through the filter, whether from command or settings.
     */
    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force,
                           String overrideAmount) {

        if (targets.isEmpty() && overrideAmount == null && !settings.has(AMOUNT)) {
            return false;
        }

        for (LivingEntity target : targets) {
            // Always run through filter
            String amountStr = (overrideAmount != null)
                    ? filter(caster, target, overrideAmount)
                    : filter(caster, target, settings.getString(AMOUNT));

            double amount;
            try {
                amount = Math.max(0, Double.parseDouble(amountStr));
            } catch (NumberFormatException e) {
                continue; // skip invalid values
            }

            target.setHealth(Math.max(0, target.getHealth() - amount));
            Bukkit.getPluginManager().callEvent(new CDmgDEvent(caster, target, amount));
        }

        return true;
    }

    /**
     * Backwards-compatible Double override, now just forwards to the String version.
     */
    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force,
                           Double overrideAmount) {
        return execute(caster, level, targets, force,
                (overrideAmount != null) ? String.valueOf(overrideAmount) : null);
    }

    @Override
    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force) {
        return execute(caster, level, targets, force, (String) null);
    }
}

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

    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force,
                           Double overrideAmount) {

        double amount = (overrideAmount != null)
                ? Math.max(0, overrideAmount)
                : Math.max(0, parseValues(caster, AMOUNT, level, 0));

        for (LivingEntity target : targets) {
            target.setHealth(Math.max(0, target.getHealth() - amount));
            Bukkit.getPluginManager().callEvent(new CDmgDEvent(caster, target, amount));
        }

        return true;
    }

    @Override
    public boolean execute(LivingEntity caster,
                           int level,
                           List<LivingEntity> targets,
                           boolean force) {
        return execute(caster, level, targets, force, null);
    }
}

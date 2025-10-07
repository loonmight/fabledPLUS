package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CDmgAEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;   // the entity who applied the modifier
    private final LivingEntity target;   // the entity who received the modifier
    private final LivingEntity victim;   // the entity who was attacked
    private final double extraAmount;
    private final String skillId;

    public CDmgAEvent(LivingEntity caster, LivingEntity target, LivingEntity victim,
                      double extraAmount, String skillId) {
        this.caster = caster;
        this.target = target;
        this.victim = victim;
        this.extraAmount = extraAmount;
        this.skillId = skillId;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public double getExtraAmount() {
        return extraAmount;
    }

    public String getSkillId() {
        return skillId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

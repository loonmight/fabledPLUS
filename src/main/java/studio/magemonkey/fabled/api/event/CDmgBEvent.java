package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CDmgBEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;   // entity who applied the modifier
    private final LivingEntity target;   // entity who received the modifier
    private final LivingEntity victim;   // entity actually taking the damage
    private final double blockedAmount;  // damage that was blocked/reduced
    private final String skillId;        // skill that caused the reduction

    public CDmgBEvent(LivingEntity caster, LivingEntity target, LivingEntity victim,
                      double blockedAmount, String skillId) {
        this.caster = caster;
        this.target = target;
        this.victim = victim;
        this.blockedAmount = blockedAmount;
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

    public double getBlockedAmount() {
        return blockedAmount;
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

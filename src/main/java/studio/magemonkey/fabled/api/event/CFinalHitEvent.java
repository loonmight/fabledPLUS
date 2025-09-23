package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CFinalHitEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;  // Dealer of damage
    private final LivingEntity target;  // Victim who "died"
    private final double amount;
    private final String skillid;
    private final String skilltype;

    public CFinalHitEvent(LivingEntity caster, LivingEntity target, double amount, String skillid, String skilltype) {
        this.caster = caster;
        this.target = target;
        this.amount = amount;
        this.skillid = skillid;
        this.skilltype = skilltype;
    }

    public LivingEntity getCaster() { return caster; }
    public LivingEntity getTarget() { return target; }
    public double getAmount() { return amount; }
    public String getSkillid() { return skillid; }
    public String getSkilltype() { return skilltype; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

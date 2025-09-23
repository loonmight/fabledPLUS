package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CFinalHitEvent;

public class CFinalHitTrigger implements Trigger<CFinalHitEvent> {

    @Override
    public String getKey() {
        return "CFinalHit";
    }

    @Override
    public Class<CFinalHitEvent> getEvent() {
        return CFinalHitEvent.class;
    }

    @Override
    public boolean shouldTrigger(CFinalHitEvent event, int level, Settings settings) {
        return true; // Always fires unless you want conditions
    }

    @Override
    public void setValues(CFinalHitEvent event, CastData data) {
        data.put("cfinalhit-amount", event.getAmount());
        data.put("cfinalhit-skillid", event.getSkillid());
        data.put("cfinalhit-skilltype", event.getSkilltype());
    }

    @Override
    public LivingEntity getCaster(CFinalHitEvent event) {
        return event.getCaster(); // Dealer of damage
    }

    @Override
    public LivingEntity getTarget(CFinalHitEvent event, Settings settings) {
        return event.getTarget(); // Victim
    }
}

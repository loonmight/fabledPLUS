package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CDmgDEvent;

public class CDmgDTrigger implements Trigger<CDmgDEvent> {

    @Override
    public String getKey() {
        return "CDmgD";
    }

    @Override
    public Class<CDmgDEvent> getEvent() {
        return CDmgDEvent.class;
    }

    @Override
    public boolean shouldTrigger(CDmgDEvent event, int level, Settings settings) {
        // Optional: add config filtering, e.g. only trigger if amount >= min
        double min = settings.getDouble("min-amount", 0);
        return event.getAmount() >= min;
    }

    @Override
    public void setValues(CDmgDEvent event, CastData data) {
        data.put("cdmgd-amount", event.getAmount());
    }

    @Override
    public LivingEntity getCaster(CDmgDEvent event) {
        return event.getCaster();
    }

    @Override
    public LivingEntity getTarget(CDmgDEvent event, Settings settings) {
        return event.getTarget();
    }
}

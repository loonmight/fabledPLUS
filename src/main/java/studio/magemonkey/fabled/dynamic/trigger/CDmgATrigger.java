package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CDmgAEvent;

import java.util.List;
import java.util.stream.Collectors;

public class CDmgATrigger implements Trigger<CDmgAEvent> {

    @Override
    public String getKey() {
        return "CDmgA";
    }

    @Override
    public Class<CDmgAEvent> getEvent() {
        return CDmgAEvent.class;
    }

    @Override
    public boolean shouldTrigger(CDmgAEvent event, int level, Settings settings) {
        double min = settings.getDouble("min-amount", 0);
        double max = settings.getDouble("max-amount", 999);
        double extra = event.getExtraAmount();

        if (extra < min || extra > max) return false;

        // --- Skill ID check ---
        List<String> skillIds = settings.getStringList("allowed-skillids").stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> blackSkillIds = skillIds.stream().filter(s -> s.startsWith("!"))
                .map(s -> s.substring(1))
                .collect(Collectors.toList());
        skillIds = skillIds.stream().filter(s -> !s.startsWith("!")).collect(Collectors.toList());

        if (!skillIds.isEmpty() && !skillIds.contains(event.getSkillId())
                || blackSkillIds.contains(event.getSkillId())) {
            return false;
        }

        return true;
    }

    @Override
    public void setValues(CDmgAEvent event, CastData data) {
        data.put("cdmga-amount", event.getExtraAmount());
        data.put("cdmga-skillid", event.getSkillId());
        data.put("cdmga-victim", event.getVictim());
    }

    @Override
    public LivingEntity getCaster(CDmgAEvent event) {
        return event.getCaster();
    }

    @Override
    public LivingEntity getTarget(CDmgAEvent event, Settings settings) {
        return event.getTarget();
    }

    public LivingEntity getVictim(CDmgAEvent event) {
        return event.getVictim();
    }
}

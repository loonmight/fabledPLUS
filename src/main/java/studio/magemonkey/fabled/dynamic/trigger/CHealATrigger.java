package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CHealAEvent;

import java.util.List;
import java.util.stream.Collectors;

public class CHealATrigger implements Trigger<CHealAEvent> {

    @Override
    public String getKey() {
        return "CHealA";
    }

    @Override
    public Class<CHealAEvent> getEvent() {
        return CHealAEvent.class;
    }

    @Override
    public boolean shouldTrigger(CHealAEvent event, int level, Settings settings) {
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
    public void setValues(CHealAEvent event, CastData data) {
        data.put("cheala-amount", event.getExtraAmount());
        data.put("cheala-skillid", event.getSkillId());
        data.put("cheala-healed", event.getHealed());
    }

    @Override
    public LivingEntity getCaster(CHealAEvent event) {
        return event.getCaster();
    }

    @Override
    public LivingEntity getTarget(CHealAEvent event, Settings settings) {
        return event.getTarget();
    }

	public LivingEntity getHealed(CHealAEvent event) {
		return event.getHealed();
	}
}

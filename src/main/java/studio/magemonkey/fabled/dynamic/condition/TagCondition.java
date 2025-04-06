package studio.magemonkey.fabled.dynamic.condition;

import org.bukkit.entity.LivingEntity;

public class TagCondition extends ConditionComponent {
    private static final String TAG = "tag";

    @Override
    public String getKey() {
        return "tag";
    }

    @Override
    public boolean test(LivingEntity caster, int level, LivingEntity target) {
        String requiredTag = settings.getString(TAG);
        return target.getScoreboardTags().contains(requiredTag);
    }
}

package transformations;

import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.TransformationManager;
import ct25.xtreme.gameserver.model.L2Transformation;

public class SujinChild extends L2Transformation
{
	private static final int[] SKILLS = new int[]
	{
		619
	};

	public SujinChild()
	{
		// id, colRadius, colHeight
		super(20003, 10, 11.00);
	}

	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 20003 || getPlayer().isCursedWeaponEquipped())
			return;

		transformedSkills();
	}

	public void transformedSkills()
	{
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void onUntransform()
	{
		removeSkills();
	}

	public void removeSkills()
	{
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}

	public static void main(final String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new SujinChild());
	}
}

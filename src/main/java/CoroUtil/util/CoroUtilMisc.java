package CoroUtil.util;

import CoroUtil.forge.CULog;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class CoroUtilMisc {

	public static void faceEntity(Entity entToRotate, Entity par1Entity, float par2, float par3)
    {
        double d0 = par1Entity.posX - entToRotate.posX;
        double d1 = par1Entity.posZ - entToRotate.posZ;
        double d2;

        if (par1Entity instanceof EntityLivingBase)
        {
        	EntityLivingBase entityliving = (EntityLivingBase)par1Entity;
            d2 = entityliving.posY + (double)entityliving.getEyeHeight() - (entToRotate.posY + (double)entToRotate.getEyeHeight());
        }
        else
        {
            d2 = (par1Entity.getEntityBoundingBox().minY + par1Entity.getEntityBoundingBox().maxY) / 2.0D - (entToRotate.posY + (double)entToRotate.getEyeHeight());
        }

        double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1);
        float f2 = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float)(-(Math.atan2(d2, d3) * 180.0D / Math.PI));
        entToRotate.rotationPitch = updateRotation(entToRotate.rotationPitch, f3, par3);
        entToRotate.rotationYaw = updateRotation(entToRotate.rotationYaw, f2, par2);
    }
	
	public static float updateRotation(float par1, float par2, float par3)
    {
        float f3 = MathHelper.wrapDegrees(par2 - par1);

        if (f3 > par3)
        {
            f3 = par3;
        }

        if (f3 < -par3)
        {
            f3 = -par3;
        }

        return par1 + f3;
    }
	
	/*public static AxisAlignedBB getFixedBounds(Vector3f a, Vector3f b) {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(a.x, a.y, a.z, b.x, b.y, b.z);
		if (bb.minX > bb.maxX) {
			double swap = bb.minX;
			bb.minX = bb.maxX;
			bb.maxX = swap;
		}
		
		if (bb.minY > bb.maxY) {
			double swap = bb.minY;
			bb.minY = bb.maxY;
			bb.maxY = swap;
		}
		
		if (bb.minZ > bb.maxZ) {
			double swap = bb.minZ;
			bb.minZ = bb.maxZ;
			bb.maxZ = swap;
		}
		
		return bb;
	}*/
	
	public static BlockCoord vecToChunkCoords(Vec3 parVec) {
		return new BlockCoord(MathHelper.floor(parVec.xCoord), MathHelper.floor(parVec.yCoord), MathHelper.floor(parVec.zCoord));
	}
	
	public static BlockCoord addCoords(BlockCoord coords1, BlockCoord coords2) {
		return new BlockCoord(coords1.posX+coords2.posX, coords1.posY+coords2.posY, coords1.posZ+coords2.posZ);
	}
	
	public static void sendPlayerMsg(EntityPlayerMP entP, String msg) {
		sendCommandSenderMsg(entP, msg);
	}
	
	public static void sendCommandSenderMsg(ICommandSender entP, String msg) {
		entP.sendMessage(new TextComponentString(msg));
	}

    public static float adjVal(float source, float target, float adj) {
        if (source < target) {
            source += adj;
            //fix over adjust
            if (source > target) {
                source = target;
            }
        } else if (source > target) {
            source -= adj;
            //fix over adjust
            if (source < target) {
                source = target;
            }
        }
        return source;
    }

    /**
     * Game likes to force an exception and crash out if a mod adds a zero weight spawn entry to a biome+enum ceature type when the total weight is zero for all entries in the list
     * this method checks for this and removes them
     */
    public static void fixBadBiomeEntitySpawns() {
        for (Biome biome : Biome.REGISTRY) {

            for (EnumCreatureType type : EnumCreatureType.values()) {

                List<Biome.SpawnListEntry> list = biome.getSpawnableList(type);
                boolean found = false;
                String str = "";
                int totalWeight = 0;

                for (Biome.SpawnListEntry entry : list) {
                    totalWeight += entry.itemWeight;
                    if (entry.itemWeight == 0) {
                        found = true;
                        str += entry.entityClass.getName() + ", ";
                    }
                }

                if (found) {
                    if (totalWeight == 0) {
                        CULog.log("Detected issue for entity(s)" + str);
                        CULog.log("Biome '" + biome.biomeName + "' for EnumCreatureType '" + type.name() + "', SpawnListEntry size: " + list.size());
                        CULog.log("Clearing relevant spawnableList to fix issue");
                        //detected crashable state of data, clear out spawnlist then
                        if (type == EnumCreatureType.MONSTER) {
                            biome.spawnableMonsterList.clear();
                        } else if (type == EnumCreatureType.CREATURE) {
                            biome.spawnableCreatureList.clear();
                        } else if (type == EnumCreatureType.WATER_CREATURE) {
                            biome.spawnableWaterCreatureList.clear();
                        } else if (type == EnumCreatureType.AMBIENT) {
                            biome.spawnableCaveCreatureList.clear();
                        } else {
                            //theres also Biome.modSpawnableLists for modded entries, but ive decided not to care about this one
                        }
                    }
                }
            }
        }
    }
}

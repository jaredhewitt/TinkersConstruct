package slimeknights.tconstruct.gadgets.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import scala.None;
import slimeknights.tconstruct.library.TinkerRegistry;

public class BlockPunji extends Block {

  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  public static final PropertyEnum<ConnectionHorizontal> CON_HOR = PropertyEnum.create("connection_horizontal", ConnectionHorizontal.class);
  public static final PropertyEnum<ConnectionDiagonal> CON_DIA = PropertyEnum.create("connection_diagonal", ConnectionDiagonal.class);
  public static final PropertyEnum<ConnectionVertical> CON_VER = PropertyEnum.create("connection_vertical", ConnectionVertical.class);
  //public static final PropertyBool NORTH = PropertyBool.create("north");
  //public static final PropertyBool EAST = PropertyBool.create("east");
  //public static final PropertyBool NORTHEAST = PropertyBool.create("northeast");
  //public static final PropertyBool NORTHWEST = PropertyBool.create("northwest");

  public BlockPunji() {
    super(Material.plants);

    this.setBlockBounds(0.125f, 0, 0.125f, 0.875f, 0.375f, 0.875f);
    this.setStepSound(Block.soundTypeGrass);
    this.setCreativeTab(TinkerRegistry.tabGadgets);
    this.setHardness(3.0f);

    this.setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.DOWN));
  }

  @Override
  protected BlockState createBlockState() {
    return new BlockState(this, FACING, CON_HOR, CON_DIA, CON_VER);
  }

  /**
   * Convert the given metadata into a BlockState for this Block
   */
  public IBlockState getStateFromMeta(int meta) {
    if(meta >= EnumFacing.values().length) {
      meta = EnumFacing.DOWN.ordinal();
    }
    EnumFacing face = EnumFacing.values()[meta];

    return this.getDefaultState().withProperty(FACING, face).withProperty(CON_HOR, ConnectionHorizontal.NONE).withProperty(CON_VER, ConnectionVertical.NONE);
  }

  /**
   * Convert the BlockState into the correct metadata value
   */
  public int getMetaFromState(IBlockState state) {
    return state.getValue(FACING).ordinal();
  }

  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    ConnectionVertical ver = ConnectionVertical.NONE;
    ConnectionDiagonal dia = ConnectionDiagonal.NONE;
    ConnectionHorizontal hor = ConnectionHorizontal.NONE;
    EnumFacing facing = state.getValue(FACING);

    int off = -facing.ordinal() % 2;

    EnumFacing face1 = EnumFacing.values()[(facing.ordinal() + 2) % 6];
    EnumFacing face2 = EnumFacing.values()[(facing.ordinal() + 4 + off) % 6];

    // North/East Connector
    IBlockState north = worldIn.getBlockState(pos.offset(face1));
    IBlockState east = worldIn.getBlockState(pos.offset(face2));
    if(north.getBlock() == this && north.getValue(FACING) == facing) {
      hor = ConnectionHorizontal.NORTH;
    }
    if(east.getBlock() == this && east.getValue(FACING) == facing) {
      if(hor == ConnectionHorizontal.NORTH) {
        hor = ConnectionHorizontal.BOTH;
      }
      else {
        hor = ConnectionHorizontal.EAST;
      }
    }

    // Diagonal connections
    IBlockState northeast = worldIn.getBlockState(pos.offset(face1).offset(face2));
    IBlockState southwest = worldIn.getBlockState(pos.offset(face1).offset(face2.getOpposite()));
    if(northeast.getBlock() == this && northeast.getValue(FACING) == facing) {
      dia = ConnectionDiagonal.NORTHEAST;
    }
    if(southwest.getBlock() == this && southwest.getValue(FACING) == facing) {
      if(dia == ConnectionDiagonal.NORTHEAST) {
        dia = ConnectionDiagonal.BOTH;
      }
      else {
        dia = ConnectionDiagonal.SOUTHWEST;
      }
    }

    // up/down model?
    if(facing.getAxis() != EnumFacing.Axis.Y) {
      for(EnumFacing dir : EnumFacing.Plane.HORIZONTAL.facings()) {
        IBlockState stateDir = worldIn.getBlockState(pos.offset(dir));
        if(stateDir.getBlock() == this) {
          if(stateDir.getValue(FACING) == EnumFacing.DOWN) {
            if(ver == ConnectionVertical.NONE) {
              ver = ConnectionVertical.DOWN;
            }
            else {
              ver = ConnectionVertical.BOTH;
            }
          }
          if(stateDir.getValue(FACING) == EnumFacing.UP) {
            if(ver == ConnectionVertical.NONE) {
              ver = ConnectionVertical.UP;
            }
            else {
              ver = ConnectionVertical.BOTH;
            }
          }
        }
      }
    }

    ver = ConnectionVertical.NONE;

    return state.withProperty(CON_HOR, hor)
                .withProperty(CON_DIA, dia)
                .withProperty(CON_VER, ver);
  }

  /**
   * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
   * IBlockstate
   */
  public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    EnumFacing enumfacing = facing.getOpposite();

    return this.getDefaultState().withProperty(FACING, enumfacing);
  }

  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
    EnumFacing facing = worldIn.getBlockState(pos).getValue(FACING);
    setBlockBoundsBasedOnState(facing);
  }

  public void setBlockBoundsBasedOnState(EnumFacing facing) {
    float h = 0.375f;

    float xMin = 0.1875f;
    float xMax = 1f - xMin;
    float zMin = 0.1875f;
    float zMax = 1f - zMin;
    float yMin = 0.1875f;
    float yMax = 1f - yMin;

    switch(facing) {
      case DOWN:
        yMin = 0;
        yMax = h;
        break;
      case UP:
        yMin = 1f-h;
        yMax = 1;
        break;
      case SOUTH:
        zMin = 1f-h;
        zMax = 1;
        break;
      case NORTH:
        zMax = h;
        zMin = 0;
        break;
      case EAST:
        xMin = 1f-h;
        xMax = 1;
        break;
      case WEST:
        xMax = h;
        xMin = 0;
        break;
    }

    this.setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    setBlockBoundsBasedOnState(state.getValue(FACING));
    return super.getCollisionBoundingBox(worldIn, pos, state);
  }

  @Override
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    if(entityIn instanceof EntityLiving) {
      float damage = 3f;
      if(entityIn.fallDistance > 0) {
        damage += entityIn.fallDistance * 1.5f + 2f;
      }
      entityIn.attackEntityFrom(DamageSource.cactus, damage);
      ((EntityLiving) entityIn).addPotionEffect(new PotionEffect(Potion.moveSlowdown.getId(), 20, 1));
    }
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean isFullCube() {
    return false;
  }

  private enum ConnectionHorizontal implements IStringSerializable {
    NONE,
    NORTH,
    EAST,
    BOTH;

    @Override
    public String getName() {
      return this.toString();
    }
  }

  private enum ConnectionDiagonal implements IStringSerializable {
    NONE,
    NORTHEAST,
    SOUTHWEST,
    BOTH;

    @Override
    public String getName() {
      return this.toString();
    }
  }

  private enum ConnectionVertical implements IStringSerializable {
    NONE,
    UP,
    DOWN,
    BOTH;

    @Override
    public String getName() {
      return this.toString();
    }
  }
}
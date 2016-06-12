package slimeknights.tconstruct.plugin.waila;

import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mcp.mobius.waila.api.IWailaRegistrar;
import slimeknights.mantle.pulsar.pulse.Pulse;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.tileentity.IProgress;
import slimeknights.tconstruct.smeltery.block.BlockCasting;
import slimeknights.tconstruct.smeltery.block.BlockTank;
import slimeknights.tconstruct.smeltery.tileentity.TileCasting;
import slimeknights.tconstruct.smeltery.tileentity.TileTank;

@Pulse(id = Waila.PulseId, modsRequired = Waila.modid, defaultEnable = true)
public class Waila {

  public static final String modid = "Waila";
  public static final String PulseId = modid + "Integration";

  static final String CONFIG_TANK = Util.prefix("tank");
  static final String CONFIG_CASTING = Util.prefix("casting");
  static final String CONFIG_PROGRESS = Util.prefix("progress");

  @Subscribe
  public void preInit(FMLPreInitializationEvent event) {
    FMLInterModComms.sendMessage("Waila", "register", "slimeknights.tconstruct.plugin.waila.Waila.wailaCallback");
  }

  public static void wailaCallback(IWailaRegistrar registrar) {
    // config entries
    registrar.addConfig(TConstruct.modName, CONFIG_TANK, true);
    registrar.addConfig(TConstruct.modName, CONFIG_CASTING, true);
    registrar.addConfig(TConstruct.modName, CONFIG_PROGRESS, true);

    // Casting info
    CastingDataProvider castingDataProvider = new CastingDataProvider();
    registrar.registerBodyProvider(castingDataProvider, TileCasting.class);

    // Fluid Display
    TankDataProvider tankDataProvider = new TankDataProvider();
    registrar.registerBodyProvider(tankDataProvider, TileTank.class);
    registrar.registerBodyProvider(tankDataProvider, TileCasting.class);

    // Progress
    ProgressDataProvider progressDataProvider = new ProgressDataProvider();
    registrar.registerBodyProvider(progressDataProvider, IProgress.class);

    //registrar.registerBodyProvider(new CastingChannelDataProvider(), CastingChannelLogic.class);
    //registrar.registerBodyProvider(new EssenceExtractorDataProvider(), EssenceExtractor.class);

    // Casting systems
    //registrar.registerBodyProvider(new BasinDataProvider(), CastingBasinLogic.class);
    //registrar.registerBodyProvider(new TableDataProvider(), CastingTableLogic.class);

    // Smeltery
    //registrar.registerBodyProvider(new SmelteryDataProvider(), SmelteryBlock.class);
  }
}

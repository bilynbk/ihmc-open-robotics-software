package us.ihmc.quadrupedRobotics.providers;

import us.ihmc.communication.net.PacketConsumer;
import us.ihmc.communication.streamingData.GlobalDataProducer;
import us.ihmc.quadrupedRobotics.communication.packets.PlanarVelocityPacket;
import us.ihmc.quadrupedRobotics.params.DoubleArrayParameter;
import us.ihmc.quadrupedRobotics.params.ParameterFactory;
import us.ihmc.robotics.MathTools;
import us.ihmc.robotics.dataStructures.registry.YoVariableRegistry;
import us.ihmc.robotics.dataStructures.variable.DoubleYoVariable;

import javax.vecmath.Vector3d;

public class QuadrupedPlanarVelocityInputProvider
{
   private final YoVariableRegistry registry = new YoVariableRegistry(getClass().getSimpleName());

   private final ParameterFactory parameterFactory = ParameterFactory.createWithRegistry(getClass(), registry);
   private final DoubleArrayParameter planarVelocityLowerLimitsParameter = parameterFactory.createDoubleArray("planarVelocityLowerLimits", -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
   private final DoubleArrayParameter planarVelocityUpperLimitsParameter = parameterFactory.createDoubleArray("planarVelocityUpperLimits", Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

   private final DoubleYoVariable yoPlanarVelocityInputX;
   private final DoubleYoVariable yoPlanarVelocityInputY;
   private final DoubleYoVariable yoPlanarVelocityInputZ;
   private final Vector3d planarVelocityInput;

   public QuadrupedPlanarVelocityInputProvider(GlobalDataProducer globalDataProducer, YoVariableRegistry parentRegistry)
   {
      yoPlanarVelocityInputX = new DoubleYoVariable("planarVelocityInputX", registry);
      yoPlanarVelocityInputY = new DoubleYoVariable("planarVelocityInputY", registry);
      yoPlanarVelocityInputZ = new DoubleYoVariable("planarVelocityInputZ", registry);
      yoPlanarVelocityInputX.set(0);
      yoPlanarVelocityInputY.set(0);
      yoPlanarVelocityInputZ.set(0);
      planarVelocityInput = new Vector3d();

      if (globalDataProducer != null)
      {
         globalDataProducer.attachListener(PlanarVelocityPacket.class, new PacketConsumer<PlanarVelocityPacket>()
         {
            @Override
            public void receivedPacket(PlanarVelocityPacket packet)
            {
               packet.get(planarVelocityInput);
               yoPlanarVelocityInputX.set(MathTools
                     .clipToMinMax(planarVelocityInput.getX(), planarVelocityLowerLimitsParameter.get(0), planarVelocityUpperLimitsParameter.get(0)));
               yoPlanarVelocityInputY.set(MathTools
                     .clipToMinMax(planarVelocityInput.getY(), planarVelocityLowerLimitsParameter.get(1), planarVelocityUpperLimitsParameter.get(1)));
               yoPlanarVelocityInputZ.set(MathTools
                     .clipToMinMax(planarVelocityInput.getZ(), planarVelocityLowerLimitsParameter.get(2), planarVelocityUpperLimitsParameter.get(2)));
            }
         });
      }

      parentRegistry.addChild(registry);
   }

   public Vector3d get()
   {
      planarVelocityInput.set(yoPlanarVelocityInputX.getDoubleValue(), yoPlanarVelocityInputY.getDoubleValue(), yoPlanarVelocityInputZ.getDoubleValue());
      return planarVelocityInput;
   }
}

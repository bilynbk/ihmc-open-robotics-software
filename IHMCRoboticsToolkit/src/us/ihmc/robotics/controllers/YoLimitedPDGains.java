package us.ihmc.robotics.controllers;

import us.ihmc.robotics.dataStructures.registry.YoVariableRegistry;
import us.ihmc.robotics.dataStructures.variable.DoubleYoVariable;
import us.ihmc.robotics.math.filters.RateLimitedYoVariable;

public class YoLimitedPDGains extends YoPDGains
{
   private final RateLimitedYoVariable limitedKp;
   private final RateLimitedYoVariable limitedKd;

   private final DoubleYoVariable maxKpRate;
   private final DoubleYoVariable maxKdRate;

   public YoLimitedPDGains(String suffix, double controlDT, YoVariableRegistry registry)
   {
      super(suffix, registry);

      maxKpRate = new DoubleYoVariable("maxKpRate" + suffix, registry);
      maxKdRate = new DoubleYoVariable("maxKdRate" + suffix, registry);

      limitedKp = new RateLimitedYoVariable("limitedKp" + suffix, registry, maxKpRate, kp, controlDT);
      limitedKd = new RateLimitedYoVariable("limitedKd" + suffix, registry, maxKdRate, kd, controlDT);

      maxKpRate.set(Double.POSITIVE_INFINITY);
      maxKdRate.set(Double.POSITIVE_INFINITY);
   }

   public void setMaxKpRate(double maxKpRate)
   {
      this.maxKpRate.set(maxKpRate);
   }

   public void setMaxKdRate(double maxKdRate)
   {
      this.maxKdRate.set(maxKdRate);
   }

   @Override
   public double getKp()
   {
      return limitedKp.getDoubleValue();
   }

   @Override
   public double getKd()
   {
      return limitedKd.getDoubleValue();
   }

   @Override
   public DoubleYoVariable getYoKp()
   {
      return limitedKp;
   }

   @Override
   public DoubleYoVariable getYoKd()
   {
      return limitedKd;
   }
}
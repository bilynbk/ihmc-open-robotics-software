package us.ihmc.commonWalkingControlModules.instantaneousCapturePoint.icpOptimization.projectionAndRecursionMultipliers.interpolation;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import us.ihmc.robotics.MathTools;

public class CubicProjectionMatrix extends DenseMatrix64F
{
   private final CubicSplineCoefficientMatrix cubicSplineCoefficientMatrix = new CubicSplineCoefficientMatrix();
   private final CubicTimeMatrix cubicTimeMatrix = new CubicTimeMatrix();

   private double duration;

   public CubicProjectionMatrix()
   {
      super(1, 4);
   }

   public void setSegmentDuration(double duration)
   {
      this.duration = duration;
      cubicSplineCoefficientMatrix.setSegmentDuration(duration);
   }

   public double getSegmentDuration()
   {
      return duration;
   }

   public void update(double timeRemaining)
   {
      double timeInCurrentState = duration - timeRemaining;
      timeInCurrentState = MathTools.clipToMinMax(timeInCurrentState, 0.0, duration);
      cubicTimeMatrix.setCurrentTime(timeInCurrentState);

      CommonOps.mult(cubicTimeMatrix, cubicSplineCoefficientMatrix, this);
   }
}

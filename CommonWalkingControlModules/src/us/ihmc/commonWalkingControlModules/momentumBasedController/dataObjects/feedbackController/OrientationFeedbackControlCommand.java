package us.ihmc.commonWalkingControlModules.momentumBasedController.dataObjects.feedbackController;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.ejml.data.DenseMatrix64F;

import us.ihmc.robotics.controllers.OrientationPIDGains;
import us.ihmc.robotics.controllers.OrientationPIDGainsInterface;
import us.ihmc.robotics.geometry.FrameOrientation;
import us.ihmc.robotics.geometry.FrameVector;
import us.ihmc.robotics.referenceFrames.ReferenceFrame;
import us.ihmc.robotics.screwTheory.RigidBody;
import us.ihmc.robotics.screwTheory.SpatialAccelerationVector;

public class OrientationFeedbackControlCommand extends FeedbackControlCommand<OrientationFeedbackControlCommand>
{
   private static final ReferenceFrame worldFrame = ReferenceFrame.getWorldFrame();
   private final Quat4d desiredOrientationInWorld = new Quat4d();
   private final Vector3d desiredAngularVelocityInWorld = new Vector3d();
   private final Vector3d feedForwardAngularAccelerationInWorld = new Vector3d();
   private final DenseMatrix64F selectionMatrix = new DenseMatrix64F(3, SpatialAccelerationVector.SIZE);

   private RigidBody base;
   private RigidBody endEffector;

   private final OrientationPIDGains gains = new OrientationPIDGains();
   private double weightForSolver = Double.POSITIVE_INFINITY;

   public OrientationFeedbackControlCommand()
   {
      super(FeedbackControlCommandType.ORIENTATION_CONTROL);
      setSelectionMatrixToIdentity();
   }

   @Override
   public void set(OrientationFeedbackControlCommand other)
   {
      set(other.base, other.endEffector);
      setSelectionMatrix(other.selectionMatrix);
      setGains(other.gains);
      desiredOrientationInWorld.set(other.desiredOrientationInWorld);
      desiredAngularVelocityInWorld.set(other.desiredAngularVelocityInWorld);
      feedForwardAngularAccelerationInWorld.set(other.feedForwardAngularAccelerationInWorld);
      setWeightForSolver(other.weightForSolver);
   }

   public void set(RigidBody base, RigidBody endEffector)
   {
      this.base = base;
      this.endEffector = endEffector;
   }

   public void setBase(RigidBody base)
   {
      this.base = base;
   }

   public void setEndEffector(RigidBody endEffector)
   {
      this.endEffector = endEffector;
   }

   public void setGains(OrientationPIDGainsInterface gains)
   {
      this.gains.set(gains);
   }

   public void set(FrameOrientation desiredOrientation, FrameVector desiredAngularVelocity, FrameVector feedForwardAngularAcceleration)
   {
      desiredOrientation.checkReferenceFrameMatch(worldFrame);
      desiredAngularVelocity.checkReferenceFrameMatch(worldFrame);
      feedForwardAngularAcceleration.checkReferenceFrameMatch(worldFrame);

      desiredOrientation.getQuaternion(desiredOrientationInWorld);
      desiredAngularVelocity.get(desiredAngularVelocityInWorld);
      feedForwardAngularAcceleration.get(feedForwardAngularAccelerationInWorld);
   }

   public void setSelectionMatrixToIdentity()
   {
      selectionMatrix.reshape(3, SpatialAccelerationVector.SIZE);
      selectionMatrix.set(0, 0, 1.0);
      selectionMatrix.set(1, 1, 1.0);
      selectionMatrix.set(2, 2, 1.0);
   }

   public void setSelectionMatrix(DenseMatrix64F selectionMatrix)
   {
      if (selectionMatrix.getNumRows() > 3)
         throw new RuntimeException("Unexpected number of rows: " + selectionMatrix.getNumRows());
      if (selectionMatrix.getNumCols() != SpatialAccelerationVector.SIZE)
         throw new RuntimeException("Unexpected number of columns: " + selectionMatrix.getNumCols());

      this.selectionMatrix.set(selectionMatrix);
   }

   public DenseMatrix64F getSelectionMatrix()
   {
      return selectionMatrix;
   }

   public void setWeightForSolver(double weight)
   {
      weightForSolver = weight;
   }

   public void getIncludingFrame(FrameOrientation desiredOrientationToPack, FrameVector desiredAngularVelocityToPack, FrameVector feedForwardAngularAccelerationToPack)
   {
      desiredOrientationToPack.setIncludingFrame(worldFrame, desiredOrientationInWorld);
      desiredAngularVelocityToPack.setIncludingFrame(worldFrame, desiredAngularVelocityInWorld);
      feedForwardAngularAccelerationToPack.setIncludingFrame(worldFrame, feedForwardAngularAccelerationInWorld);
   }

   public RigidBody getBase()
   {
      return base;
   }

   public RigidBody getEndEffector()
   {
      return endEffector;
   }

   public double getWeightForSolver()
   {
      return weightForSolver;
   }

   public OrientationPIDGainsInterface getGains()
   {
      return gains;
   }
}

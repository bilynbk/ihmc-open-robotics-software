package us.ihmc.robotics.geometry;

import org.junit.Ignore;
import org.junit.Test;
import us.ihmc.robotics.referenceFrames.ReferenceFrame;
import us.ihmc.tools.agileTesting.BambooAnnotations.EstimatedDuration;
import us.ihmc.tools.random.RandomTools;
import us.ihmc.tools.test.JUnitTools;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 *
 * FIXME: don't use scales in test...
 *
 */
public class FramePointTest extends FrameTupleTest<Point3d>
{
   public static double epsilon = 1e-10;

   @Override
   public FramePoint createEmptyFrameTuple()
   {
      return new FramePoint();
   }

   @Override
   public FramePoint createFrameTuple(ReferenceFrame referenceFrame, double x, double y, double z)
   {
      return new FramePoint(referenceFrame, x, y, z);
   }

   @Override
   public FramePoint createFrameTuple(ReferenceFrame referenceFrame, double x, double y, double z, String name)
   {
      return new FramePoint(referenceFrame, x, y, z, name);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public final void testRunTestMain()
   {
      // create frames
      ReferenceFrame A = ReferenceFrame.constructARootFrame("A", false, true, true);

      Transform3d transform = new Transform3d();
      Vector3d euler = new Vector3d(Math.PI / 2.0, 0.0, 0.0);
      transform.setEuler(euler);
      Vector3d translation = new Vector3d(5.0, 0.0, 0.0);
      transform.setTranslation(translation);

      //    System.out.println("B translation = \n" + transform);
      ReferenceFrame B = ReferenceFrame.constructFrameWithUnchangingTransformToParent("B", A, transform, false, true, true);

      transform = new Transform3d();
      euler = new Vector3d(0.0, Math.PI / 2.0, 0.0);
      transform.setEuler(euler);
      translation = new Vector3d(5.0, 0.0, 0.0);
      transform.setTranslation(translation);

      //    System.out.println("C translation = \n" + transform);
      ReferenceFrame C = ReferenceFrame.constructFrameWithUnchangingTransformToParent("C", B, transform, false, true, true);

      transform = new Transform3d();
      euler = new Vector3d(0.0, 0.0, Math.PI / 2.0);
      transform.setEuler(euler);
      translation = new Vector3d(5.0, 0.0, 0.0);
      transform.setTranslation(translation);

      //    System.out.println("D translation = \n" + transform);
      ReferenceFrame D = ReferenceFrame.constructFrameWithUnchangingTransformToParent("D", C, transform, false, true, true);

      FramePoint V1 = new FramePoint(D, 2.0, 3.0, 4.0);

      // System.out.println("V1 = " + V1);
      assertEquals(2, V1.getX(), epsilon);
      assertEquals(3, V1.getY(), epsilon);
      assertEquals(4, V1.getZ(), epsilon);

      try
      {
         FramePoint V1inD = new FramePoint(V1);
         V1inD.changeFrame(D);

         //       System.out.println(V1inD);
         assertEquals(2, V1inD.getX(), epsilon);
         assertEquals(3, V1inD.getY(), epsilon);
         assertEquals(4, V1inD.getZ(), epsilon);

         FramePoint V1inC = new FramePoint(V1);
         V1inC.changeFrame(C);

         //       System.out.println(V1inC);
         assertEquals(2, V1inC.getX(), epsilon);
         assertEquals(2, V1inC.getY(), epsilon);
         assertEquals(4, V1inC.getZ(), epsilon);

         FramePoint V1inB = new FramePoint(V1);
         V1inB.changeFrame(B);

         //       System.out.println(V1inB);
         assertEquals(9, V1inB.getX(), epsilon);
         assertEquals(2, V1inB.getY(), epsilon);
         assertEquals(-2, V1inB.getZ(), epsilon);

         FramePoint V1inA = new FramePoint(V1);
         V1inA.changeFrame(A);

         //       System.out.println(V1inA);
         assertEquals(14, V1inA.getX(), epsilon);
         assertEquals(2, V1inA.getY(), epsilon);
         assertEquals(2, V1inA.getZ(), epsilon);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail();
      }

      V1 = new FramePoint(A, 2.0, 3.0, 4.0);
      assertEquals(2, V1.getX(), epsilon);
      assertEquals(3, V1.getY(), epsilon);
      assertEquals(4, V1.getZ(), epsilon);

      // System.out.println("V2 = " + V1);
      try
      {
         FramePoint V1inA = new FramePoint(V1);
         V1inA.changeFrame(A);
         assertEquals(2, V1inA.getX(), epsilon);
         assertEquals(3, V1inA.getY(), epsilon);
         assertEquals(4, V1inA.getZ(), epsilon);

         FramePoint V1inB = new FramePoint(V1);
         V1inB.changeFrame(B);
         assertEquals(-3, V1inB.getX(), epsilon);
         assertEquals(4, V1inB.getY(), epsilon);
         assertEquals(-3, V1inB.getZ(), epsilon);

         FramePoint V1inC = new FramePoint(V1);
         V1inC.changeFrame(C);
         assertEquals(3, V1inC.getX(), epsilon);
         assertEquals(4, V1inC.getY(), epsilon);
         assertEquals(-8, V1inC.getZ(), epsilon);

         FramePoint V1inD = new FramePoint(V1);
         V1inD.changeFrame(D);
         assertEquals(4, V1inD.getX(), epsilon);
         assertEquals(2, V1inD.getY(), epsilon);
         assertEquals(-8, V1inD.getZ(), epsilon);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail();
      }

      V1 = new FramePoint(B, 2.0, 3.0, 4.0);
      assertEquals(2, V1.getX(), epsilon);
      assertEquals(3, V1.getY(), epsilon);
      assertEquals(4, V1.getZ(), epsilon);

      try
      {
         FramePoint V1inA = new FramePoint(V1);
         V1inA.changeFrame(A);
         assertEquals(7, V1inA.getX(), epsilon);
         assertEquals(-4, V1inA.getY(), epsilon);
         assertEquals(3, V1inA.getZ(), epsilon);
         FramePoint V1inB = new FramePoint(V1);
         V1inB.changeFrame(B);
         assertEquals(2, V1inB.getX(), epsilon);
         assertEquals(3, V1inB.getY(), epsilon);
         assertEquals(4, V1inB.getZ(), epsilon);
         FramePoint V1inC = new FramePoint(V1);
         V1inC.changeFrame(C);
         assertEquals(-4, V1inC.getX(), epsilon);
         assertEquals(3, V1inC.getY(), epsilon);
         assertEquals(-3, V1inC.getZ(), epsilon);
         FramePoint V1inD = new FramePoint(V1);
         V1inD.changeFrame(D);
         assertEquals(3, V1inD.getX(), epsilon);
         assertEquals(9, V1inD.getY(), epsilon);
         assertEquals(-3, V1inD.getZ(), epsilon);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail();
      }

   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testOtherConstructors() //Brett was here
   {
      Tuple3d position = new Point3d(1.0, 1.0, 1.0);
      String name = "myName";
      FramePoint framePosition = new FramePoint(theFrame, position);
      assertEquals("These should be equal", position, framePosition.getPoint());
      framePosition.setName(name);
      assertEquals("These should be equal", name, framePosition.getName());
      assertEquals("These should be equal", theFrame, framePosition.getReferenceFrame());

      FramePoint framePositionName = new FramePoint(aFrame, position, name);
      assertEquals("These should be equal", position, framePositionName.getPoint());
      assertEquals("These should be equal", name, framePositionName.getName());
      assertEquals("These should be equal", aFrame, framePositionName.getReferenceFrame());

      double[] doubleArray = {7.0, 7.0, 7.0};
      Tuple3d position2 = new Point3d(doubleArray);
      String name2 = "name-O";
      FramePoint framePositionArray = new FramePoint(theFrame, doubleArray);
      assertEquals("These should be equal", position2, framePositionArray.getPoint());
      framePositionArray.setName(name2);
      assertEquals("These should be equal", name2, framePositionArray.getName());
      assertEquals("These should be equal", theFrame, framePositionArray.getReferenceFrame());

      double[] doubleArray2 = {-7.0, 14.0, 21.0};
      Tuple3d position3 = new Point3d(doubleArray2);
      String name3 = "name-P";
      FramePoint framePositionArrayName = new FramePoint(theFrame, doubleArray2);
      assertEquals("These should be equal", position3, framePositionArrayName.getPoint());
      framePositionArrayName.setName(name3);
      assertEquals("These should be equal", name3, framePositionArrayName.getName());
      assertEquals("These should be equal", theFrame, framePositionArrayName.getReferenceFrame());

      String name4 = "name-Q";
      FramePoint frameName = new FramePoint(theFrame, name4);
      Tuple3d position4 = new Point3d();
      assertEquals("These should be equal", position4, frameName.getPoint());
      assertEquals("These should be equal", name4, frameName.getName());
      assertEquals("These should be equal", theFrame, frameName.getReferenceFrame());
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testMidPoint()
   {
      FramePoint expectedMidPoint = new FramePoint(theFrame, 9.0, 3.6, -1.16);
      FrameVector difference = new FrameVector(theFrame, 1.5, -3.1, 12.9);

      FramePoint framePoint1 = new FramePoint(expectedMidPoint);
      FramePoint framePoint2 = new FramePoint(expectedMidPoint);
      framePoint1.add(difference);
      framePoint2.sub(difference);

      FramePoint actualMidPoint = FramePoint.getMidPoint(framePoint1, framePoint2);

      assertTrue(expectedMidPoint.epsilonEquals(actualMidPoint, epsilon));
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000,expected = RuntimeException.class)
   public void testMidPointException()
   {
      FramePoint framePoint1 = new FramePoint(theFrame, 1.0, 2.0, 3.0);
      FramePoint framePoint2 = new FramePoint(aFrame, 0.0, -1.0, 8.2);

      FramePoint.getMidPoint(framePoint1, framePoint2);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testAverage()
   {
      List<FramePoint> framePoints = new ArrayList<FramePoint>();
      framePoints.add(new FramePoint(theFrame, 1.0, 3.0, 9.0));
      framePoints.add(new FramePoint(theFrame, 5.0, 3.0, 3.0));
      framePoints.add(new FramePoint(theFrame, 6.0, 0.0, 18.0));
      FramePoint average = FramePoint.average(framePoints);
      assertEquals(4.0, average.getX(), epsilon);
      assertEquals(2.0, average.getY(), epsilon);
      assertEquals(10.0, average.getZ(), epsilon);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000,expected = ReferenceFrameMismatchException.class)
   public void testGetXYplaneDistance()
   {
      FramePoint firstPoint = new FramePoint(theFrame, 1.0, 2.0, 5.0);
      FramePoint secondPoint = new FramePoint(theFrame, 4.0, -2.0, -3.0);

      assertEquals(5.0, firstPoint.getXYPlaneDistance(secondPoint), epsilon);

      //Test for reference frame mismatch
      FramePoint thirdPoint = new FramePoint(aFrame, 4.0, -2.0, -3.0);
      firstPoint.getXYPlaneDistance(thirdPoint);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000,expected = ReferenceFrameMismatchException.class)
   public void testDistance() //Brett
   {
      FramePoint framePoint1 = new FramePoint(theFrame);
      FramePoint framePoint2 = new FramePoint(aFrame);

      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 3.0);
      double expectedReturn = Math.sqrt(14.0);
      double actualReturn = framePoint1.distance(framePoint);
      assertEquals("return value", expectedReturn, actualReturn, Double.MIN_VALUE);

      //Test for reference frame mismatch
      framePoint1.distance(framePoint2);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000,expected = ReferenceFrameMismatchException.class)
   public void testDistanceSquared() //Brett
   {
      FramePoint framePoint1 = new FramePoint(theFrame);
      FramePoint framePoint2 = new FramePoint(aFrame);


      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 3.0);
      double expectedReturn = 14.0;
      double actualReturn = framePoint.distanceSquared(framePoint1);
      assertEquals("return value", expectedReturn, actualReturn, Double.MIN_VALUE);

      //Test for reference frame mismatch
      framePoint1.distanceSquared(framePoint2);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testToFramePoint2d() //Brett
   {
      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 5.0);
      FramePoint2d framePoint2d = framePoint.toFramePoint2d();

      assertSame(theFrame, framePoint2d.getReferenceFrame());
      assertEquals(1.0, framePoint2d.getX(), epsilon);
      assertEquals(2.0, framePoint2d.getY(), epsilon);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testGetFramePoint2d() //Brett
   {
      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 5.0);
      FramePoint2d framePoint2d = new FramePoint2d();
      framePoint.getFramePoint2d(framePoint2d);
      //      System.out.println(framePoint);
      //      System.out.println(framePoint2d);
      assertSame(theFrame, framePoint2d.getReferenceFrame());
      assertEquals(1.0, framePoint2d.getX(), epsilon);
      assertEquals(2.0, framePoint2d.getY(), epsilon);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testGetPoint()
   {
      Point3d tuple3d = new Point3d(1.0, 1.0, 1.0);
      Point3d tuple3dCopy = new Point3d();
      FramePoint framePoint = new FramePoint(theFrame, tuple3d, "framePoint");

      tuple3dCopy = framePoint.getPoint();
      assertTrue(tuple3d.epsilonEquals(tuple3dCopy, epsilon));
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public void testFrameChanges()
   {
      FramePoint framePoint = new FramePoint(theFrame);
      Transform3d transform3d = new Transform3d();
      FramePoint result = new FramePoint();
      
    //TODO deprecate method FramePoint.changeFrameUsingTransformCopy() or convert to abstract
      result = framePoint.changeFrameUsingTransformCopy(childFrame, transform3d);
      result.checkReferenceFrameMatch(childFrame);
      
      result = new FramePoint(framePoint);
      result.changeFrame(theFrame);
      result.checkReferenceFrameMatch(theFrame);
      
      framePoint.changeFrameUsingTransform(childFrame, transform3d);
      framePoint.checkReferenceFrameMatch(childFrame);   
      
      framePoint.changeFrame(theFrame); //cause of failure
      framePoint.checkReferenceFrameMatch(theFrame);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public final void testApplyTransform()
   {
      FramePoint frameTuple = new FramePoint(theFrame);

      Transform3d transform = new Transform3d();
      transform.setTranslation(new Vector3d(2.0, 4.0, 8.0));
      frameTuple.applyTransform(transform);

      assertEquals(2.0, frameTuple.getX(), epsilon);
      assertEquals(4.0, frameTuple.getY(), epsilon);
      assertEquals(8.0, frameTuple.getZ(), epsilon);
      frameTuple.applyTransform(transform);
      assertEquals(4.0, frameTuple.getX(), epsilon);
      assertEquals(8.0, frameTuple.getY(), epsilon);
      assertEquals(16.0, frameTuple.getZ(), epsilon);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public final void testApplyTransformScale()
   {
      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 3.0);
      Transform3d transform3D = new Transform3d();
      transform3D.setScale(0.1, 0.01, 0.001);

      FramePoint resultPoint = new FramePoint(framePoint);
      resultPoint.applyTransform(transform3D);
      FramePoint expectedResultPoint = new FramePoint(theFrame, 0.1, 0.02, 0.003);

      assertNotSame(framePoint, resultPoint);
      assertTrue(expectedResultPoint.epsilonEquals(resultPoint, epsilon));
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public final void testApplyTransformTranslate()
   {
      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 3.0);
      Transform3d transform3D = new Transform3d();

      Vector3d translateVector = new Vector3d(0.1, 0.5, 0.9);
      transform3D.setTranslation(translateVector);
      FramePoint resultPoint = new FramePoint(framePoint);
      resultPoint.applyTransform(transform3D);
      FramePoint expectedResultPoint = new FramePoint(theFrame, 1.1, 2.5, 3.9);

      assertNotSame(framePoint, resultPoint);
      assertTrue(expectedResultPoint.epsilonEquals(resultPoint, epsilon));
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000)
   public final void testApplyTransformRotateZ()
   {
      FramePoint framePoint = new FramePoint(theFrame, 1.0, 2.0, 3.0);
      Transform3d transform3D = new Transform3d();

      Vector3d rotateVector = new Vector3d(0.0, 0.0, Math.PI / 2.0);
      transform3D.setEuler(rotateVector);
      FramePoint resultPoint = new FramePoint(framePoint);
      resultPoint.applyTransform(transform3D);
      FramePoint expectedResultPoint = new FramePoint(theFrame, -2.0, 1.0, 3.0);

      assertNotSame(framePoint, resultPoint);
      assertTrue(expectedResultPoint.epsilonEquals(resultPoint, epsilon));
   }

   @Ignore("Doesn't throw exception since FrameTuple has been created. Not sure if it should throw an exception yet. Sylvain")

	@EstimatedDuration
	@Test(timeout=300000)
   public void testConstructors()
   {
      Random random = new Random();
      double[] xyz = RandomTools.generateRandomDoubleArray(random, 3, Double.MAX_VALUE);
      FramePoint pointToBeTested;
      ReferenceFrame referenceFrame = null;

      try
      {
         pointToBeTested = new FramePoint(referenceFrame, xyz);
         fail("Should have thrown an exception");
      }
      catch (RuntimeException e)
      {
         //Good
      }

      pointToBeTested = new FramePoint(aFrame, xyz);
      Point3d point3dExpected = new Point3d(xyz);
      assertTrue(aFrame == pointToBeTested.getReferenceFrame());
      assertTrue(pointToBeTested.getPoint().epsilonEquals(point3dExpected, epsilon));

      double max = Double.MAX_VALUE / 2.0;
      point3dExpected = RandomTools.generateRandomPoint(random, max, max, max);
      pointToBeTested = null;
      try
      {
         pointToBeTested = new FramePoint(referenceFrame, point3dExpected, "");
         fail("Should have thrown an exception");
      }
      catch (RuntimeException e)
      {
         //Good
      }
      pointToBeTested = new FramePoint(aFrame, point3dExpected, "");
      assertTrue(aFrame == pointToBeTested.getReferenceFrame());
      assertTrue("Expected: " + point3dExpected + ", actual: " + pointToBeTested.getPoint(), point3dExpected.epsilonEquals(pointToBeTested.getPoint(), epsilon));

      xyz = RandomTools.generateRandomDoubleArray(random, 3, Double.MAX_VALUE);

      try
      {
         pointToBeTested = new FramePoint(referenceFrame, xyz, "");
         fail("Should have thrown an exception");
      }
      catch (RuntimeException e)
      {
         //Good
      }

      pointToBeTested = new FramePoint(aFrame, xyz, "");
      point3dExpected = new Point3d(xyz);
      assertTrue(aFrame == pointToBeTested.getReferenceFrame());
      assertTrue(pointToBeTested.getPoint().epsilonEquals(point3dExpected, epsilon));
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000,expected = ReferenceFrameMismatchException.class)
   public void testYawAboutPoint() //Brett
   {
      FramePoint pointToYawAbout = new FramePoint(theFrame, 0, 0, 0);
      FramePoint point = new FramePoint(theFrame, 1, 1, 1);
      double yaw = Math.PI;

      FramePoint result = point.yawAboutPoint(pointToYawAbout, yaw);
      //      System.out.println(result);
      assertEquals("These should be equal", -1.0, result.getX(), epsilon);
      assertEquals("These should be equal", -1.0, result.getY(), epsilon);
      assertEquals("These should be equal", 1.0, result.getZ(), epsilon);

      //Check for reference frame mismatch
      FramePoint point2 = new FramePoint(aFrame, 1, 1, 1);
      point2.yawAboutPoint(pointToYawAbout, yaw);
   }

	@EstimatedDuration(duration = 0.0)
	@Test(timeout = 30000,expected = ReferenceFrameMismatchException.class)
   public void testPitchAboutPoint() //Brett
   {
      FramePoint pointToPitchAbout = new FramePoint(theFrame, 0, 0, 0);
      FramePoint point = new FramePoint(theFrame, 1, 1, 1);
      double pitch = Math.PI;

      FramePoint result = point.pitchAboutPoint(pointToPitchAbout, pitch);
      //      System.out.println(result);
      assertEquals("These should be equal", -1.0, result.getX(), epsilon);
      assertEquals("These should be equal", 1.0, result.getY(), epsilon);
      assertEquals("These should be equal", -1.0, result.getZ(), epsilon);

      //Check for reference frame mismatch
      FramePoint point2 = new FramePoint(aFrame, 1, 1, 1);
      point2.pitchAboutPoint(pointToPitchAbout, pitch);
   }

   public static void assertFramePointEquals(FramePoint expected, FramePoint actual, double delta)
   {
      expected.checkReferenceFrameMatch(actual);
      JUnitTools.assertTuple3dEquals(expected.getPoint(), actual.getPoint(), delta);
   }
}
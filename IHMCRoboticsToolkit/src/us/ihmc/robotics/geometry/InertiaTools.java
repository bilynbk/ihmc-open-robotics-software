package us.ihmc.robotics.geometry;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.ejml.data.DenseMatrix64F;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class InertiaTools
{
   /**
    * Returns the radii of an ellipsoid given the inertia parameters, assuming a uniform mass distribution.
    * @param principalMomentsOfInertia principal moments of inertia {Ixx, Iyy, Izz}
    * @param mass mass of the link
    * @return the three radii of the inertia ellipsoid
    */
   public static Vector3d getInertiaEllipsoidRadii(Vector3d principalMomentsOfInertia, double mass)
   {
      double Ixx = principalMomentsOfInertia.getX();
      double Iyy = principalMomentsOfInertia.getY();
      double Izz = principalMomentsOfInertia.getZ();

//    http://en.wikipedia.org/wiki/Ellipsoid#Mass_properties
      Vector3d ret = new Vector3d();
      ret.setX(Math.sqrt(5.0 / 2.0 * (Iyy + Izz - Ixx) / mass));
      ret.setY(Math.sqrt(5.0 / 2.0 * (Izz + Ixx - Iyy) / mass));
      ret.setZ(Math.sqrt(5.0 / 2.0 * (Ixx + Iyy - Izz) / mass));

      return ret;
   }

   public static Matrix3d rotate(RigidBodyTransform inertialFrameRotation, Matrix3d inertia)
   {
      Matrix3d temp = new Matrix3d();
      inertialFrameRotation.getRotation(temp);

      return rotate(temp, inertia);
   }

   public static Matrix3d rotate(Matrix3d inertialFrameRotation, Matrix3d inertia)
   {
      Matrix3d temp = new Matrix3d();
      temp.mulTransposeRight(inertia, inertialFrameRotation);

      Matrix3d result = new Matrix3d();
      result.mul(inertialFrameRotation, temp);

      return result;

//
//    inertialFrameRotation.transpose();
//    inertia.mul(inertialFrameRotation);
//    inertialFrameRotation.transpose();
//    inertialFrameRotation.mul(inertia);

   }


   public static void computePrincipalMomentsOfInertia(DenseMatrix64F Inertia, Matrix3d principalAxesRotationToPack, Vector3d principalMomentsOfInertiaToPack)
   {
      double[][] moiArray = new double[][]{
         {Inertia.get(0, 0), Inertia.get(0, 1), Inertia.get(0, 2)},
         {Inertia.get(1, 0), Inertia.get(1, 1), Inertia.get(1, 2)},
         {Inertia.get(2, 0), Inertia.get(2, 1), Inertia.get(2, 2)}
      };

      Matrix inertiaForSVD = new Matrix(moiArray);
      computePrincipalMomentsOfInertia(inertiaForSVD, principalAxesRotationToPack, principalMomentsOfInertiaToPack);
   }

   public static void computePrincipalMomentsOfInertia(Matrix3d Inertia, Matrix3d principalAxesRotationToPack, Vector3d principalMomentsOfInertiaToPack)
   {
      double[][] moiArray = new double[][]{
         {Inertia.getM00(), Inertia.getM01(), Inertia.getM02()},
         {Inertia.getM10(), Inertia.getM11(), Inertia.getM12()},
         {Inertia.getM20(), Inertia.getM21(), Inertia.getM22()}
      };

      Matrix inertiaForSVD = new Matrix(moiArray);
      computePrincipalMomentsOfInertia(inertiaForSVD, principalAxesRotationToPack, principalMomentsOfInertiaToPack);
   }

   public static void computePrincipalMomentsOfInertia(Matrix inertiaForSVD, Matrix3d principalAxesRotationToPack, Vector3d principalMomentsOfInertiaToPack)
   {
      // Decompose Inertia Matrix:  I = U*sigma*V

      SingularValueDecomposition inertiaSVD = new SingularValueDecomposition(inertiaForSVD);

      Matrix sigma = inertiaSVD.getS();

      //    Matrix U = inertiaSVD.getU();
      Matrix V = inertiaSVD.getV();

      // If determinant is -1.0, then multiply V by -1. Since I = U*sigma*U_Transpose, then I = (-U) * sigma * (-U_Transpose)
      double determinant = V.det();
      if (determinant < 0.0)
      {
         V = V.times(-1.0);
         determinant = -determinant;
      }

      if (Math.abs(determinant - 1.0) > 1e-5)
      {
         throw new RuntimeException("Problem in Link.addEllipsoidFromMassProperties(). Determinant should be 1.0");
      }

      principalMomentsOfInertiaToPack.set(sigma.get(0, 0), sigma.get(1, 1), sigma.get(2, 2));
      principalAxesRotationToPack.set(V.getRowPackedCopy());
   }

}

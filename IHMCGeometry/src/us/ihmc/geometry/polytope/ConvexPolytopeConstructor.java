package us.ihmc.geometry.polytope;

import java.util.Random;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import us.ihmc.robotics.random.RandomTools;

public class ConvexPolytopeConstructor
{

   public static ConvexPolytope constructUnitCube()
   {
      ConvexPolytope polytope = new ConvexPolytope();

      PolytopeVertex vertex0 = polytope.addVertex(0.0, 0.0, 0.0);
      PolytopeVertex vertex1 = polytope.addVertex(1.0, 0.0, 0.0);
      PolytopeVertex vertex2 = polytope.addVertex(1.0, 1.0, 0.0);
      PolytopeVertex vertex3 = polytope.addVertex(0.0, 1.0, 0.0);

      PolytopeVertex vertex4 = polytope.addVertex(new Point3d(0.0, 0.0, 1.0));
      PolytopeVertex vertex5 = polytope.addVertex(new Point3d(1.0, 0.0, 1.0));
      PolytopeVertex vertex6 = polytope.addVertex(new Point3d(1.0, 1.0, 1.0));
      PolytopeVertex vertex7 = polytope.addVertex(new Point3d(0.0, 1.0, 1.0));

      polytope.addEdge(vertex0, vertex1);
      polytope.addEdge(vertex1, vertex2);
      polytope.addEdge(vertex2, vertex3);
      polytope.addEdge(vertex3, vertex0);

      polytope.addEdge(vertex4, vertex5);
      polytope.addEdge(vertex5, vertex6);
      polytope.addEdge(vertex6, vertex7);
      polytope.addEdge(vertex7, vertex4);

      polytope.addEdge(vertex0, vertex4);
      polytope.addEdge(vertex1, vertex5);
      polytope.addEdge(vertex2, vertex6);
      polytope.addEdge(vertex3, vertex7);

      return polytope;
   }

   public static ConvexPolytope constructBoxWithCenterAtZero(double halfLengthX, double halfWidthY, double halfHeightZ)
   {
      ConvexPolytope polytope = new ConvexPolytope();

      PolytopeVertex vertex0 = polytope.addVertex(-halfLengthX, -halfWidthY, -halfHeightZ);
      PolytopeVertex vertex1 = polytope.addVertex(halfLengthX, -halfWidthY, -halfHeightZ);
      PolytopeVertex vertex2 = polytope.addVertex(halfLengthX, halfWidthY, -halfHeightZ);
      PolytopeVertex vertex3 = polytope.addVertex(-halfLengthX, halfWidthY, -halfHeightZ);

      PolytopeVertex vertex4 = polytope.addVertex(new Point3d(-halfLengthX, -halfWidthY, halfHeightZ));
      PolytopeVertex vertex5 = polytope.addVertex(new Point3d(halfLengthX, -halfWidthY, halfHeightZ));
      PolytopeVertex vertex6 = polytope.addVertex(new Point3d(halfLengthX, halfWidthY, halfHeightZ));
      PolytopeVertex vertex7 = polytope.addVertex(new Point3d(-halfLengthX, halfWidthY, halfHeightZ));

      polytope.addEdge(vertex0, vertex1);
      polytope.addEdge(vertex1, vertex2);
      polytope.addEdge(vertex2, vertex3);
      polytope.addEdge(vertex3, vertex0);

      polytope.addEdge(vertex4, vertex5);
      polytope.addEdge(vertex5, vertex6);
      polytope.addEdge(vertex6, vertex7);
      polytope.addEdge(vertex7, vertex4);

      polytope.addEdge(vertex0, vertex4);
      polytope.addEdge(vertex1, vertex5);
      polytope.addEdge(vertex2, vertex6);
      polytope.addEdge(vertex3, vertex7);

      return polytope;
   }

   public static ConvexPolytope constructRandomSphereOutlinedPolytope(Random random, int numberOfPoints, double radius, double xyzBoundary)
   {
      ConvexPolytope polytope = new ConvexPolytope();

      Point3d sphereCenter = RandomTools.generateRandomPoint(random, xyzBoundary, xyzBoundary, xyzBoundary);

      for (int i = 0; i < numberOfPoints; i++)
      {
         Vector3d randomVector = RandomTools.generateRandomVector(random, radius);
         Point3d point = new Point3d(sphereCenter);
         point.add(randomVector);

         //TODO: Need to connect the edges later once they are used in the algorithm!!
         polytope.addVertex(point);
      }

      return polytope;
   }

   public static ConvexPolytope constructSinglePointPolytope(Point3d singlePoint)
   {
      ConvexPolytope polytope = new ConvexPolytope();
      polytope.addVertex(singlePoint);
      return polytope;
   }

   public static ConvexPolytope constructFromVertices(double[][] vertices)
   {
      ConvexPolytope polytope = new ConvexPolytope();
      for (double[] vertex : vertices)
      {
         polytope.addVertex(vertex);
      }

      return polytope;
   }
}

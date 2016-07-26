package us.ihmc.exampleSimulations.skippy;

import us.ihmc.graphics3DAdapter.GroundProfile3D;
import us.ihmc.graphics3DAdapter.graphics.Graphics3DObject;
import us.ihmc.graphics3DAdapter.graphics.appearances.YoAppearance;
import us.ihmc.robotics.Axis;
import us.ihmc.simulationconstructionset.*;
import us.ihmc.simulationconstructionset.util.LinearGroundContactModel;
import us.ihmc.simulationconstructionset.util.ground.FlatGroundProfile;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

/**
 * This class SkippyRobot is a public class that extends Robot. The class Robot is
 * included in the Simulation Construction Set and has built in graphics, dynamics, etc.
 * Extending the class is an easy way to make a new type of robot, in this case a SkippyRobot.
 */
public class SkippyRobot extends Robot
{
   private static final long serialVersionUID = -7671864179791904256L;
   private UniversalJoint foot;
   private PinJoint hip;
   private PinJoint shoulder;
   private ArrayList<GroundContactPoint> groundContactPoints = new ArrayList();

   /* L* are the link lengths, M* are the link masses, and R* are the radii of the links.
    * Iyy* are the moments of inertia of the links, which are defined about the COM for each link.
    */
   public static final double
      L1 = 1.0, M1 = 1.0, R1 = 0.1, Iyy1 = (1.0/3.0)*M1*Math.pow(L1,2), // Leg
      L2 = 2.0, M2 = 1.0, R2 = 0.05, Iyy2 = (1.0/3.0)*M2*Math.pow(L2,2), // Torso
      L3 = 3.0, M3 = 0.5, R3 = 0.05, Iyy3 = (1.0/12.0)*M3*Math.pow(L3,2); // Crossbar

   public SkippyRobot()
   {
      super("Skippy");

      this.setGravity(0.0,0.0,-9.81); // Newtons

      // Create GroundContactPoints to distinguish when robot touches the ground
      GroundContactPoint footContact = new GroundContactPoint("rootContactPoint", new Vector3d(0.0, 0.0, 0.0), this);
      GroundContactPoint hipContact = new GroundContactPoint("hipContactPoint", new Vector3d(0.0, 0.0, 0.0), this);
      GroundContactPoint shoulderContact = new GroundContactPoint("shoulderContactPoint", new Vector3d(0.0, 0.0, 0.0), this);
      GroundContactPoint leftContact = new GroundContactPoint("leftContactPoint", new Vector3d(-L3 / 2.0, 0.0, 0.0), this);
      GroundContactPoint rightContact = new GroundContactPoint("rightContactPoint", new Vector3d(L3 / 2.0, 0.0, 0.0), this);

      groundContactPoints.add(footContact);
      groundContactPoints.add(hipContact);
      groundContactPoints.add(shoulderContact);
      groundContactPoints.add(leftContact);
      groundContactPoints.add(rightContact);

      // Create joints and assign links. Each joint should be placed L* distance away from its previous joint.
      foot = new UniversalJoint("foot_X", "foot_Y", new Vector3d(0.0, 0.0, 0.0), this, Axis.X, Axis.Y);
      foot.changeOffsetVector(new Vector3d(0.0, 0.0, 0.0)); // initial Cartesian position of foot
      foot.setInitialState(0.75, 0.0, 0.75, 0.0); // initial position "q" of foot
      Link leg = createLeg();
      foot.setLink(leg);
      this.addRootJoint(foot);
      foot.addGroundContactPoint(footContact);

      hip = new PinJoint("hip", new Vector3d(0.0, 0.0, L1), this, Axis.X);
      Link torso = createTorso();
      hip.setLink(torso);
      hip.setInitialState(-2.5,0.0);
      this.foot.addJoint(hip);
      hip.addGroundContactPoint(hipContact);

      shoulder = new PinJoint("shoulder", new Vector3d(0.0, 0.0, L2), this, Axis.Y);
      Link arms = createArms();
      shoulder.setLink(arms);
      shoulder.setInitialState(0.5,0.0);
      shoulder.setDamping(0.3);
      this.hip.addJoint(shoulder);
      shoulder.addGroundContactPoint(shoulderContact);
      shoulder.addGroundContactPoint(leftContact);
      shoulder.addGroundContactPoint(rightContact);

      GroundContactModel ground = new LinearGroundContactModel(this, 1422, 150.6, 50.0, 1000.0, this.getRobotsYoVariableRegistry());
      GroundProfile3D profile = new FlatGroundProfile();
      ground.setGroundProfile3D(profile);
      this.setGroundContactModel(ground);
   }

   private Link createLeg()
   {
      Link leg = new Link("leg");
      leg.setMass(M1);
      leg.setComOffset(0.0, 0.0, L1 / 2.0);
      leg.setMomentOfInertia(Iyy1, Iyy1, 0.0001);

      // create a LinkGraphics object to manipulate the visual representation of the link
      Graphics3DObject linkGraphics = new Graphics3DObject();
      linkGraphics.addCube(R1, R1, L1, YoAppearance.Crimson());

      // associate the linkGraphics object with the link object
      leg.setLinkGraphics(linkGraphics);

      return leg;
   }

   private Link createTorso()
   {
      Link torso = new Link("torso");
      torso.setMass(M2);
      torso.setComOffset(0.0, 0.0, L2 / 2.0);
      torso.setMomentOfInertia(Iyy2, 0.0001, 0.0001);

      Graphics3DObject linkGraphics = new Graphics3DObject();
      linkGraphics.addCylinder(L2, R2, YoAppearance.LightSkyBlue());
      linkGraphics.addSphere(0.10,YoAppearance.White());

      torso.setLinkGraphics(linkGraphics);

      return torso;
   }

   private Link createArms()
   {
      Link arms = new Link("arms");
      arms.setMass(M3);
      arms.setComOffset(0.0, 0.0, L3 / 2.0);
      arms.setMomentOfInertia(0.0001, Iyy3, 0.0001);

      Graphics3DObject linkGraphics = new Graphics3DObject();
      linkGraphics.rotate(Math.toRadians(90), Axis.Y);
      linkGraphics.translate(0.0, 0.0, -L3 / 2.0);
      linkGraphics.addCylinder(L3, R3, YoAppearance.DarkBlue());

      arms.setLinkGraphics(linkGraphics);

      return arms;
   }
   public UniversalJoint getLegJoint()
   {
      return foot;
   }
   public PinJoint getHipJoint()
   {
      return hip;
   }

   public PinJoint getShoulderJoint()
   {
      return shoulder;
   }
}
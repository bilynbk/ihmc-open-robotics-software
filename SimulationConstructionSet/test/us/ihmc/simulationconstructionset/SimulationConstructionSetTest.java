package us.ihmc.simulationconstructionset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.AWTException;

import org.fest.swing.exception.ActionFailedException;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import us.ihmc.utilities.Axis;
import us.ihmc.utilities.ThreadTools;
import us.ihmc.yoUtilities.dataStructure.registry.YoVariableRegistry;
import us.ihmc.yoUtilities.dataStructure.variable.BooleanYoVariable;
import us.ihmc.yoUtilities.dataStructure.variable.DoubleYoVariable;
import us.ihmc.yoUtilities.dataStructure.variable.EnumYoVariable;

import us.ihmc.simulationconstructionset.examples.FallingBrickRobot;
import us.ihmc.simulationconstructionset.gui.SimulationGUITestFixture;

public class SimulationConstructionSetTest
{
   private boolean isGradleBuild()
   {
      String property = System.getProperty("bamboo.gradle");
      if (property != null && property.contains("yes"))
      {
         return true;
      }

      return false;
   }
   
   @Ignore
   @Test(timeout=300000)
   public void testSimulationConstructionSetNewViewportWindowUsingGUITestFixture() throws AWTException
   {
      Assume.assumeTrue(!isGradleBuild());
      FallingBrickRobot robot = new FallingBrickRobot();

      SimulationConstructionSet scs = new SimulationConstructionSet(robot);
      scs.setDT(0.0001, 100);
      scs.setFrameMaximized();
      scs.startOnAThread();
      scs.setSimulateDuration(2.0);

      ThreadTools.sleep(2000);
      SimulationGUITestFixture testFixture = new SimulationGUITestFixture(scs);
      
      testFixture.closeAllViewportWindows();
      testFixture.selectNewViewportWindowMenu();
      
      testFixture.focusNthViewportWindow(0);

      ThreadTools.sleepForever();
      
      testFixture.closeAndDispose();
      scs.closeAndDispose();
      scs = null;
      testFixture = null;

   }

   @Ignore
   @Test(timeout=300000)
   public void testSimulationConstructionSetMovieGenerationUsingGUITestFixture() throws AWTException
   {
      Assume.assumeTrue(!isGradleBuild());
      FallingBrickRobot robot = new FallingBrickRobot();

      SimulationConstructionSet scs = new SimulationConstructionSet(robot);
      scs.setDT(0.0001, 100);
      scs.setFrameMaximized();
      scs.startOnAThread();
      scs.setSimulateDuration(2.0);

      SimulationGUITestFixture testFixture = new SimulationGUITestFixture(scs);
      testFixture.clickSimulateButton();
      ThreadTools.sleep(1000);
      
      testFixture.clickMediaCaptureButton();

      testFixture.focusDialog("Export Movie");
      testFixture.clickPlayButton();
      
      ThreadTools.sleepForever();

      testFixture.closeAndDispose();
      scs.closeAndDispose();
      scs = null;
      testFixture = null;
   }
}

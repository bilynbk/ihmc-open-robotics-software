package us.ihmc.tools.processManagement;

import java.util.ArrayList;

import org.junit.Test;

import us.ihmc.tools.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.tools.io.printing.PrintTools;
import us.ihmc.tools.processManagement.ProcessTools;

public class ProcessToolsTest
{

	@ContinuousIntegrationTest(estimatedDuration = 0.0)
	@Test(timeout = 30000)
   public void testGetAllSystemProcesses()
   {
      ArrayList<String> processLines = ProcessTools.getAllSystemProcesses();
      
      for (String line : processLines)
         PrintTools.info(this, line);
   }
}

package us.ihmc.tools.io.logging;

import org.junit.Test;

import us.ihmc.tools.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class LogToolsTest
{
   private static final Level[] levels = new Level[]{Level.ALL, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.INFO, Level.OFF, Level.SEVERE, Level.SEVERE};

   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testSetGlobalLogLevel()
   {
      for(Level level : levels)
      {
         LogTools.setGlobalLogLevel(level);

         assertEquals(level.getName(), LogTools.getGlobalLogLevel().getName());
         assertEquals(level.intValue(), LogTools.getGlobalLogLevel().intValue());
      }
   }
}

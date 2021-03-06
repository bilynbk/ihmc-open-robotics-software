package us.ihmc.plotting;

import javax.vecmath.Point2d;

import us.ihmc.plotting.artifact.LineArtifact;

public class PlotterDemo
{
   public void showPlotter()
   {
      Plotter plotter = new Plotter();
      plotter.setPreferredSize(800, 600);
      
      plotter.setViewRange(10.0);
      plotter.setXYZoomEnabled(true);
//      plotter.setViewRange(1.0);
      plotter.setShowLabels(true);
      
      plotter.addArtifact(new LineArtifact("01", new Point2d(0, 0), new Point2d(1, 1)));
      plotter.addArtifact(new LineArtifact("02", new Point2d(1, 1), new Point2d(2, 0)));
      plotter.addArtifact(new LineArtifact("03", new Point2d(2, 0), new Point2d(3, 1)));
      
      plotter.showInNewWindow();
      
//      plotter.setScale(40.0, 20.0);
//      plotter.setFocusPointX(2.5);
//      plotter.setFocusPointY(-3.0);
   }
   
   public static void main(String[] args)
   {
      PlotterDemo plotterDemo = new PlotterDemo();
      plotterDemo.showPlotter();
   }
}

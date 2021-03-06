package us.ihmc.plotting.artifact;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import us.ihmc.plotting.Graphics2DAdapter;
import us.ihmc.plotting.Plotter2DAdapter;

public class PointListArtifact extends Artifact
{
   private final List<Point2d> points = new ArrayList<Point2d>();
   private int historyLength = 1;
   private Color historyColor = Color.BLUE;
   int medianFilterSize = 20;
   int meanFilterSize = 999;
   private int size = 10;
   
   private final Point2d tempPoint = new Point2d();
   private final Vector2d tempRadii = new Vector2d();

   public PointListArtifact(String id)
   {
      this(id, 1);
   }

   public PointListArtifact(String id, Point2d point)
   {
      this(id, 1);

      setPoint(point);
   }

   public PointListArtifact(String id, int history)
   {
      super(id);
      setType("point");
      setLevel(2);
      System.currentTimeMillis();
      historyLength = history;
      color = Color.red;
   }

   public void setPoint(Point2d point)
   {
      synchronized (points)
      {
         points.add(point);

         if (points.size() > historyLength)
         {
            points.remove(0);
         }
      }
   }

   public void setSize(int size)
   {
      this.size = size;
   }

   public Point2d getPoint2d()
   {
      if (points.size() == 0)
         return null;

      return points.get(0);
   }

   public void setHistoryLength(int length)
   {
      historyLength = length;
   }

   public void setHistoryColor(Color color)
   {
      historyColor = color;
   }

   public static double getMedian(Vector<?> buffer)
   {
      int n = buffer.size();
      double[] unsorted = new double[n];
      double[] sorted = new double[n];

      for (int i = 0; i < n; i++)
      {
         unsorted[i] = ((Double) buffer.elementAt(i)).doubleValue();
      }

      System.arraycopy(unsorted, 0, sorted, 0, n);
      Arrays.sort(sorted);

      return sorted[n / 2];
   }

   public double getMean(Vector<?> buffer)
   {
      int n = buffer.size();
      double mean = 0;

      for (int i = 0; i < n; i++)
      {
         mean += ((Double) buffer.elementAt(i)).doubleValue();
      }

      mean = mean / n;

      return mean;
   }

   public double getStdDev(Vector<?> buffer, double mean)
   {
      int n = buffer.size();
      double sd = 0;

      for (int i = 0; i < n; i++)
      {
         sd += Math.pow((((Double) buffer.elementAt(i)).doubleValue() - mean), 2);
      }

      sd = Math.sqrt(sd);

      return sd;
   }

   /**
    * Must provide a draw method for plotter to render artifact
    */
   @Override
   public void draw(Graphics2DAdapter graphics)
   {
      Vector<Double> xMedianFliter = new Vector<Double>();
      Vector<Double> yMedianFliter = new Vector<Double>();
      Vector<Double> xMeanFilter = new Vector<Double>();
      Vector<Double> yMeanFilter = new Vector<Double>();

      Point2d coordinate = null;
      synchronized (points)
      {
         for (int i = 0; i < points.size(); i++)
         {
            // paint points
            coordinate = points.get(i);

            if (coordinate != null)
            {
               if (i == (points.size() - 1))
               {
                  graphics.setColor(color);
                  tempPoint.set(coordinate.getX(), coordinate.getY());
                  tempRadii.set(size, size);
                  graphics.drawOvalFilled(tempPoint, tempRadii);
               }
               else
               {
                  graphics.setColor(historyColor);
                  tempPoint.set(coordinate.getX(), coordinate.getY());
                  tempRadii.set(size * 0.7, size * 0.7);
                  graphics.drawOvalFilled(tempPoint, tempRadii);
               }

               // save for median and mean
               if (i >= (points.size() - medianFilterSize))
               {
                  xMedianFliter.addElement(new Double(coordinate.getX()));
                  yMedianFliter.addElement(new Double(coordinate.getY()));
               }

               if (i >= (points.size() - meanFilterSize))
               {
                  xMeanFilter.addElement(new Double(coordinate.getX()));
                  yMeanFilter.addElement(new Double(coordinate.getY()));
               }
            }

         }
      }
   }

   @Override
   public void drawLegend(Plotter2DAdapter graphics, Point2d origin)
   {
   }

   public void save(PrintWriter printWriter)
   {
      for (int i = 0; i < points.size(); i++)
      {
         Point2d coordinate = points.get(i);
         printWriter.println(coordinate.getX() + " " + coordinate.getY());
      }
   }

   @Override
   public void drawHistory(Graphics2DAdapter graphics)
   {
      throw new RuntimeException("Not implemented!");
   }

   @Override
   public void takeHistorySnapshot()
   {
      throw new RuntimeException("Not implemented!");
   }
}

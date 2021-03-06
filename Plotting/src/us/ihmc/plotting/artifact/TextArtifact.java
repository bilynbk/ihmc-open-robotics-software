package us.ihmc.plotting.artifact;

import java.awt.Font;
import java.io.PrintWriter;

import javax.vecmath.Point2d;

import us.ihmc.plotting.Graphics2DAdapter;
import us.ihmc.plotting.Plotter2DAdapter;

public class TextArtifact extends Artifact
{
   private double x1;
   private double y1;
   private String text;
   private Font font = Font.getFont(Font.SANS_SERIF);
   private int xPixelOffset = 0;
   private int yPixelOffset = 0;
   
   private final Point2d tempPoint = new Point2d();

   public TextArtifact(String id, String text, double x1, double y1)
   {
      super(id);
      setLevel(1);
      this.text = text;
      this.x1 = x1;
      this.y1 = y1;
   }

   public void setPosition(double x1, double y1)
   {
      this.x1 = x1;
      this.y1 = y1;
   }

   public void setPixelOffset(int pixelOffset)
   {
      this.xPixelOffset = pixelOffset;
      this.yPixelOffset = pixelOffset;
   }

   public void setxPixelOffset(int xPixelOffset)
   {
      this.xPixelOffset = xPixelOffset;
   }

   public void setyPixelOffset(int yPixelOffset)
   {
      this.yPixelOffset = yPixelOffset;
   }

   public String getText()
   {
      return text;
   }

   public void setText(String text)
   {
      this.text = text;
   }

   public double getX()
   {
      return x1;
   }

   public double getY()
   {
      return y1;
   }

   public void setFontSize(int size)
   {
      font = new Font(Font.SANS_SERIF, Font.PLAIN, size);
   }

   /**
    * Must provide a draw method for plotter to render artifact
    */
   @Override
   public void draw(Graphics2DAdapter graphics)
   {
      graphics.setColor(color);
      graphics.setFont(font);

      tempPoint.set(x1, y1);
      graphics.drawString(text, tempPoint);
   }

   @Override
   public void drawLegend(Plotter2DAdapter graphics, Point2d origin)
   {
      graphics.setColor(color);
      graphics.setFont(font);

      tempPoint.set(origin.getX() - 30.0, origin.getY() + 6.0);
      graphics.drawString(graphics.getScreenFrame(), text, tempPoint);
   }

   public void save(PrintWriter printWriter)
   {
      printWriter.println(x1 + " " + y1 + " " + id);
   }

   public TextArtifact getCopy()
   {
      TextArtifact cirlceCopy = new TextArtifact(this.getID(), this.text, x1, y1);
      cirlceCopy.setColor(this.getColor());

      return cirlceCopy;
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

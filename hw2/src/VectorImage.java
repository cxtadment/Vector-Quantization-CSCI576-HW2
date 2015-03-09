import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;

public class VectorImage {
	
	public static BufferedImage displayOriginal(String fileName, int width, int height){
		
		  BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		    try {
			    File file = new File(fileName);
			    InputStream is1 = new FileInputStream(file);
			    InputStream is2 = new FileInputStream(file);
			    InputStream is3 = new FileInputStream(file);
			    Vector<InputStream> inputStreams = new Vector<InputStream>();
				inputStreams.add(is1);
				inputStreams.add(is2);
				inputStreams.add(is3);

				Enumeration<InputStream> enu = inputStreams.elements();
				SequenceInputStream sis = new SequenceInputStream(enu);
			    
			    long len = file.length();
			    byte[] bytes = new byte[(int)len*3];
			    
			    int offset = 0;
			    int numRead = 0;
			    while (offset < (bytes.length) && (numRead=sis.read(bytes, offset, bytes.length-offset)) >= 0) {
		          offset += numRead;
			    }
		    
		    		
		    	int ind = 0;
				  for(int y = 0; y < height; y++){
			
					for(int x = 0; x < width; x++){
				 
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
						
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
				
				
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    
		    return img;
	}
	
	 public static void main(String[] args) throws IOException {
	    	
	    	String fileName = args[0];
	    	
	    	int width = 352;
	    	int height = 288;
	       
		    // Use a panel and label to display the image
		    JPanel  panel = new JPanel ();
		    panel.add (new JLabel (new ImageIcon (displayOriginal(fileName, width, height))));
		    
		    JFrame frame = new JFrame("Display images");
		    
		    frame.getContentPane().add (panel);
		    frame.pack();
		    frame.setVisible(true);
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
		
		 }
}

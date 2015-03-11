
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

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
	
	public static BufferedImage displayVectorCompress(int numVector, String fileName, int width, int height){
		 BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		 double trans[][] = new double[height][width];

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
		    
		    	//initialization	
		    	int ind = 0;
				  for(int j = 0; j < height; j++){
			
					for(int i = 0; i < width; i++){
				 
						byte a = 0;
						byte br = bytes[ind];
						byte bg = bytes[ind+height*width];
						byte bb = bytes[ind+height*width*2]; 
						
						int r = br & 0xff;
					    int g = bg & 0xff;
					    int b = bb & 0xff;
					    
					    int y = r;
					    trans[j][i] = y;

						ind++;
					}
				}
				  
				double newtrans[][] = vectorQuantization(trans, numVector, height, width);
				for(int j=0; j<height; j++){
					for(int i=0; i<width; i++){
						int a = 0;
						int y = (int)newtrans[j][i];
						int pix = ((a << 24) + (y << 16) + (y << 8) + y);
						img.setRGB(i,j,pix);
					}
				}
				
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    
		    return img;
	}
	
	private static double[][] vectorQuantization(double[][] trans, int numVector, int height, int width) {
		
		double[][] newtrans = new double[height][width];
		List<Point> points = new ArrayList<Point>();
		
		//convert double array into List<Point> two dimension
		for(int j=0; j<trans.length; j++){
			for(int i=0; i<trans[j].length; i+=2){
				Point p = new Point();
				p.setX(trans[j][i]);
				p.setY(trans[j][i+1]);
				points.add(p);
			}
		}
		
		//initial centroids
		int num = (int) (Math.log(numVector)/Math.log(2));
		double perlength = (double)256/(double)(2*num);
		int allnum = num*num;
		List<Centroid> centroids = new ArrayList<Centroid>();
		for(int m=1; m<=num; m++){
			for(int n=1; n<=num; n++){
				Centroid centroid = new Centroid();
				centroid.setX(((n*2-1)*perlength)-1);
				centroid.setY(((m*2-1)*perlength)-1);
				centroids.add(centroid);
			}
		}
		
		//doing kmeans (core) and get new List<Point> back
		List<Point> newPoints = kmeans(centroids, points);
		
		//covert List<points> to double[][]
		int k = 0;
		for(int j=0; j<newtrans.length; j++){
			for(int i=0; i<newtrans[j].length;i+=2){
				newtrans[j][i] = newPoints.get(k).getX();
				newtrans[j][i+1] = newPoints.get(k).getY();
				k++;
			}
		}
		
		return newtrans;
	}


	 

	private static List<Point> kmeans(List<Centroid> centroids, List<Point> points) {
        
		final double bigNumber = Math.pow(10, 10);  
        double minimum = bigNumber; 
        boolean moving = true;
		
        while(moving){

        	//make the points connect to its vector according to distance
        	for(int i=0; i<points.size();i++){
        		minimum = bigNumber;
        		int clusterindex = 0;
        		for(int j=0; j<centroids.size();j++){
        			double distance = dist(points.get(i), centroids.get(j));
        			if(distance<minimum){
        				minimum = distance;
        				clusterindex = j;
        			}
        		}
        		points.get(i).setCluster(clusterindex);
        	}
            // calculate new centroids.
            for(int i = 0; i < centroids.size(); i++){
                int totalX = 0;
                int totalY = 0;
                int totalInCluster = 0;
                for(int j = 0; j < points.size(); j++){
                    if(points.get(j).getCluster() == i){
                        totalX += points.get(j).getX();
                        totalY += points.get(j).getY();
                        totalInCluster++;
                    }
                }
                if(totalInCluster > 0){
                    centroids.get(i).setX(totalX/totalInCluster);
                    centroids.get(i).setY(totalY/totalInCluster);

                }
            }
            
            moving = false;
            double totaldiff = 0;
            //judge moving or not by calculating mean quantization error
            for(int i=0; i<points.size();i++){
            	double diffx = points.get(i).getX() - centroids.get(points.get(i).getCluster()).getX();
            	double diffy = points.get(i).getY() - centroids.get(points.get(i).getCluster()).getY();
            	double diffxy = Math.abs(diffx)+Math.abs(diffy);
            	totaldiff += diffxy;
            }
            
            
            
            if((totaldiff/points.size())>20){
            	moving = true;
            }
        }
        
        for(int i=0;i<points.size();i++){
        	for(int j=0;j<centroids.size();j++){
        		if(points.get(i).getCluster()==j){
        			points.get(i).setX(centroids.get(j).getX());
        			points.get(i).setY(centroids.get(j).getY());
        		}
        	}
        }
		return points;
	}

	private static double dist(Point p, Centroid c){
        return Math.sqrt(Math.pow((c.getY() - p.getY()), 2) + Math.pow((c.getX() - p.getX()), 2));
    }

	public static void main(String[] args) throws IOException {
	    	
	    	String fileName = args[0];
	    	int numVector = Integer.parseInt(args[1]);
	    	
	    	int width = 352;
	    	int height = 288;
	       
		    // Use a panel and label to display the image
		    JPanel  panel = new JPanel ();
		    panel.add (new JLabel (new ImageIcon (displayOriginal(fileName, width, height))));
		    panel.add (new JLabel (new ImageIcon (displayVectorCompress(numVector, fileName, width, height))));
		    JFrame frame = new JFrame("Display images");
		    
		    frame.getContentPane().add (panel);
		    frame.pack();
		    frame.setVisible(true);
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
		
		 }
}

class Centroid{
	private double x;
	private double y;
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public Centroid(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Centroid(){
		
	}
}

class Point{
	private double x;
	private double y;
	private int cluster;
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public int getCluster() {
		return cluster;
	}
	public void setCluster(int cluster) {
		this.cluster = cluster;
	}
	
	public Point(){
		
	}
	
	public Point(double x, double y, int cluster){
		this.x = x;
		this.y = y;
		this.cluster = cluster;
	}

}
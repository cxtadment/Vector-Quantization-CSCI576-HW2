import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

public class VectorImageNew {
	
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
					    
					    //get Y
					    double y = (int) (r*0.299 + g*0.587 +b*0.114);
					    if(y<0){
					    	y=0;
					    }else if(y>255){
					    	y=255;
					    }
					    trans[j][i] = y;
						ind++;
					}
				}
				double newtrans[][] = vectorQuantization(trans, numVector, width, height);
				//displayimg
				for(int m=0; m<newtrans.length; m++){
					for(int n=0; n<newtrans[m].length; n++){
						double newy = newtrans[m][n];
						int newr = (int) (newy*0.999);
				    	int newg = (int) (newy*1.000);
				    	int newb = (int) (newy*1.000); 
				    	
				    	int pix = ((0 << 24) + (newr << 16) + (newg << 8) + newb);
				    	
				    	img.setRGB(n,m,pix);
					}
				}
				
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    
		    return img;
	}
	
	 private static double[][] vectorQuantization(double[][] trans, int numVector, int width, int height ) {
		
		double[][] dataDetails = new double[width*height/2][2];
		
		int k = 0;
		//create vector space
		for(int j=0; j<trans.length; j++){
			for(int i=0; i<trans[j].length; i+=2){
				dataDetails[k][0] = trans[j][i];
				dataDetails[k][1] = trans[j][i+1];
				k++;
			}
		}
		
		//initial centroids according to the input
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
		
		
		KMean kmean = new KMean();
		int num_clusters = num*num;
		int total_data = width*height/2;
		double samples[][] = dataDetails;
		kmean.initialize(centroids);
		List<Data> results=kmean.kMeanCluster(num_clusters, total_data, samples);
		
		
		//covert results 
		int count = 0;
		for(int j=0; j<trans.length; j++){
			for(int i=0; i<trans[j].length; i+=2){
				trans[j][i] = results.get(count).X();
				trans[j][i+1] = results.get(count).Y();
			}
		}
		
		return trans;
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
	public Centroid() {
		
	}
	public Centroid(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	
	
}

class Data
{
    private double mX = 0;
    private double mY = 0;
    private int mCluster = 0;
    
    public Data()
    {
        return;
    }
    
    public Data(double x, double y)
    {
        this.X(x);
        this.Y(y);
        return;
    }
    
    public void X(double x)
    {
        this.mX = x;
        return;
    }
    
    public double X()
    {
        return this.mX;
    }
    
    public void Y(double y)
    {
        this.mY = y;
        return;
    }
    
    public double Y()
    {
        return this.mY;
    }
    
    public void cluster(int clusterNumber)
    {
        this.mCluster = clusterNumber;
        return;
    }
    
    public int cluster()
    {
        return this.mCluster;
    }
}

class KMean{
	
	
    
    private static ArrayList<Data> dataSet = new ArrayList<Data>();
    private static ArrayList<Centroid> centroids = new ArrayList<Centroid>();
    
    public static void initialize(List<Centroid> inputs)
    {
        System.out.println("Centroids initialized at:");
        for(int i=0; i<inputs.size();i++){
        	centroids.add(inputs.get(i)); 
            System.out.println("     (" + centroids.get(i).getX() + ", " + centroids.get(i).getY() + ")");
            System.out.print("\n");
        }  
        return;
    }
    
    public static List<Data> kMeanCluster(int num_clusters, int total_data, double samples[][])
    {
    	System.out.println("caonima: "+centroids.size());
        final double bigNumber = Math.pow(10, 10);    // some big number that's sure to be larger than our data range.
        double minimum = bigNumber;                   // The minimum value to beat. 
        double distance = 0.0;                        // The current minimum value.
        int sampleNumber = 0;
        int cluster = 0;
        boolean isStillMoving = true;
        Data newData = null;
        
        // Add in new data, one at a time, recalculating centroids with each new one. 
        while(dataSet.size() < total_data)
        {
            newData = new Data(samples[sampleNumber][0], samples[sampleNumber][1]);
            dataSet.add(newData);
            minimum = bigNumber;
            for(int i = 0; i < num_clusters; i++)
            {
                distance = dist(newData, centroids.get(i));
                if(distance < minimum){
                    minimum = distance;
                    cluster = i;
                }
            }
            newData.cluster(cluster);
            
            // calculate new centroids.
            for(int i = 0; i < num_clusters; i++)
            {
                int totalX = 0;
                int totalY = 0;
                int totalInCluster = 0;
                for(int j = 0; j < dataSet.size(); j++)
                {
                    if(dataSet.get(j).cluster() == i){
                        totalX += dataSet.get(j).X();
                        totalY += dataSet.get(j).Y();
                        totalInCluster++;
                    }
                }
                if(totalInCluster > 0){
                    centroids.get(i).setX(totalX / totalInCluster);
                    centroids.get(i).setY(totalY / totalInCluster);
                }
            }
            sampleNumber++;
        }
        
        // Now, keep shifting centroids until equilibrium occurs.
        while(isStillMoving)
        {
            // calculate new centroids.
            for(int i = 0; i < num_clusters; i++)
            {
                int totalX = 0;
                int totalY = 0;
                int totalInCluster = 0;
                for(int j = 0; j < dataSet.size(); j++)
                {
                    if(dataSet.get(j).cluster() == i){
                        totalX += dataSet.get(j).X();
                        totalY += dataSet.get(j).Y();
                        totalInCluster++;
                    }
                }
                if(totalInCluster > 0){
                    centroids.get(i).setX(totalX / totalInCluster);
                    centroids.get(i).setY(totalY / totalInCluster);
                }
            }
            
            // Assign all data to the new centroids
            isStillMoving = false;
            
            for(int i = 0; i < dataSet.size(); i++)
            {
                Data tempData = dataSet.get(i);
                minimum = bigNumber;
                for(int j = 0; j < num_clusters; j++)
                {
                    distance = dist(tempData, centroids.get(j));
                    if(distance < minimum){
                        minimum = distance;
                        cluster = j;
                    }
                }
                tempData.cluster(cluster);
                if(tempData.cluster() != cluster){
                    tempData.cluster(cluster);
                    isStillMoving = true;
                }
            }
        }
        
        for(int i = 0; i < num_clusters; i++){
            for(int j = 0; j < total_data; j++){
                if(dataSet.get(j).cluster() == i){
                	dataSet.get(j).X(centroids.get(i).getX());
                	dataSet.get(j).Y(centroids.get(i).getY());
                }
            } 
        } 
        return dataSet;
    }
    
    /**
     * // Calculate Euclidean distance.
     * @param d - Data object.
     * @param c - Centroid object.
     * @return - double value.
     */
    private static double dist(Data d, Centroid c)
    {
        return Math.sqrt(Math.pow((c.getY() - d.Y()), 2) + Math.pow((c.getX() - d.X()), 2));
    }
    
    
    
    public static List<Centroid> returnCentroid(){
		return centroids;
    	
    }
    
}

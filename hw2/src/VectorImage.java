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
				  
				double newtrans[][] = vectorQuantization(trans, numVector);
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
	
	 private static double[][] vectorQuantization(double[][] trans, int numVector) {
		 
		List<VectorItem> vectorItems = new ArrayList<VectorItem>();
		
		int k = 0;
		//create vector space
		for(int j=0; j<trans.length; j++){
			for(int i=0; i<trans[j].length; i+=2){
				VectorItem vectorItem = new VectorItem();
				vectorItem.setX(trans[j][i]);
				vectorItem.setY(trans[j][i+1]);
				vectorItems.add(vectorItem);
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
		
		//doing quantization
		List<VectorItem> newVectorItems = quantizationProcessor(vectorItems, centroids);
		
		for(int count=0; count<newVectorItems.size(); count++){
			for(int j=0; j<trans.length; j++){
				for(int i=0; i<trans[j].length; i+=2){
					trans[j][i] = newVectorItems.get(count).getX();
					trans[j][i+1] = newVectorItems.get(count).getY();
				}
			}
		}
		
		return trans;
	}
	 
	

	private static List<VectorItem> quantizationProcessor(List<VectorItem> vectorItems, List<Centroid> centroids) {
		
		List<VectorItem> newVectorItems = new ArrayList<VectorItem>();
		
		List<CodeWord> codeWords = new ArrayList<CodeWord>();
		
		//initial codeWords
		//有问题
		for(int w=0; w<centroids.size(); w++){
			CodeWord codeWord = new CodeWord();
			codeWord.setX(centroids.get(w).getX());
			codeWord.setY(centroids.get(w).getY());
			codeWords.add(codeWord);
		}
		
		double difference = 100000000.0; 
		while(difference>100){	
			//distance	
			for(int j=0; j<vectorItems.size(); j++){
				double[] dis = new double[centroids.size()];
				for(int i=0; i<centroids.size(); i++){
					double disx = vectorItems.get(j).getX()-centroids.get(i).getX();
					double disy = vectorItems.get(j).getY()-centroids.get(i).getY();
					dis[i] = Math.sqrt(disx*disx+disy*disy);
						
				}
				double min = 100000000;
				int minindex = 0;
				for(int dcount=0; dcount<dis.length; dcount++){
					if(dis[dcount]<min){
						min = dis[dcount];
						minindex = dcount;
					}
				}
				codeWords.get(minindex).getVectors().add(vectorItems.get(j));
					
			}
			
			
			//core, update position
			for(int m=0; m<codeWords.size(); m++){
				
				double amountx = 0;
				double amounty = 0;
				for(int n=0; n<codeWords.get(m).getVectors().size();n++){
					amountx += codeWords.get(m).getVectors().get(n).getX();
					amounty += codeWords.get(m).getVectors().get(n).getY();
				}
				double sizec = codeWords.get(m).getVectors().size();
				double x = amountx/sizec;
				double y = amounty/sizec;
				centroids.get(m).setX(x);
				centroids.get(m).setY(y);
				
			}
			
			
			
			//calculate difference
			//有问题
			double amountdiff = 0;
			for(int p=0; p<centroids.size();p++){
				double xdiff = centroids.get(p).getX()-codeWords.get(p).getX();
				double ydiff = centroids.get(p).getY()-codeWords.get(p).getY();
				double diff = Math.sqrt(xdiff*xdiff+ydiff*ydiff);
				amountdiff += diff;
				
			}
			
			difference = amountdiff;
//			System.out.print("difference:"+difference);
//			
		}
		
		//process return
		double min = 1000000000;
		for(int j=0; j<vectorItems.size(); j++){
				for(int i=0; i<centroids.size(); i++){
					double disx = vectorItems.get(j).getX()-centroids.get(i).getX();
					double disy = vectorItems.get(j).getY()-centroids.get(i).getY();
					double dis = Math.sqrt(disx*disx+disy*disy);
					if(dis<min){
						min = dis;
						vectorItems.get(j).setX(centroids.get(i).getX());
						vectorItems.get(j).setY(centroids.get(i).getY());
					}
				}
		}
		return newVectorItems;
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

class CodeWord{
	private double x;
	private double y;
	private ArrayList<VectorItem> vectors = new ArrayList<VectorItem>();
	
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
	public ArrayList<VectorItem> getVectors() {
		return vectors;
	}
	public void setVectors(ArrayList<VectorItem> vectors) {
		this.vectors = vectors;
	}
	public CodeWord(){
		
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
	
	
}

class VectorItem{
	
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
	
	public VectorItem(){
		
	}
	
}

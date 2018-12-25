public class Bubble{
	public static final int NONE = 0;
	public static final int BLUE = 1;
	public static final int YELLOW = 2;
	public static final int GREEN = 3;
	public static final int RED = 4;
	public static final int TOTAL_COLOR = 4;
	public static final int RADIUS = 20;
	
	public int xCenter;	// coordinate x
	public int yCenter;	// coordinare y
	public int color;
	
	public Bubble(int xCenter, int yCenter, int color){
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.color = color;
	}
	
	public boolean isCollide(Bubble other){
		int len = (int)(Math.sqrt((xCenter - other.xCenter) * (xCenter - other.xCenter) + 
			((yCenter - other.yCenter) * (yCenter - other.yCenter))));
		return len < Bubble.RADIUS * 2;
	}
	
	public static int getRandomColor(){
		return (int)(Math.random() * Bubble.TOTAL_COLOR) + 1;
	}
}
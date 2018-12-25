public class BubbleShot extends Bubble{
	public static final int DEFAULT_SPEED = 10;
	public int speed;
	public int degree;
	public boolean exist;
	public BubbleShot(int xCenter, int yCenter, int color, int speed, int degree){
		super(xCenter, yCenter, color);
		this.speed = speed;
		this.degree = degree;
	}
}
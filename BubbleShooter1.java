import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.awt.Graphics.*;
import java.awt.geom.*;

public class BubbleShooter1 extends JPanel{
	private int columns = 10;
	private int rows = 10;
	private int panelWidth = columns * 2 * Bubble.RADIUS  + Bubble.RADIUS;
	private int panelHeight = (rows+3) * 2 * Bubble.RADIUS + Bubble.RADIUS;
	private Bubble[][] bubbles = new Bubble[rows][columns];
	
	private int playerDegree = 90;
	private int playerLen = Bubble.RADIUS * 3;
	private int playerXCenter = panelWidth / 2;
	private int playerYCenter = (rows+2) * 2 * Bubble.RADIUS;
	
	private BubbleShot bubbleShot, currentBubble, nextBubble;
	private Timer gameTimer;
	private int increseLineCounter = 1;
	private int increseLineCounterMax = 500;
	
	boolean processed[][] = new boolean[rows][columns];	// mark for the processed node
	boolean res[][] = new boolean[rows][columns];	// mark for the removable
	
	public BubbleShooter1(){
		initBubble();
		
		bubbleShot = new BubbleShot(playerXCenter, playerYCenter, 0,
			bubbleShot.DEFAULT_SPEED, playerDegree);
		bubbleShot.exist = false;
		currentBubble = new BubbleShot(playerXCenter, playerYCenter, Bubble.getRandomColor(), 
			BubbleShot.DEFAULT_SPEED, 0);
		nextBubble = new BubbleShot(playerXCenter, playerYCenter + Bubble.RADIUS * 2, Bubble.getRandomColor(), 
			BubbleShot.DEFAULT_SPEED, 0);
		
		gameTimer = new Timer(20, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (isLost())
					gameTimer.stop();
				onUpdate();
				repaint();
			}
		});
		
		gameTimer.start();
		
		addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				int x = e.getX();
				int y = e.getY();
				double len = Math.sqrt((x - playerXCenter) * (x - playerXCenter) + 
					(y - playerYCenter) * (y - playerYCenter));
				double rad = Math.acos((x - playerXCenter) / len);
				playerDegree = (int)Math.toDegrees(rad);
				if (playerDegree < 8)
					playerDegree = 8;
				if (playerDegree > 172)
					playerDegree = 172;
			}
		});
		
		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if (bubbleShot.exist)
					return;
				
				int x = e.getX();
				int y = e.getY();
				double len = Math.sqrt((x - playerXCenter) * (x - playerXCenter) + 
					(y - playerYCenter) * (y - playerYCenter));
				double rad = Math.acos((x - playerXCenter) / len);
				playerDegree = (int)Math.toDegrees(rad);
				if (playerDegree < 8)
					playerDegree = 8;
				if (playerDegree > 172)
					playerDegree = 172;
				
				bubbleShot.xCenter = currentBubble.xCenter;
				bubbleShot.yCenter = currentBubble.yCenter;
				bubbleShot.color = currentBubble.color;
				bubbleShot.exist = true;
				bubbleShot.degree = playerDegree;
				
				currentBubble.color = nextBubble.color;
				nextBubble.color = Bubble.getRandomColor();
			}
		});
	}
	
	public boolean isLost(){
		for (int i = 0; i < columns; i++){
			if (bubbles[rows-1][i].color != Bubble.NONE)
				return true;
		}
		return false;
	}
	
	public void onUpdate(){
		// wall collide detection and snaping bubble if it touches the roof
		boolean isSnap = false;
		touchWall();
		if(touchRoof()){
			isSnap = true;
			bubbleShot.exist = false;
			snapBubble();
		}
		
		// move bubbles
		if (bubbleShot.exist)
			moveBubble();
		
		if (bubbleShot.exist && isCollideWithBall()){
			// add bubble to grid
			snapBubble();
			isSnap = true;
		}
		
		findCluster(0, 0, false, true, true);	// remove floating bubbles
		
		if (isSnap == true){
			int cor[] = locateGrid(bubbleShot.xCenter, bubbleShot.yCenter);
			findCluster(cor[0], cor[1], true, true, true);
		}
		
		if (increseLineCounter % increseLineCounterMax == 0)
			increseLine();
		increseLineCounter = (increseLineCounter + 1) % increseLineCounterMax;
	}
	
	public int[] locateGrid(int xCenter, int yCenter){
		int[] res = new int[2];
		int y = (int)Math.floor(yCenter / (Bubble.RADIUS * 2));

		int tmpX = xCenter;
		if (y % 2 != 0)
			tmpX -= Bubble.RADIUS;
		int x = (int)Math.floor(tmpX / (Bubble.RADIUS * 2));
		res[0] = y;
		res[1] = x;
		//System.out.println(bubbleShot.xCenter + " " +bubbleShot.yCenter);
		//System.out.println("i, j: " + y + " " + x);
		return res;
	}
	
	public void findCluster(int r, int c, boolean findCluster, boolean findFloat, boolean remove){
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++){
				processed[i][j] = false;
				res[i][j] = false;
			}
		
		if (findCluster){
			int count = findAll(r, c, bubbles[r][c].color);
			//System.out.println("count: " + count);
			if (remove && count >= 3){
				for (int i = 0; i < rows; i++)
					for(int j = 0; j < columns; j++){
						if (res[i][j])
							bubbles[i][j].color = Bubble.NONE;
					}
			}
		}
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++){
				processed[i][j] = false;
				res[i][j] = false;
			}
		
		if (findFloat){
			for (int i = 0; i < rows; i++)
				for (int j = 0; j < columns; j++){
					initProcessed();
					res[i][j] = !isConnectedToRoof(i, j);
				}
				
			if (remove){
				for (int i = 0; i < rows; i++)
					for(int j = 0; j < columns; j++){
						if (res[i][j])
							bubbles[i][j].color = Bubble.NONE;
					}
			}
		}
	}
	
	public void initProcessed(){
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++){
				processed[i][j] = false;
			}
	}
	
	public boolean isConnectedToRoof(int r, int c){
		if (c < 0 || c >= columns || r >= rows || r < 0)
			return false;
		if (processed[r][c])
			return false;
		
		processed[r][c] = true;
		if (r == 0 && bubbles[r][c].color != Bubble.NONE)
			return true;

		if (bubbles[r][c].color == Bubble.NONE)
			return false;
		
		boolean isConnected = isConnectedToRoof(r-1, c) || isConnectedToRoof(r+1, c) ||
			isConnectedToRoof(r, c-1) || isConnectedToRoof(r, c+1);
		if (r % 2 != 0)
			isConnected = isConnected || isConnectedToRoof(r-1, c+1)  || isConnectedToRoof(r+1, c+1);
		else
			isConnected = isConnected || isConnectedToRoof(r-1, c-1) || isConnectedToRoof(r+1, c-1);
		
		return isConnected;
	}
	
	/** find all the bubbles with the same color */
	public int findAll(int r, int c, int color){
		if (r < 0 || r >= rows || c < 0 || c >= columns)
			return 0;
		if (processed[r][c])
			return 0;
		
		processed[r][c] = true;
		if (bubbles[r][c].color != color)
			return 0;
		
		res[r][c] = true;
		//System.out.println("i, j: " + r + " " + c);
		int count = 1;
		count += findAll(r-1, c, color) + findAll(r+1, c, color) + findAll(r, c-1,color) + findAll(r, c+1, color);
		if (r % 2 == 0){
			count += findAll(r-1, c-1, color);
			count += findAll(r+1, c-1, color);
		}
		else{
			count += findAll(r-1, c+1, color);
			count += findAll(r+1, c-1, color);
		}
		return count;
	}
	
	public boolean touchWall(){
		if (bubbleShot.exist && !isCollideWithBall()){
			// move bubbleShot
			if (bubbleShot.degree < 90 && bubbleShot.xCenter + Bubble.RADIUS + 
					bubbleShot.speed * Math.cos(bubbleShot.degree * 2 * Math.PI / 360) > panelWidth){
				bubbleShot.degree = 180 - bubbleShot.degree;
				bubbleShot.xCenter = panelWidth - Bubble.RADIUS;
				return true;
			}
			else if (bubbleShot.degree > 90 && bubbleShot.xCenter - Bubble.RADIUS + 
					bubbleShot.speed * Math.cos(bubbleShot.degree * 2 * Math.PI / 360) < 0){
				bubbleShot.degree = 180 - bubbleShot.degree;
				bubbleShot.xCenter = Bubble.RADIUS;
				return true;
			}
		}
		return false;
	}
	
	public boolean touchRoof(){
		if (bubbleShot.exist && !isCollideWithBall()){
			if (bubbleShot.yCenter - bubbleShot.speed * Math.sin(bubbleShot.degree * 2 * Math.PI / 360) < 0){
				return true;
			}
		}
		return false;
	}
	
	public void moveBubble(){
		bubbleShot.xCenter += (int)((double)bubbleShot.speed * Math.cos(bubbleShot.degree * 2 * Math.PI / 360));
		bubbleShot.yCenter -= (int)((double)bubbleShot.speed * Math.sin(bubbleShot.degree * 2 * Math.PI / 360));
	}
	
	public void snapBubble(){
		System.out.println(bubbleShot.xCenter + " " +bubbleShot.yCenter);
		int y = (int)Math.floor(bubbleShot.yCenter / (Bubble.RADIUS * 2));

		int tmpX = bubbleShot.xCenter;
		if (y % 2 != 0)
			tmpX -= Bubble.RADIUS;
		int x = (int)Math.floor(tmpX / (Bubble.RADIUS * 2));
		
		//int[] cor = locateGrid(bubbleShot.xCenter, bubbleShot.yCenter);
		if (bubbles[y][x].color == Bubble.NONE){
			bubbles[y][x].color = bubbleShot.color;
		}
		
		
		System.out.println("i, j: " + y + " " + x);
		bubbleShot.exist = false;
	}
	
	public boolean isCollideWithBall(){
		if (!bubbleShot.exist)
			return false;
		for (int i = 0; i < rows-1; i++){
			for (int j = 0; j < columns; j++){
				if (bubbles[i][j].color != Bubble.NONE && bubbles[i][j].isCollide(bubbleShot))
					return true;
			}
		}
		return false;
	}
	
	public void increseLine(){
		for (int i = rows-1; i > 0; i--){
			for (int j = 0; j < columns; j++){
				bubbles[i][j].color = bubbles[i-1][j].color;
			}
		}
		
		// generate a new line for first line
		for (int i = 0; i < columns; i++){
			bubbles[0][i] = new Bubble(2 * Bubble.RADIUS * i + Bubble.RADIUS, Bubble.RADIUS, Bubble.getRandomColor());
		}
	}
	
	public void initBubble(){
		// Initiate bubbles
		int i;
		
		for (i = 0; i < rows / 2; i++){
			int offset = (i % 2 != 0?Bubble.RADIUS:0);
			for (int j = 0; j < columns; j++){
				int color = Bubble.getRandomColor();
				bubbles[i][j] = new Bubble(offset + j * Bubble.RADIUS * 2 + Bubble.RADIUS, 
					i * Bubble.RADIUS * 2 + Bubble.RADIUS, color);
			}
		}
		for (; i < rows; i++){
			int offset = (i % 2 != 0?Bubble.RADIUS:0);
			for (int j = 0; j < columns; j++){
				bubbles[i][j] = new Bubble(offset + j * Bubble.RADIUS * 2 + Bubble.RADIUS, 
					i * Bubble.RADIUS * 2 + Bubble.RADIUS, Bubble.NONE);
			}
		}
	}
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		// draw player 
		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(5));
		int xOffset = (int)(playerLen * Math.cos(playerDegree * 2 * Math.PI / 360));
		int yOffset = (int)(playerLen * Math.sin(playerDegree * 2 * Math.PI / 360));
		g2.draw(new Line2D.Double(playerXCenter, playerYCenter, playerXCenter + xOffset, 
			playerYCenter - yOffset));
			
		// draw bubble shot
		if (bubbleShot.exist){
			setBubbleColor(bubbleShot.color, g);
			g.fillOval(bubbleShot.xCenter - Bubble.RADIUS, bubbleShot.yCenter - Bubble.RADIUS,
				Bubble.RADIUS * 2, Bubble.RADIUS * 2);
			//System.out.println(playerDegree + " " + bubbleShot.xCenter + " " + bubbleShot.yCenter);
		}
		
		// draw current and next bubbles
		setBubbleColor(currentBubble.color, g);
			g.fillOval(currentBubble.xCenter - Bubble.RADIUS, currentBubble.yCenter - Bubble.RADIUS,
				Bubble.RADIUS * 2, Bubble.RADIUS * 2);
		setBubbleColor(nextBubble.color, g);
			g.fillOval(nextBubble.xCenter - Bubble.RADIUS, nextBubble.yCenter - Bubble.RADIUS,
				Bubble.RADIUS * 2, Bubble.RADIUS * 2);
		
		
		// draw a steady dead line
		g2.setColor(Color.BLACK);
		for (int i = 0; i < panelWidth; i = i + 10)
			g2.draw(new Line2D.Double(i, 10 * 2 * Bubble.RADIUS, 
				i+3, 10 * 2 * Bubble.RADIUS));
				
		// draw bubbles
		drawBubbles(g);
	}
	
	/** draw bubbles */
	private void drawBubbles(Graphics g){
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < columns; j++){
				if (bubbles[i][j].color <= 0 || bubbles[i][j].color > Bubble.TOTAL_COLOR)
					continue;
				setBubbleColor(bubbles[i][j].color, g);
				Bubble tmp = bubbles[i][j];
				int radius = Bubble.RADIUS;
				g.fillOval(tmp.xCenter - Bubble.RADIUS, tmp.yCenter - Bubble.RADIUS, radius * 2, radius * 2);
			}
		}
	}
	
	private void setBubbleColor(int color, Graphics g){
		switch(color){
			case Bubble.RED:
				g.setColor(Color.RED);
				break;
			case Bubble.BLUE:
				g.setColor(Color.BLUE);
				break;
			case Bubble.YELLOW:
				g.setColor(Color.YELLOW);
				break;
			case Bubble.GREEN:
				g.setColor(Color.GREEN);
				break;
			default:
				break;
		}
	}
	
	public Dimension getPreferredSize(){
		return new Dimension(panelWidth, panelHeight);
	}
}
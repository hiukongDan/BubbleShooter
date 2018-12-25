import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TestBubbleShooter1 extends JFrame{
	public static void main(String[] args){
		TestBubbleShooter1 frame = new TestBubbleShooter1();
		frame.pack();
		frame.setTitle("Test BubbleSHooter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public TestBubbleShooter1(){
		add(new BubbleShooter1());
	}
}
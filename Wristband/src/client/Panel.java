package client;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

/**
 * Created by kevok on 3/10/15.
 */
public class Panel extends JPanel{
    JFrame frame;
    Baselining baseline;
    NetworkThread networkThread;

    JButton baseliningButton;
    JButton learningButton;
    JButton summaryButton;
    JSlider thresholdControl;
    JSpinner timeControl;

    JButton arduinoFail;

    int startingWidth = 800;
    int startingHeight = 600;
    
    public Panel(JFrame frame, Baselining baseline) {
        this.frame = frame;
        this.baseline = baseline;
        networkThread = baseline.getWristbandInterface();

        timeControl = new JSpinner(new SpinnerNumberModel(2,.5,10,.5));
        timeControl.setFont(new Font("Courier New", Font.PLAIN, 20));

        baseliningButton = new JButton("Start Baselining Phase");

        learningButton = new JButton("Start Learning Phase");

        /*Create Summary Button & Implementation for OnClick */
        summaryButton = new JButton("Summary");
        final Baselining b2 = baseline;
        summaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new Summary(new JFrame(), b2);
            }
        });

        /* Create Slider for adjusting Threshold */
        thresholdControl = new JSlider(JSlider.HORIZONTAL,0,100,50);
        thresholdControl.setMajorTickSpacing(10);
        thresholdControl.setMinorTickSpacing(1);
        thresholdControl.setPaintTicks(true);
        thresholdControl.setPaintLabels(true);

        /*Create button for Reload if Arduino Fails */
        arduinoFail = new JButton("Retry");
        arduinoFail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                networkThread.restartFailedToConnect();
                arduinoFail.invalidate();
                arduinoFail.setVisible(false);
            }
        });

        /*Add all Buttons & Inputs to the Form */
        this.setLayout(null);
        this.add(timeControl);
        this.add(baseliningButton);
        this.add(learningButton);
        this.add(summaryButton);
        this.add(thresholdControl);
        this.add(arduinoFail);
        
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(startingWidth, startingHeight);
        frame.setResizable(true);
        frame.setAlwaysOnTop(false);
        frame.setVisible(true);
        frame.setTitle("WristBand");
        frame.setBackground(Color.white);
        //green = new Color(5,128,0);

        frame.setMinimumSize(new Dimension(400,300));

        /*Function which is called upon window closing */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                networkThread.close();
            }
        });
    }

    /**
     * paint(Graphics g) is called repeatedly whenever possible to 
     * repaint the window until the user exits the window. Repaint can be 
     * called to force an update immediately.
    */
    @Override
    public void paint (Graphics g) {
        super.paint(g);

        /*check for Ardiuno on startup */
        int foundCom = networkThread.getFoundCom();
        if(foundCom == 0){ //still searching
            g.drawString("Loading",(int) (.25 * getWidth()), (int) (.4 * getHeight()));
        }
        else if(foundCom < 0){ //failed
            g.setFont(new Font("Times New Roman", Font.BOLD, 50));
            g.drawString("Failed to find Arduino!", (int) (.25 * getWidth()), (int) (.4 * getHeight()));
            arduinoFail.setVisible(true);
            arduinoFail.setBounds((int) (.25 * getWidth()), (int) (.55 * getHeight()), (int) (.35 * getWidth()), (int) (.13 * getHeight()));
        }
        else { //success

            LinkedList<DataPoint> values = baseline.getSessionData();
            g.setFont(new Font("Courier New", Font.PLAIN, 20));
            g.drawString("Data:", (int) (.06 * this.getWidth()), (int) (.4 * this.getHeight()));
            g.drawString("Sum:", (int) (.25 * this.getWidth()), (int) (.4 * this.getHeight()));
            //float sum = baseline.getSum();
            g.drawString(baseline.getSum() + "", (int) (.4 * this.getWidth()), (int) (.4 * this.getHeight()) + 30); //need to append "" to make the float a String
            g.drawString("Baseline:", (int) (.4 * this.getWidth()), (int) (.4 * this.getHeight()));
            //float avg = baseline.getBaseline();
            g.drawString(baseline.getBaseline() + "", (int) (.25 * this.getWidth()), (int) (.4 * this.getHeight()) + 30); //need to append "" to make the float a String
            LinkedList<DataPoint> top20 = new LinkedList<DataPoint>();
            for (int i = values.size() - 1; i > Math.max(0, values.size() - 20); i--) {
                top20.add(values.get(i));
            }
            int y = 0;
            for (DataPoint d : top20) {
                //this.setForeground(Color.BLUE);
                //g.setFont(g.getFont().setForeground(Color.BLUE));
                g.drawString(String.format(
                        "%7.2f: %5.2f",
                        d.getTime(),
                        Math.abs(d.getMagnitude())
                ), (int) (.01 * this.getWidth()), (int) (.45 * this.getHeight()) + y);
                y += 20;
            }

        /* Reset all buttons and inputs if window was resized */
            timeControl.setBounds((int) (.2 * getWidth()), (int) (.02 * getHeight()), (int) (.6 * getWidth()), (int) (.07 * getHeight()));
            baseliningButton.setBounds((int) (.05 * getWidth()), (int) (.1 * getHeight()), (int) (.42 * getWidth()), (int) (.1 * getHeight()));
            learningButton.setBounds((int) (.5 * getWidth()), (int) (.1 * getHeight()), (int) (.42 * getWidth()), (int) (.1 * getHeight()));
            summaryButton.setBounds((int) (.65 * getWidth()), (int) (.35 * getHeight()), (int) (.25 * getWidth()), (int) (.08 * getHeight()));
            summaryButton.setBounds((int) (.65 * getWidth()), (int) (.35 * getHeight()), (int) (.25 * getWidth()), (int) (.08 * getHeight()));
            thresholdControl.setBounds((int) (.04 * getWidth()), (int) (.24 * getHeight()), (int) (.9 * getWidth()), (int) (.08 * getHeight()));
            revalidate();
        }
    }

}
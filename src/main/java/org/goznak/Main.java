package org.goznak;

import org.goznak.Panels.MainPanel;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    MainPanel mainPanel;
    public Dimension defaultDimension = new Dimension(1150, 150);

    public static void main(String[] args){
        new Main("Trend recorder");
    }

    public Main(String title){
        super(title);
        mainPanel = new MainPanel(this, defaultDimension);
        add(mainPanel);
        setPreferredSize(defaultDimension);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        revalidate();
        pack();
        setVisible(true);
    }
}

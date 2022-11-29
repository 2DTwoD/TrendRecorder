package org.goznak.Instruments;

import javax.swing.*;
import java.awt.*;

public class VerticalLayout implements LayoutManager {

    final private Dimension size = new Dimension();
    final private int distanceX;
    final private int distanceY;
    final int align;
    static final public int LEFT = 0;
    static final public int CENTER = 1;
    JFrame panel;

    public VerticalLayout(JFrame panel, int distanceX, int distanceY, int align){
        this.panel = panel;
        this.distanceX = distanceX;
        this.distanceY = distanceY;
        this.align = align;
    }
    @Override
    public void addLayoutComponent(String name, Component comp) {
    }
    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container c) {
        return calculateBestSize(c);
    }

    @Override
    public Dimension minimumLayoutSize(Container c) {
        return calculateBestSize(c);
    }

    @Override
    public void layoutContainer(Container container) {
        Component[] list = container.getComponents();
        int currentX = distanceX;
        if (align == CENTER)
            currentX = panel.getSize().width / 2;

        int currentY = distanceY;
        int shiftingX = 0;
        for (Component component : list) {
            Dimension pref = component.getPreferredSize();
            if (align == CENTER)
                shiftingX = pref.width / 2;
            component.setBounds(currentX - shiftingX, currentY, pref.width, pref.height);
            currentY += distanceY;
            currentY += pref.height;
        }
        container.revalidate();
    }

    private Dimension calculateBestSize(Container c){
        Component[] list = c.getComponents();
        int maxWidth = 0;
        for (Component component : list) {
            int width = component.getWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        size.width = maxWidth;
        int height = 0;
        for (Component component : list) {
            height += distanceY;
            height += component.getHeight();
        }
        size.height = height;
        return size;
    }
}

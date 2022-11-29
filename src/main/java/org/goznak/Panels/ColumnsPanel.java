package org.goznak.Panels;

import javax.swing.*;
import java.awt.*;

public class ColumnsPanel extends JPanel {

    Dimension[] rowDim;
    JLabel nameLabel = new JLabel("Имя тренда", JLabel.CENTER);
    JLabel areaLabel = new JLabel("Область", JLabel.CENTER);
    JLabel dbLabel = new JLabel("N DB", JLabel.CENTER);
    JLabel byteLabel = new JLabel("Байт", JLabel.CENTER);
    JLabel bitLabel = new JLabel("Бит", JLabel.CENTER);
    JLabel typeLabel = new JLabel("Тип", JLabel.CENTER);
    JLabel periodLabel = new JLabel("Период", JLabel.CENTER);
    JLabel applyLabel = new JLabel("Применить", JLabel.CENTER);
    JLabel checkLabel = new JLabel("Проверить", JLabel.CENTER);
    JLabel valueLabel = new JLabel("Значение", JLabel.CENTER);
    JLabel startLabel = new JLabel("Пуск/стоп", JLabel.CENTER);
    JLabel statusLabel = new JLabel("Статус", JLabel.CENTER);
    JLabel deleteLabel = new JLabel("Удал.", JLabel.CENTER);
    static int numOfSize = 0;

    public ColumnsPanel(LayoutManager layout){
        super(layout);
        rowDim = MainPanel.rowDim;
        setParameters(nameLabel);
        setParameters(areaLabel);
        setParameters(dbLabel);
        setParameters(byteLabel);
        setParameters(bitLabel);
        setParameters(typeLabel);
        setParameters(periodLabel);
        setParameters(applyLabel);
        setParameters(checkLabel);
        setParameters(valueLabel);
        setParameters(startLabel);
        setParameters(statusLabel);
        setParameters(deleteLabel);
        add(nameLabel);
        add(areaLabel);
        add(dbLabel);
        add(byteLabel);
        add(bitLabel);
        add(typeLabel);
        add(periodLabel);
        add(applyLabel);
        add(checkLabel);
        add(valueLabel);
        add(startLabel);
        add(statusLabel);
        add(deleteLabel);
    }

    private void setParameters(JLabel label){
        label.setPreferredSize(rowDim[numOfSize++]);
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }
}

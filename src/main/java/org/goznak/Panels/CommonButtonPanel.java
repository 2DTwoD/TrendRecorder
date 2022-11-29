package org.goznak.Panels;

import org.goznak.Instruments.Dialogs;
import org.goznak.Instruments.PLCConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommonButtonPanel extends JPanel {

    Dimension[] rowDim;
    MainPanel mainPanel;
    PLCConnection plcConnection;
    JLabel plug = new JLabel();
    JButton applyAllButton = new JButton("Прим. все");
    JButton checkAllButton = new JButton("Пров. все");
    JButton startAllButton = new JButton("Пуск всех");
    JButton stopAllButton = new JButton("Стоп всех");
    JButton deleteAllButton = new JButton("Удал. все");
    ScheduledExecutorService runtimeExecutor;

    CommonButtonPanel(MainPanel mainPanel, PLCConnection plcConnection, LayoutManager layout){
        super(layout);
        this.mainPanel = mainPanel;
        this.plcConnection = plcConnection;
        rowDim = MainPanel.rowDim;
        Dimension labelSize = new Dimension(0, rowDim[0].height);
        for(int i = 0; i < 7; i++){
            labelSize.width += rowDim[i].width;
        }
        int gap = ((FlowLayout) layout).getHgap();
        labelSize.width += 6 * gap;
        plug.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        plug.setPreferredSize(labelSize);
        Dimension buttonSize = new Dimension(0, rowDim[0].height);
        for(int i = 9; i < rowDim.length; i++){
            buttonSize.width += rowDim[i].width + gap;
        }
        buttonSize.width /= 3;
        buttonSize.width -= gap;
        applyAllButton.setPreferredSize(rowDim[7]);
        checkAllButton.setPreferredSize(rowDim[7]);
        startAllButton.setPreferredSize(buttonSize);
        stopAllButton.setPreferredSize(buttonSize);
        deleteAllButton.setPreferredSize(buttonSize);
        add(plug);
        add(applyAllButton);
        add(checkAllButton);
        add(startAllButton);
        add(stopAllButton);
        add(deleteAllButton);
        applyAllButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(Dialogs.confirmDialog("Применить настройки ко всем переменным?") != 0){
                    return;
                }
                mainPanel.applyAllRecords();
            }
        });
        checkAllButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mainPanel.checkAllVars();
            }
        });
        startAllButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!plcConnection.getConnectFlag() || Dialogs.confirmDialog("Запустить на запись все переменные?") != 0){
                    return;
                }
                mainPanel.startAllRecords();
            }
        });
        stopAllButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(Dialogs.confirmDialog("Остановить запись всех переменных?") != 0){
                    return;
                }
                mainPanel.stopAllRecords();
            }
        });
        deleteAllButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(Dialogs.confirmDialog("Удалить все переменные?") != 0){
                    return;
                }
                mainPanel.deleteAllRecords();
            }
        });
        runtimeExecutor = Executors.newSingleThreadScheduledExecutor();
        runtimeExecutor.scheduleAtFixedRate(this::runTimeUpdate, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void activeButtonsVisualisation(Color color){
        startAllButton.setForeground(color);
        checkAllButton.setForeground(color);
    }

    private void runTimeUpdate(){
        if(plcConnection.connected()){
            activeButtonsVisualisation(Color.BLACK);
        } else {
            activeButtonsVisualisation(Color.LIGHT_GRAY);
        }
    }

    public void shutdownRuntime(){
        runtimeExecutor.shutdown();
    }
}

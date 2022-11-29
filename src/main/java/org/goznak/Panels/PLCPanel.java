package org.goznak.Panels;

import org.goznak.Instruments.JTextFieldLimit;
import org.goznak.Instruments.PLCConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PLCPanel extends JPanel {

    JLabel PLCipLabel = new JLabel("IP адрес ПЛК Siemens (S7-300/400/1200/1500): ");
    JTextField octField1 = new JTextField();
    JTextField octField2 = new JTextField();
    JTextField octField3 = new JTextField();
    JTextField octField4 = new JTextField();
    JTextField rackField = new JTextField();
    JTextField slotField = new JTextField();
    JButton applyButton = new JButton("Применить");
    JButton connectButton = new JButton("Подключиться");
    JButton disconnectButton = new JButton("Отключиться");
    JLabel connectionLabel = new JLabel("?", JLabel.CENTER);
    String oct1 = "";
    String oct2 = "";
    String oct3 = "";
    String oct4 = "";
    int rack = 0;
    int slot = 1;
    Dimension small = new Dimension(28, 20);
    Dimension middle = new Dimension(100, 20);
    Dimension big = new Dimension(150, 20);
    ScheduledExecutorService runtimeExecutor;
    PLCConnection plcConnection;
    MainPanel mainPanel;

    public PLCPanel(MainPanel mainPanel, PLCConnection plcConnection, LayoutManager layout){
        super(layout);
        this.mainPanel = mainPanel;
        this.plcConnection = plcConnection;
        octField1.setPreferredSize(small);
        octField2.setPreferredSize(small);
        octField3.setPreferredSize(small);
        octField4.setPreferredSize(small);
        rackField.setPreferredSize(small);
        slotField.setPreferredSize(small);
        applyButton.setPreferredSize(middle);
        connectButton.setPreferredSize(big);
        disconnectButton.setPreferredSize(big);
        connectionLabel.setPreferredSize(middle);
        octField1.setDocument(new JTextFieldLimit(3));
        octField2.setDocument(new JTextFieldLimit(3));
        octField3.setDocument(new JTextFieldLimit(3));
        octField4.setDocument(new JTextFieldLimit(3));
        rackField.setDocument(new JTextFieldLimit(3));
        slotField.setDocument(new JTextFieldLimit(3));
        runtimeExecutor = Executors.newSingleThreadScheduledExecutor();
        runtimeExecutor.scheduleAtFixedRate(this::runTimeUpdate, 0, 100, TimeUnit.MILLISECONDS);
        connectionLabel.setBackground(Color.LIGHT_GRAY);
        connectionLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        connectionLabel.setOpaque(true);
        applyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                applyFields();
            }
        });
        rackField.setText("0");
        slotField.setText("1");

        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                plcConnection.connect();
            }
        });
        disconnectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                plcConnection.disconnect();
                mainPanel.stopAllRecords();
            }
        });
        add(PLCipLabel);
        add(octField1);
        add(new JLabel("."));
        add(octField2);
        add(new JLabel("."));
        add(octField3);
        add(new JLabel("."));
        add(octField4);
        add(new JLabel("Рейка:"));
        add(rackField);
        add(new JLabel("Слот:"));
        add(slotField);
        add(applyButton);
        add(connectButton);
        add(disconnectButton);
        add(connectionLabel);
        applyFields();
        SwingUtilities.invokeLater(this::revalidate);
    }

    void runTimeUpdate(){
        MainPanel.checkField(octField1, oct1);
        MainPanel.checkField(octField2, oct2);
        MainPanel.checkField(octField3, oct3);
        MainPanel.checkField(octField4, oct4);
        MainPanel.checkField(rackField, String.valueOf(rack));
        MainPanel.checkField(slotField, String.valueOf(slot));
        if(plcConnection.getConnectFlag()) {
            connectButton.setForeground(Color.LIGHT_GRAY);
            disconnectButton.setForeground(Color.BLACK);
        } else {
            connectButton.setForeground(Color.BLACK);
            disconnectButton.setForeground(Color.LIGHT_GRAY);
        }
        switch (plcConnection.getLinkStatus()){
            case PLCConnection.BAD -> {
                connectionLabel.setText("X");
                connectionLabel.setBackground(Color.RED);
            }
            case PLCConnection.GOOD -> {
                connectionLabel.setText(plcConnection.getPlcStatus());
                connectionLabel.setBackground(Color.GREEN);
            }
            case PLCConnection.UNKNOWN -> {
                connectionLabel.setText("?");
                connectionLabel.setBackground(Color.LIGHT_GRAY);
            }
        }
        if(plcConnection.getConnectFlag()) {
            applyButton.setForeground(Color.LIGHT_GRAY);
        } else {
            applyButton.setForeground(Color.BLACK);
        }
    }

    void applyFields(){
        if(plcConnection.connecting() || plcConnection.getConnectFlag()){
            return;
        }
        oct1 = getValueIP(octField1);
        oct2 = getValueIP(octField2);
        oct3 = getValueIP(octField3);
        oct4 = getValueIP(octField4);
        rack = MainPanel.getValue(rackField);
        slot = MainPanel.getValue(slotField);
        octField1.setText(oct1);
        octField2.setText(oct2);
        octField3.setText(oct3);
        octField4.setText(oct4);
        rackField.setText(String.valueOf(rack));
        slotField.setText(String.valueOf(slot));
        plcConnection.newParameters(getIP(), rack, slot);
    }

    private String getValueIP(JTextField textField){
        if(textField.getText().equals("")){
            return "0";
        } else if(Integer.parseInt(textField.getText()) > 255){
            return "255";
        }
        return textField.getText();
    }

    public String getIP(){
        return String.format("%s.%s.%s.%s", oct1, oct2, oct3, oct4);
    }

    public void shutdownRuntime(){
        runtimeExecutor.shutdown();
    }
}

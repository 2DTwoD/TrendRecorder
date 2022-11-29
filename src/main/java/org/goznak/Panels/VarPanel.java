package org.goznak.Panels;

import org.goznak.Instruments.JTextFieldLimit;
import org.goznak.Instruments.PLCParameters;
import org.goznak.Instruments.PLCConnection;
import org.goznak.Instruments.Recorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VarPanel extends JPanel {

    JTextField nameField = new JTextField();
    String[] addressNameArray = {"M", "MB", "MW", "MD", "DBX", "DBB", "DBW", "DBD", "PI", "PIW", "PQ", "PQW"};
    JComboBox<String> addressCombo = new JComboBox<>(addressNameArray);
    JTextField dbNumberField = new JTextField();
    JTextField byteField = new JTextField();
    JTextField bitField = new JTextField();
    String[] floatingArray = {"Целое", "Дробное"};
    JComboBox<String> floatingCombo = new JComboBox<>(floatingArray);
    JButton checkButton = new JButton("Проверка");
    JButton applyButton = new JButton("Применить");
    JLabel readLabel = new JLabel("?", JLabel.CENTER);
    String[] periodsArray = {"100мс", "200мс", "500мс", "1с", "2с", "5с", "10с"};
    int[] intPeriodsArray = {100, 200, 500, 1000, 2000, 5000, 10000};
    JComboBox<String> periodCombo = new JComboBox<>(periodsArray);
    JButton recordButton = new JButton("Пуск");
    JLabel recordStateLabel = new JLabel("Остановлено", JLabel.CENTER);
    JButton deleteButton = new JButton("X");
    public Recorder recorder;
    MainPanel mainPanel;
    Dimension[] rowDim;
    ScheduledExecutorService runtimeExecutor;
    PLCConnection plcConnection;

    public VarPanel(MainPanel mainPanel,PLCConnection plcConnection, int id,
                    PLCParameters plcAddress, LayoutManager layout){
        super(layout);
        rowDim = MainPanel.rowDim;
        this.mainPanel = mainPanel;
        this.plcConnection = plcConnection;
        recorder = new Recorder(plcConnection, id, plcAddress);
        int tmpID = id;
        while(mainPanel.matchNameInTrendMap(recorder.getName())) {
            recorder.setName(++tmpID);
        }
        dbNumberField.setDocument(new JTextFieldLimit(4));
        byteField.setDocument(new JTextFieldLimit(7));
        bitField.setDocument(new JTextFieldLimit(1));
        nameField.setPreferredSize(rowDim[0]);
        addressCombo.setPreferredSize(rowDim[1]);
        dbNumberField.setPreferredSize(rowDim[2]);
        byteField.setPreferredSize(rowDim[3]);
        bitField.setPreferredSize(rowDim[4]);
        floatingCombo.setPreferredSize(rowDim[5]);
        periodCombo.setPreferredSize(rowDim[6]);
        applyButton.setPreferredSize(rowDim[7]);
        checkButton.setPreferredSize(rowDim[8]);
        readLabel.setPreferredSize(rowDim[9]);
        recordButton.setPreferredSize(rowDim[10]);
        recordStateLabel.setPreferredSize(rowDim[11]);
        deleteButton.setPreferredSize(rowDim[12]);
        readLabel.setBackground(Color.LIGHT_GRAY);
        readLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        readLabel.setOpaque(true);
        recordStateLabel.setBackground(Color.LIGHT_GRAY);
        recordStateLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        recordStateLabel.setOpaque(true);
        nameField.setText(recorder.getName());
        applyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                apply();
            }
        });
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mainPanel.deleteVar(id);
            }
        });
        addressCombo.addActionListener(e -> {
            if(addressCombo.getSelectedIndex() == 0 || addressCombo.getSelectedIndex() == 4 ||
                    addressCombo.getSelectedIndex() == 8 || addressCombo.getSelectedIndex() == 10){
                bitField.setEditable(true);
                bitField.setText(String.valueOf(recorder.getAreaBit()));
            } else {
                bitField.setEditable(false);
                bitField.setBackground(Color.LIGHT_GRAY);
                bitField.setText("0");
            }
            if(addressCombo.getSelectedIndex() > 3 && addressCombo.getSelectedIndex() < 8){
                dbNumberField.setEditable(true);
                dbNumberField.setText(String.valueOf(recorder.getDBNumber()));
            } else {
                dbNumberField.setEditable(false);
                dbNumberField.setBackground(Color.LIGHT_GRAY);
                dbNumberField.setText("0");
            }
            if(addressCombo.getSelectedIndex() == 3 || addressCombo.getSelectedIndex() == 7){
                floatingCombo.setEnabled(true);
                floatingCombo.setSelectedItem(floatingArray[recorder.getFloating()]);
            } else {
                floatingCombo.setEnabled(false);
                floatingCombo.setBackground(Color.GRAY);
                floatingCombo.setSelectedItem(floatingArray[0]);
            }
            revalidate();
        });
        checkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                readOnce();
            }
        });
        recordButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(recorder.record){
                    recorder.shutdown();
                } else {
                    recorder.start(false);
                }
            }
        });
        runtimeExecutor = Executors.newSingleThreadScheduledExecutor();
        runtimeExecutor.scheduleAtFixedRate(this::runTimeUpdate, 0, 100, TimeUnit.MILLISECONDS);
        add(nameField);
        add(addressCombo);
        add(dbNumberField);
        add(byteField);
        add(bitField);
        add(floatingCombo);
        add(periodCombo);
        add(applyButton);
        add(checkButton);
        add(readLabel);
        add(recordButton);
        add(recordStateLabel);
        add(deleteButton);
        setFirstFields();
        apply();
        SwingUtilities.invokeLater(this::revalidate);
        mainPanel.revalidateAll();
    }

    public void shutdownRecorder(){
        recorder.shutdown();
        recorder.saving = true;
        recorder.record = true;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(recorder::save);
        executor.shutdown();
    }

    public boolean recorderSaveCompleted(){
        return recorder.saving;
    }

    public void readOnce(){
        recorder.start(true);
    }

    public void startRecord(){
        recorder.start(false);
    }

    public void stopRecord(){
        recorder.shutdown();
    }

    public void apply(){
        String name = nameField.getText();
        int areaType = addressCombo.getSelectedIndex();
        int dbNumber = MainPanel.getValue(dbNumberField);
        int areaByte = MainPanel.getValue(byteField);
        int areaBit = MainPanel.getValue(bitField);
        if(areaBit > 7){
            areaBit = 7;
        }
        int period = intPeriodsArray[periodCombo.getSelectedIndex()];
        int floating;
        floating = floatingCombo.getSelectedIndex();
        if(mainPanel.matchNameInTrendMap(name)){
            name = recorder.getName();
        }
        nameField.setText(name);
        addressCombo.setSelectedItem(addressNameArray[areaType]);
        dbNumberField.setText(String.valueOf(dbNumber));
        byteField.setText(String.valueOf(areaByte));
        bitField.setText(String.valueOf(areaBit));
        periodCombo.setSelectedItem(addressNameArray[areaType]);
        floatingCombo.setSelectedItem(floatingArray[floating]);
        recorder.setParameters(name, areaType,dbNumber, areaByte, areaBit, period, floating);
        recorder.restart();
        mainPanel.setPrevAddress(getRecordPlcParameters());
    }

    private void runTimeUpdate(){
        MainPanel.checkField(nameField, recorder.getName());
        if(dbNumberField.isEditable()) {
            MainPanel.checkField(dbNumberField, String.valueOf(recorder.getDBNumber()));
        }
        MainPanel.checkField(byteField, String.valueOf(recorder.getAreaByte()));
        if(bitField.isEditable()) {
            MainPanel.checkField(bitField, String.valueOf(recorder.getAreaBit()));
        }
        checkCombo(addressCombo, recorder.getAreaType());
        checkCombo(periodCombo, getMatchIndex(intPeriodsArray, recorder.getPeriod()));
        checkCombo(floatingCombo, recorder.getFloating());
        if(recorder.record){
            recordButton.setText("Стоп");
            recordStateLabel.setText("Идёт запись");
            recordStateLabel.setBackground(Color.RED);
            recordStateLabel.setForeground(Color.WHITE);
        } else {
            recordButton.setText("Пуск");
            recordStateLabel.setText("Остановлено");
            recordStateLabel.setBackground(Color.LIGHT_GRAY);
            recordStateLabel.setForeground(Color.BLACK);
        }
        String value = recorder.getLinkValue() == null ? "?" : String.valueOf(recorder.getLinkValue());
        readLabel.setText(value);
        switch (plcConnection.getLinkStatus()){
            case PLCConnection.BAD -> {
                readLabel.setBackground(Color.RED);
                activeButtonsVisualisation(Color.LIGHT_GRAY);
                setRecorderLinkStatus(PLCConnection.BAD);
            }
            case PLCConnection.UNKNOWN -> {
                readLabel.setBackground(Color.LIGHT_GRAY);
                activeButtonsVisualisation(Color.LIGHT_GRAY);
                setRecorderLinkStatus(PLCConnection.UNKNOWN);
            }
            default -> {
                activeButtonsVisualisation(Color.BLACK);
                switch (recorder.getLinkStatus()) {
                    case PLCConnection.BAD -> readLabel.setBackground(Color.RED);
                    case PLCConnection.GOOD -> readLabel.setBackground(Color.GREEN);
                    case PLCConnection.UNKNOWN -> readLabel.setBackground(Color.LIGHT_GRAY);
                }
                if(!recorder.record && recorder.getLinkStatus() != PLCConnection.BAD){
                    recorder.setLinkStatus(PLCConnection.UNKNOWN);
                }
            }
        }
    }

    private void activeButtonsVisualisation(Color color){
        if(recorder.record){
            checkButton.setForeground(Color.LIGHT_GRAY);
        } else {
            checkButton.setForeground(color);
        }
        if(!recorder.record) {
            recordButton.setForeground(color);
        } else {
            recordButton.setForeground(Color.BLACK);
        }
    }

    private void checkCombo(JComboBox<String> comboBox, int index){
        if(comboBox.getSelectedIndex() == index){
            comboBox.setBackground(Color.WHITE);
        } else {
            comboBox.setBackground(Color.YELLOW);
        }
    }

    private int getMatchIndex(int[] array, int item){
        for(int i = 0; i < array.length; i++){
            if(item == array[i]){
                return i;
            }
        }
        return 0;
    }

    public PLCParameters getRecordPlcParameters(){
        return recorder.getPlcParameters();
    }

    public void setFirstFields(){
        addressCombo.setSelectedItem(addressNameArray[recorder.getAreaType()]);
        dbNumberField.setText(String.valueOf(recorder.getDBNumber()));
        byteField.setText(String.valueOf(recorder.getAreaByte()));
        bitField.setText(String.valueOf(recorder.getAreaBit()));
        periodCombo.setSelectedItem(periodsArray[getMatchIndex(intPeriodsArray, recorder.getPeriod())]);
        floatingCombo.setSelectedItem(floatingArray[recorder.getFloating()]);
    }

    public void setRecorderLinkStatus(int status){
        recorder.setLinkStatus(status);
    }

    public void shutdownRuntime(){
        runtimeExecutor.shutdown();
    }

    public String getRecordName(){
        return recorder.getName();
    }
}
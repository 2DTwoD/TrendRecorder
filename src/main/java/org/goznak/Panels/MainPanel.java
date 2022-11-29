package org.goznak.Panels;

import org.goznak.Instruments.PLCParameters;
import org.goznak.Instruments.PLCConnection;
import org.goznak.Instruments.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.Set;

public class MainPanel extends JScrollPane {

    JFrame mainFrame;
    JPanel mainPanel;
    PLCPanel plcPanel;
    JPanel panelForVars = new JPanel();
    JButton addVarButton = new JButton("+Добавить переменную+");
    HashMap<Integer, VarPanel> varPanelsMap = new HashMap<>();
    int id = 0;
    PLCConnection plcConnection;
    int varLim = 10;
    Dimension defaultDimension;
    PLCParameters prevAddress;
    public static final Dimension tiny = new Dimension(35, 20);
    public static final Dimension small = new Dimension(45, 20);
    public static final Dimension middle = new Dimension(65, 20);
    public static final Dimension big = new Dimension(100, 20);
    public static final Dimension huge = new Dimension(150, 20);
    public static final Dimension[] rowDim = {huge, middle, tiny, middle, tiny,
            big, middle, big, big, big, middle, big, small};
    FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 2, 0);
    ColumnsPanel columnsPanel = new ColumnsPanel(layout);
    CommonButtonPanel commonButtonPanel;

    public MainPanel(JFrame mainFrame, Dimension defaultDimension){
        super();
        this.mainFrame = mainFrame;
        this.defaultDimension = defaultDimension;
        plcConnection = new PLCConnection(this);
        commonButtonPanel = new CommonButtonPanel(this, plcConnection, layout);
        mainPanel = new JPanel(new VerticalLayout(mainFrame, 0, 5, VerticalLayout.CENTER));
        setViewportView(mainPanel);
        getVerticalScrollBar().setUnitIncrement(20);
        plcPanel = new PLCPanel(this, plcConnection, layout);
        panelForVars.setLayout(new BoxLayout(panelForVars, BoxLayout.Y_AXIS));
        addVarButton.setPreferredSize(new Dimension(180, 20));
        mainPanel.add(plcPanel);
        mainPanel.add(columnsPanel);
        mainPanel.add(panelForVars);
        mainPanel.add(commonButtonPanel);
        mainPanel.add(addVarButton);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                for (VarPanel varPanel: varPanelsMap.values()) {
                    varPanel.shutdownRecorder();
                    varPanel.shutdownRuntime();
                }
                plcPanel.shutdownRuntime();
                commonButtonPanel.shutdownRuntime();
                plcConnection.shutdownCheckConnection();
                while (!allCompleted());
                System.exit(0);
            }
        });
        addVarButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                newVar();
            }
        });
        revalidateAll();
        modifyComponents();
    }

    public boolean allCompleted(){
        boolean result = true;
        for (VarPanel varPanel: varPanelsMap.values()) {
            result &= !varPanel.recorderSaveCompleted();
        }
        return result;
    }

    public void newVar(){
        if(varPanelsMap.size() >= varLim){
            System.out.println("Слишком много переменных");
            return;
        }
        VarPanel varPanel = new VarPanel(this, plcConnection, id, prevAddress, layout);
        setPrevAddress(varPanel.getRecordPlcParameters());
        varPanelsMap.put(id, varPanel);
        panelForVars.add(varPanel);
        id++;
        modifyComponents();
        revalidateAll();
    }

    public void deleteVar(int id){
        VarPanel varPanel = varPanelsMap.get(id);
        varPanel.shutdownRecorder();
        panelForVars.remove(varPanel);
        varPanelsMap.remove(id);
        modifyComponents();
        revalidateAll();
    }

    public static void checkField(JTextField textField, String txt){
        if(txt.equals(textField.getText())){
            textField.setBackground(Color.WHITE);
        } else {
            textField.setBackground(Color.YELLOW);
        }
    }

    public static int getValue(JTextField textField){
        if(textField.getText().equals("")){
            return 0;
        } else {
            return Integer.parseInt(textField.getText());
        }
    }

    private void modifyComponents(){
        columnsPanel.setVisible(varPanelsMap.size() >= 1);
        commonButtonPanel.setVisible(varPanelsMap.size() > 1);
        addVarButton.setVisible(varPanelsMap.size() < varLim);
        if(mainFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH){
            Dimension size = new Dimension(defaultDimension);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            size.height = size.height + 20 * varPanelsMap.size();
            if(size.height < screenSize.height) {
                mainFrame.setSize(size);
            }
        }
    }

    public void revalidateAll(){
        SwingUtilities.invokeLater(() -> {
            plcPanel.revalidate();
            columnsPanel.revalidate();
            panelForVars.revalidate();
            commonButtonPanel.revalidate();
            revalidate();
            mainFrame.revalidate();
            JScrollBar scrollBar = getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        });
    }

    public void startAllRecords(){
        for (VarPanel varPanel: varPanelsMap.values()) {
            varPanel.startRecord();
        }
    }

    public void stopAllRecords(){
         for (VarPanel varPanel: varPanelsMap.values()) {
             varPanel.stopRecord();
         }
    }

    public void checkAllVars(){
        for (VarPanel varPanel: varPanelsMap.values()) {
            varPanel.readOnce();
        }
    }

    public void applyAllRecords(){
        for (VarPanel varPanel: varPanelsMap.values()) {
            varPanel.apply();
        }
    }

    public void deleteAllRecords(){
        Set<Integer> ids = Set.copyOf(varPanelsMap.keySet());
        for (Integer id: ids) {
            deleteVar(id);
        }
    }

    public void setAllLinkStatus(int status){
        for (VarPanel varPanel: varPanelsMap.values()) {
            varPanel.setRecorderLinkStatus(status);
        }
    }

    public void setPrevAddress(PLCParameters plcParameters){
        prevAddress = plcParameters;
    }

    public boolean matchNameInTrendMap(String name){
        for(VarPanel varPanel: varPanelsMap.values()){
            if (varPanel.getRecordName().equals(name)){
                return true;
            }
        }
        return false;
    }
}

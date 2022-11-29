package org.goznak.Instruments;

import com.sourceforge.snap7.moka7.*;
import org.goznak.Panels.MainPanel;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PLCConnection {

    MainPanel mainPanel;
    String IP = "0.0.0.0";
    int rack = 0;
    int slot = 1;
    int linkStatus = UNKNOWN;
    public static final int UNKNOWN = 0;
    public static final int GOOD = 1;
    public static final int BAD = 2;
    ExecutorService startConnectionExecutor;
    S7Client client = new S7Client();
    public static final int M_AREA = 0;
    public static final int MB_AREA = 1;
    public static final int MW_AREA = 2;
    public static final int MD_AREA = 3;
    public static final int DBX_AREA = 4;
    public static final int DBB_AREA = 5;
    public static final int DBW_AREA = 6;
    public static final int DBD_AREA = 7;
    public static final int I_AREA = 8;
    public static final int PIW_AREA = 9;
    public static final int Q_AREA = 10;
    public static final int PQW_AREA = 11;
    public static final String PLC_RUN = "Статус: RUN";
    public static final String PLC_STOP = "Статус: STOP";
    public static final String PLC_UNDEFINED = "Статус: ?";
    ScheduledExecutorService checkConnectionExecutor;
    IntByRef statusConnection = new IntByRef();
    boolean connectFlag = false;
    String plcStatus = PLC_UNDEFINED;

    public PLCConnection(MainPanel mainPanel){
        this.mainPanel = mainPanel;
        client.SetConnectionType(S7.S7_BASIC);
    }

    public void newParameters(String IP, int rack, int slot){
        this.IP = IP;
        this.rack = rack;
        this.slot = slot;
        checkConnectionExecutor = Executors.newSingleThreadScheduledExecutor();
        checkConnectionExecutor.scheduleAtFixedRate(this::checkConnection, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void connect(){
        if(connecting() || connected()){
            return;
        }
        connectFlag = true;
        startConnectionExecutor = Executors.newSingleThreadExecutor();
        startConnectionExecutor.submit(() -> {
            client = new S7Client();
            client.SetConnectionType(S7.S7_BASIC);
            int res = client.ConnectTo(getIP(), getRack(), getSlot());
            if (res != 0) {
                System.out.println("place1 " + S7Client.ErrorText(res));
                setLinkStatus(BAD);
            } else {
                setLinkStatus(GOOD);
                mainPanel.setAllLinkStatus(PLCConnection.UNKNOWN);
            }
        });
        startConnectionExecutor.shutdown();
    }

    public void disconnect(){
        connectFlag = false;
        setLinkStatus(UNKNOWN);
        client.Disconnect();
    }

    public boolean connecting(){
        return startConnectionExecutor != null && !startConnectionExecutor.isTerminated();
    }

    public boolean connected() {
        return client.Connected;
    }

    public synchronized void checkConnection(){
        if (connectFlag) {
            int res;
            try {
                res = client.GetPlcStatus(statusConnection);
            }
            catch(Exception e){
                res = 1;
            }
            if (res != 0) {
                linkStatus = BAD;
                System.out.println("place2 " + S7Client.ErrorText(res));
                reconnect();
            } else {
                switch (statusConnection.Value){
                    case 4 -> plcStatus = PLC_STOP;
                    case 8 -> plcStatus = PLC_RUN;
                    default -> plcStatus = PLC_UNDEFINED;
                }
                linkStatus = GOOD;
            }
        } else {
            linkStatus = UNKNOWN;
        }
    }

    public void reconnect(){
        client.Disconnect();
        connect();
    }

    public synchronized Double getPLCVar(PLCParameters plcAddress, Recorder recorder){
        if(!connected()) {
            System.out.println("Place 3 Нет связи с ПЛК");
            return null;
        }
        Double result = null;
        try {
            byte[] data = new byte[4];
            client.ReadArea(getAreaForPLC(plcAddress.areaType), plcAddress.dbNumber, plcAddress.areaByte, 4, data);
            switch (plcAddress.areaType) {
                case M_AREA, DBX_AREA, I_AREA, Q_AREA -> result = S7.GetBitAt(data, 0, plcAddress.areaBit) ? 1.0 : 0.0;
                case MB_AREA, DBB_AREA -> result = (double) (data[0] & 255);
                case MW_AREA, DBW_AREA, PIW_AREA, PQW_AREA -> result = (double) S7.GetShortAt(data, 0);
                case MD_AREA, DBD_AREA -> {
                    if(plcAddress.floating == 1) {
                        result = (double) S7.GetFloatAt(data, 0);
                    } else {
                        result = (double) S7.GetDIntAt(data, 0);
                    }
                }
            }
        }
        catch(Exception e){
            recorder.setLinkStatus(PLCConnection.BAD);
            System.out.println("Place 4 Проблема считывания: " + e.getMessage());
            return null;
        }
        return result;
    }

    public int getAreaForPLC(int areaType){
        if(areaType > 9){
            return S7.S7AreaPA;
        } else if(areaType > 7){
            return S7.S7AreaPE;
        } else if(areaType > 3){
            return S7.S7AreaDB;
        } else {
            return S7.S7AreaMK;
        }
    }

    public void setLinkStatus(int value){
        linkStatus = value;
    }

    public String getIP(){
        return IP;
    }

    public int getRack() {
        return rack;
    }

    public int getSlot() {
        return slot;
    }

    public int getLinkStatus() {
        return linkStatus;
    }

    public boolean getConnectFlag(){
        return connectFlag;
    }

    public String getPlcStatus(){
        return plcStatus;
    }

    public void shutdownCheckConnection(){
        checkConnectionExecutor.shutdown();
    }
}

package org.goznak.Instruments;

public class PLCParameters {

    public int areaType = PLCConnection.M_AREA;
    public int dbNumber = 0;
    public int areaByte = 0;
    public int areaBit = 0;
    public int period = 100;
    public int floating = 0;

    public void copyAddress(PLCParameters plcParameters){
        if (plcParameters == null){
            return;
        }
        areaType = plcParameters.areaType;
        dbNumber = plcParameters.dbNumber;
        areaByte = plcParameters.areaByte;
        areaBit = plcParameters.areaBit;
        period = plcParameters.period;
        floating = plcParameters.floating;
    }
}

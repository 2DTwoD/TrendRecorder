package org.goznak.Instruments;

import org.goznak.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Recorder {

    ArrayList<Double> valueList = new ArrayList<>();
    ArrayList<Long> timeList = new ArrayList<>();
    ArrayList<Double> valueListForSaving = valueList;
    ArrayList<Long> timeListForSaving = timeList;
    String name = "Trend";
    PLCParameters plcParameters = new PLCParameters();
    final int capacity = 10000;
    final int saveInterval = 5;
    public boolean saving = false;
    public boolean record = false;
    public int linkStatus = PLCConnection.UNKNOWN;
    Double linkValue = null;
    ScheduledExecutorService recordExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService saveExecutorService = Executors.newSingleThreadScheduledExecutor();
    PLCConnection plcConnection;

    Runnable recordCycle = () -> {
        if(valueList.size() >= capacity){
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(this::save);
            executor.shutdown();
        }
        Double result = plcConnection.getPLCVar(plcParameters, this);
        if(result == null){
            //shutdown();
            linkStatus = PLCConnection.BAD;
            return;
        }
        linkValue = result;
        valueList.add(linkValue);
        timeList.add(new Date().getTime());
        linkStatus = PLCConnection.GOOD;
    };

    public Recorder(PLCConnection plcConnection, int id, PLCParameters plcAddress){
        this.plcConnection = plcConnection;
        name += id;
        this.plcParameters.copyAddress(plcAddress);
    }

    public void cloneList(){
        valueListForSaving = valueList;
        timeListForSaving = timeList;
        valueList = new ArrayList<>();
        timeList = new ArrayList<>();
    }

    public synchronized void save() {
        saving = true;
        cloneList();
        if(valueListForSaving.size() == 0){
            saving = false;
            return;
        }
        String path;
        try {
            path = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        }
        catch (URISyntaxException e){
            System.out.println("Ошибка определения пути к файлу" + e.getMessage());
            saving = false;
            return;
        }
        try(FileWriter writer = new FileWriter(String.format("%s/%s.txt", path, name), true))
        {
            StringBuilder result = new StringBuilder();
            String row;
            for(int i = 0; i < valueListForSaving.size(); i++){
                row = String.format("%f - %d\r\n", valueListForSaving.get(i), timeListForSaving.get(i));
                row = row.replace(",", ".");
                result.append(row);
            }
            writer.write(String.valueOf(result));
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        finally {
            saving = false;
        }
    }

    public void start(boolean once){
        if(record || plcConnection.getLinkStatus() != PLCConnection.GOOD){
            return;
        }
        linkStatus = PLCConnection.UNKNOWN;
        if(!once) {
            recordExecutorService = Executors.newSingleThreadScheduledExecutor();
            saveExecutorService = Executors.newSingleThreadScheduledExecutor();
            recordExecutorService.scheduleAtFixedRate(recordCycle, 0, plcParameters.period, TimeUnit.MILLISECONDS);
            saveExecutorService.scheduleAtFixedRate(this::save, 0, saveInterval, TimeUnit.MINUTES);
        } else {
            Double result = plcConnection.getPLCVar(plcParameters, this);
            if(result == null){
                linkStatus = PLCConnection.BAD;
            } else {
                linkValue = result;
                linkStatus = PLCConnection.GOOD;
            }
        }
        record = !once;
    }

    public void shutdown(){
        if(!record){
            return;
        }
        recordExecutorService.shutdown();
        saveExecutorService.shutdownNow();
        linkStatus = PLCConnection.UNKNOWN;
        record = false;
        save();
    }

    public void restart(){
        if(!record){
            return;
        }
        shutdown();
        start(false);
    }

    public void setName(int id){
        this.name = "Trend" + id;
    }

    public String getName() {
        return name;
    }

    public PLCParameters getPlcParameters(){
        return plcParameters;
    }

    public int getAreaType() {
        return plcParameters.areaType;
    }

    public int getDBNumber() {
        return plcParameters.dbNumber;
    }

    public int getAreaByte() {
        return plcParameters.areaByte;
    }

    public int getAreaBit() {
        return plcParameters.areaBit;
    }

    public int getPeriod() {
        return plcParameters.period;
    }

    public int getFloating() {
        return plcParameters.floating;
    }

    public Double getLinkValue() {
        return linkValue;
    }

    public int getLinkStatus(){
        return linkStatus;
    }

    public void setParameters(String name, int areaType, int dbNumber, int areaByte, int areaBit, int period, int floating) {
        this.name = name;
        plcParameters.areaType = areaType;
        plcParameters.dbNumber = dbNumber;
        plcParameters.areaByte = areaByte;
        plcParameters.areaBit = areaBit;
        plcParameters.period = period;
        plcParameters.floating = floating;
    }

    public void setLinkStatus(int status){
        linkStatus = status;
    }
}

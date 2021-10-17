package com.example.anonycall.models;

public class ChatMsg {
    private String mes,time,id;
    public ChatMsg(String mes, String time, String id){
        this.mes = mes;
        this.time = time;
        this.id = id;
    }
    public ChatMsg(){}
    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ChatMsg{" +
                "mes='" + mes + '\'' +
                ", time='" + time + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

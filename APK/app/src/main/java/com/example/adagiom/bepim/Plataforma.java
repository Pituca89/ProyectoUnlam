package com.example.adagiom.bepim;

import java.io.Serializable;

public class Plataforma implements Serializable {
    private String chipid;
    private int disponible;
    private int sectoract;
    private String ip;
    private String nombre;

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setChipid(String chipid) {
        this.chipid = chipid;
    }

    public String getChipid() {
        return chipid;
    }

    public void setDisponible(int disponible) {
        this.disponible = disponible;
    }

    public int getDisponible() {
        return disponible;
    }

    public void setSectoract(int sectoract) {
        this.sectoract = sectoract;
    }

    public int getSectoract() {
        return sectoract;
    }
}

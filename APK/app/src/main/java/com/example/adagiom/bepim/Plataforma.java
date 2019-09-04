package com.example.adagiom.bepim;

import java.io.Serializable;

public class Plataforma implements Serializable {
    private String chipid;
    private int disponible;
    private String sectoract;
    private String ip;
    private String nombre;
    private int idsector;

    public void setIdsector(int idsector) {
        this.idsector = idsector;
    }

    public int getIdsector() {
        return idsector;
    }

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

    public void setSectoract(String sectoract) {
        this.sectoract = sectoract;
    }

    public String getSectoract() {
        return sectoract;
    }
}

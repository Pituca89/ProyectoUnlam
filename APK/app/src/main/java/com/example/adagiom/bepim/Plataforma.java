package com.example.adagiom.bepim;

public class Plataforma {
    private int chipid;
    private int disponible;
    private int sectoract;

    public void setChipid(int chipid) {
        this.chipid = chipid;
    }

    public int getChipid() {
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

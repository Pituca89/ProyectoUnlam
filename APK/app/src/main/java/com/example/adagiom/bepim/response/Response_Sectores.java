package com.example.adagiom.bepim.response;

import com.example.adagiom.bepim.model.Sector;

import java.util.ArrayList;

public class Response_Sectores {
    String opcion;
    int actual;
    ArrayList<Sector> sectores;

    public ArrayList<Sector> getSectores() {
        return sectores;
    }

    public String getOpcion() {
        return opcion;
    }

    public void setOpcion(String opcion) {
        this.opcion = opcion;
    }

    public void setSectores(ArrayList<Sector> sectores) {
        this.sectores = sectores;
    }

    public Response_Sectores(String opcion){
        this.opcion = opcion;
    }
    public Response_Sectores(String opcion,ArrayList<Sector> sectores){
        this.opcion = opcion;
        this.sectores = sectores;
    }
    public void setActual(int actual) {
        this.actual = actual;
    }

    public int getActual() {
        return actual;
    }
}

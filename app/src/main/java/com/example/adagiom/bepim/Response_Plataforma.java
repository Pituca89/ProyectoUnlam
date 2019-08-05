package com.example.adagiom.bepim;

import java.util.ArrayList;

public class Response_Plataforma {
    String opcion;
    ArrayList<Plataforma> plataforma;

    public ArrayList<Plataforma> getPlataforma() {
        return plataforma;
    }

    public String getOpcion() {
        return opcion;
    }

    public void setOpcion(String opcion) {
        this.opcion = opcion;
    }

    public void setPlataformas(ArrayList<Plataforma> plataformas) {
        this.plataforma = plataformas;
    }

    public Response_Plataforma(String opcion){
        this.opcion = opcion;
    }
    public Response_Plataforma(String opcion,ArrayList<Plataforma> plataformas){
        this.opcion = opcion;
        this.plataforma = plataformas;
    }
}

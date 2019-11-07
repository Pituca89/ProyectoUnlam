package com.example.adagiom.bepim.model;

public class Sector {
    String nombre;
    int id;
    String idPlataforma;
    String mac;
    int actual;
    public Sector(String name,int id){
        this.nombre = name;
        this.id =  id;
    }

    public void setActual(int atual) {
        this.actual = atual;
    }

    public int getActual() {
        return actual;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setIdPlataforma(String idPlataforma) {
        this.idPlataforma = idPlataforma;
    }

    public String getIdPlataforma() {
        return idPlataforma;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}


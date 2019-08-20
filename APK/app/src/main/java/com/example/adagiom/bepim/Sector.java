package com.example.adagiom.bepim;

public class Sector {
    String nombre;
    int id;
    String idPlataforma;

    public Sector(String name,int id){
        this.nombre = name;
        this.id =  id;
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


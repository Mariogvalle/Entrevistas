package com.tareas2p.entrevistas.config;

public class Entrevista {
    private String id;
    private String descripcion;
    private String periodista;
    private String fecha;
    private String foto;
    private String audio;

    //Constructores

    public Entrevista() {
    }

    public Entrevista(String descripcion, String periodista, String fecha, String foto, String audio) {
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.foto = foto;
        this.audio = audio;
    }

    public Entrevista(String id, String descripcion, String periodista, String fecha, String foto, String audio) {
        this.id = id;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.foto = foto;
        this.audio = audio;
    }


//Getter and setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}

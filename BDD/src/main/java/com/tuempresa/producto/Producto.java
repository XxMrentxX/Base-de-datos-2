package com.tuempresa.producto;

import com.tuempresa.auxiliares.Comentario;
import com.tuempresa.clasesabstractas.Cambio;

import java.util.ArrayList;
import java.util.List;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private String empresa;
    private List<String> imagenes;
    private List<String> videos;
    private List<Comentario> comentarios;
    private double precioActual;
    private List<Cambio> historialCambios;


    public Producto(String id, String nombre, String descripcion, String empresa, List<String> imagenes, List<String> videos, List<Comentario> comentarios, double precio_actual, List<Cambio> historialCambios) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.empresa = empresa;
        this.imagenes = new ArrayList<>();
        this.videos = new ArrayList<>();
        this.comentarios = new ArrayList<>();
        this.precioActual = precio_actual;
        this.historialCambios = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {}

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public double getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(double precio_actual) {
        this.precioActual = precio_actual;
    }

    public List<Cambio> getHistorialCambios() {
        return historialCambios;
    }

    public void agregarComentario(Comentario comentario){
        this.comentarios.add(comentario);
    }

    public void agregarCambio (Cambio cambio){
        historialCambios.add(cambio);
        System.out.println("Cambio agregado: " + cambio.getResumen());
    }
}

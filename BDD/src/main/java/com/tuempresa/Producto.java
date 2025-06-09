package com.tuempresa;

import com.tuempresa.auxiliares.CambioCampo;
import com.tuempresa.auxiliares.CambioPrecio;
import com.tuempresa.auxiliares.Comentario;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private List<String> imagenes;
    private List<String> videos;
    private List<Comentario> comentarios;
    private double precio_actual;
    private List<CambioPrecio> historial_precio;
    private List<CambioCampo> historial_cambios;

    // Constructor
    public Producto(String id, String nombre, String descripcion, List<String> imagenes, List<String> videos, List<Comentario> comentarios, double precio_actual, List<CambioPrecio> historial_precio, List<CambioCampo> historial_cambios) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenes = new ArrayList<>();
        this.videos = new ArrayList<>();
        this.comentarios = new ArrayList<>();
        this.precio_actual = precio_actual;
        this.historial_precio = new ArrayList<>();
        this.historial_cambios = new ArrayList<>();
    }

    // Getters y Setters
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

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public double getPrecio_actual() {
        return precio_actual;
    }

    public void setPrecio_actual(double precio_actual) {
        this.precio_actual = precio_actual;
    }

    public List<CambioPrecio> getHistorial_precio() {
        return historial_precio;
    }

    public void setHistorial_precio(List<CambioPrecio> historial_precio) {
        this.historial_precio = historial_precio;
    }

    public List<CambioCampo> getHistorial_cambios() {
        return historial_cambios;
    }

    public void setHistorial_cambios(List<CambioCampo> historial_cambios) {
        this.historial_cambios = historial_cambios;
    }
}

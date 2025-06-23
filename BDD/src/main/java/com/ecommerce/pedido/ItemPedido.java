package com.ecommerce.pedido;

import java.util.UUID;

public class ItemPedido {
    private UUID productoId;
    private String nombreProducto;
    private int cantidad;
    private String empresa;
    private double precioUnitario;
    private double subtotal;
    private double iva;
    private double porcentajeDescuento;
    private double total;

    public ItemPedido(UUID productoId,String nombreProducto, int cantidad, String empresa, double precioUnitario, double subtotal, double iva, double porcentajeDescuento, double total) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.empresa = empresa;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.iva = iva;
        this.porcentajeDescuento = porcentajeDescuento;
        this.total = total;
    }

    public UUID getProductoId(){
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getPorcentajeDescuento(){
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento){
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}

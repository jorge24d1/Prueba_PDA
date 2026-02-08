package com.concesionario.dto;

import com.concesionario.model.Vehiculo;
import java.util.List;

public class RecomendacionResponse {
    private String respuesta;
    private List<Vehiculo> vehiculosRecomendados;

    public RecomendacionResponse() {}

    public RecomendacionResponse(String respuesta, List<Vehiculo> vehiculosRecomendados) {
        this.respuesta = respuesta;
        this.vehiculosRecomendados = vehiculosRecomendados;
    }

    // Getters y Setters
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public List<Vehiculo> getVehiculosRecomendados() { return vehiculosRecomendados; }
    public void setVehiculosRecomendados(List<Vehiculo> vehiculosRecomendados) {
        this.vehiculosRecomendados = vehiculosRecomendados;
    }
}
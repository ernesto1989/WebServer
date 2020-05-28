package com.conciencia.vertx.verticles.repository;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Interface que define todas las operaciones permitidas con Repositorios 
 * de datos.
 * 
 * @author Ernesto Cantu
 * 30/09/2019
 */
public interface DataRepository {
    
    /**
     * Método que incializa las propiedades especificas de cada Repositorio.
     */
    public void initInfo();
    
    /**
     * Método que permite determinar si un método http está o no permitido para
     * una entidad dada. La implementación default permite todos los métodos http
     * implementados para cualquier entidad.
     * 
     * Al sobreescribir el método, se pueden bloquear endpoints especificos
     * en entidades definidas.
     * 
     * @param method el método a ejecutar.
     * @return true si el método está permitido, false si no.
     */
    public Boolean methodAllowed(String method);
    
    /**
     * Método que al ser sobreescrito, permitirá a cada entidad definir métodos 
     * adicionales a los establecidos.
     */
    public void defineMoreMethods();
    
     /**
     * Método que permite informar de acciones ocurridas.
     * @param transaction
     */
    public void informTransaction(String transaction); 
    
    /**
     * Método que permite preparar los parámetros de un query con las propiedades
     * del objeto
     * @param entity
     * @param transaction
     * @return 
     */
    public abstract JsonArray initParams(JsonObject entity,String transaction);
    
    /**
     * Método que permite preparar los parámetros de un search con las propiedades
     * del objeto
     * @param entity
     * @param query
     * @return 
     */
    public JsonArray configSearch(JsonObject entity,StringBuilder query);
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Get All" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante un array de objetos JSON con el resultado del query
     * definido en "getAllQuery".
     */
    public void defineGetAll();
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Search" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante un array de objetos JSON con el resultado del query
     * definido en "searchQuery".
     */
    public void defineSearch();
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Add" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante el objeto JSON dado de alta con el query
     * definido en "addQuery".
     */
    public void defineAdd();
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Update" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante el objeto JSON dado de alta con el query
     * definido en "addQuery".
     */
    public void defineUpdate();
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Delete" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante el objeto JSON dado de alta con el query
     * definido en "deleteQuery".
     */
    public void defineDelete();
}

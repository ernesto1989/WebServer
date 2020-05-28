package com.conciencia.vertx.verticles.repository;


import static com.conciencia.vertx.VertxWebConfig.client;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import java.util.List;

/**
 * Clase abstracta que define operaciones genéricas de todas las entidades
 * y su interacción básica con un repositorio de datos.
 * 
 * @author Ernesto Cantu
 * 13/05/2019
 * 
 * update: 27/09/2019
 */
public abstract class DatabaseRepositoryVerticle extends AbstractVerticle implements DataRepository {

    /* Nombre de la entidad que implementa el CRUD */
    protected String entityName;
    
    /* Objeto recibido para operaciones CRUD */
    protected JsonObject entity;
    
    /* Query ejecutado al solicitar un GET ALL de la entidad solicitada */
    protected String getAllQuery;
    
    /* Query ejecutado al solicitar un SEARCH de la entidad solicitada */
    protected String searchQuery;
    
    /* Query ejecutado al solicitar un ADD de la entidad solicitada */
    protected String addQuery;
    
    /* Query ejecutado al solicitar un UPDATE de la entidad solicitada */
    protected String updateQuery;
    
    /* Query ejecutado al solicitar un DELETE de la entidad solicitada */
    protected String deleteQuery;
    
    /* JSON de propiedades del objeto */
    protected JsonArray params;
    
    /* Métodos de crud que implementa un DatabaseVerticle*/
    
    protected final String getAllMethod = "get_all";
    
    protected final String searchMethod = "search";
    
    protected final String addMethod = "add";
    
    protected final String updateMethod = "update";
    
    protected final String deleteMethod = "delete";
    
    /**
     * Método que incializa las propiedades especificas de cada Repositorio.
     */
    @Override
    public abstract void initInfo();
    
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
    @Override
    public Boolean methodAllowed(String method) {
        return true;
    }
    
    /**
     * Método que al ser sobreescrito, permitirá a cada entidad definir métodos 
     * adicionales a los establecidos.
     */
    @Override
    public void defineMoreMethods(){
    }
    
    /**
     * Método que permite informar de acciones ocurridas en Métodos http
     * @param transaction operacion realizada
     */
    @Override
    public void informTransaction(String transaction){
    }
    
    /**
     * Método que permite preparar los parámetros de un query con las propiedades
     * del objeto
     * @param entity objeto que se envía desde el cliente
     * @param transaction operación a realziar
     * @return parámetros del query en JsonArray
     */
    @Override
    public abstract JsonArray initParams(JsonObject entity,String transaction);
    
    /**
     * Método que permite preparar los parámetros de un search con las propiedades
     * del objeto 
     * @param entity configuración del objeto de búsqueda
     * @param query query a construir en base a parámetros
     * @return arrray de busqueda
     */
    @Override
    public JsonArray configSearch(JsonObject entity,StringBuilder query){
        return null;
    }
    
    // <editor-fold defaultstate="collapsed" desc="DEFINICION DE CRUD">
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Get All" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante un array de objetos JSON con el resultado del query
     * definido en "getAllQuery".
     */
    @Override
    public void defineGetAll(){
        vertx.eventBus().consumer("get_" + entityName,hndlr->{
            if(methodAllowed(getAllMethod)){
                client.getConnection(connectionHandler->{
                    if(connectionHandler.failed()){
                        hndlr.fail(0, connectionHandler.cause().toString());
                    }
                    SQLConnection connection = connectionHandler.result();
                    connection.query(getAllQuery, queryHndlr->{
                        connection.close();
                        if(queryHndlr.succeeded()){
                            ResultSet rs = queryHndlr.result();
                            List<JsonObject> results = rs.getRows();
                            hndlr.reply(new JsonArray(results));
                        }else{
                             hndlr.fail(0, queryHndlr.cause().toString());
                        }
                    });
                });
            }else{
                hndlr.fail(0, "Get all no implementado para: " + entityName);
            }
        });
    }
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Search" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante un array de objetos JSON con el resultado del query
     * definido en "searchQuery".
     */
    @Override
    public void defineSearch(){
        vertx.eventBus().consumer("search_" + entityName, hndlr->{
            if(methodAllowed(searchMethod)){
                JsonObject entity = (JsonObject)hndlr.body();
                this.entity = entity;
                client.getConnection(connectionHandler->{
                    if(connectionHandler.failed()){
                        hndlr.fail(0, connectionHandler.cause().toString());
                    }
                    SQLConnection connection = connectionHandler.result();
                    StringBuilder sQuery = new StringBuilder(String.valueOf(searchQuery));
                    JsonArray params = configSearch(entity, sQuery);
                    connection.queryWithParams(sQuery.toString(), params, queryHndlr->{
                        connection.close();
                        if(queryHndlr.succeeded()){
                            ResultSet rs = queryHndlr.result();
                            List<JsonObject> results = rs.getRows();
                            hndlr.reply(new JsonArray(results));
                        }else{
                             hndlr.fail(0, queryHndlr.cause().toString());
                        }
                    });
                });
            }else{
                hndlr.fail(0, "Search no implementado para: " + entityName);
            }
        });
    }
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Add" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante el objeto JSON dado de alta con el query
     * definido en "addQuery".
     */
    @Override
    public void defineAdd(){
        vertx.eventBus().consumer("add_" + entityName, hndlr->{
            if(methodAllowed(addMethod)){
                try{
                    JsonObject addObject = (JsonObject)hndlr.body();
                    this.entity = addObject;
                    client.getConnection(connectionHandler->{
                        if(connectionHandler.failed()){
                            hndlr.fail(0, connectionHandler.cause().getMessage());
                        }else{
                            SQLConnection connection = connectionHandler.result();
                            JsonArray params = initParams(addObject, "add");
                            connection.updateWithParams(addQuery, params, insertHandler->{
                                connection.close();
                                if(insertHandler.failed()){
                                    hndlr.fail(0, insertHandler.cause().toString());
                                }
                                int recid = insertHandler.result().getKeys().getInteger(0);
                                addObject.put("recid", recid);
                                informTransaction(addMethod);
                                hndlr.reply(addObject.put("added", Boolean.TRUE));
                            });
                        }
                    });
                }catch(Exception e){
                    hndlr.fail(0, "Error adding " + entityName + ". Reason: " + e.getMessage());
                }
            }else{
                hndlr.fail(0, "Add not implemented for " + entityName);
            }
            
        });
    }
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Update" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante el objeto JSON dado de alta con el query
     * definido en "addQuery".
     */
    @Override
    public void defineUpdate(){
        vertx.eventBus().consumer("edit_" + entityName, hndlr->{
            if(methodAllowed(updateMethod)){
                try{
                    JsonObject updateObject = (JsonObject)hndlr.body();
                    this.entity = updateObject;
                    if(updateObject.getInteger("recid") == null)
                        hndlr.fail(0, "Error updating " + updateObject + ". Reason: Missing id");
                    client.getConnection(connectionHandler->{
                        if(connectionHandler.failed())
                             hndlr.fail(0, connectionHandler.cause().getMessage());
                        SQLConnection connection = connectionHandler.result();
                        JsonArray params = initParams(updateObject,"update");
                        connection.updateWithParams(updateQuery, params, update->{
                            connection.close();
                            if(update.failed()){
                                hndlr.fail(0, update.cause().toString());
                            }
                            informTransaction(updateMethod);
                            hndlr.reply(updateObject.put("updated", Boolean.TRUE));
                        });
                    });
                }catch(Exception e){
                    hndlr.fail(0, "Error updating " + entityName + ". Reason: " + e.getMessage());
                }
            }else{
                hndlr.fail(0, "Edit not implemented for " + entityName);
            }
            
        });
    }
    
    /**
     * Método que crea una dirección en el event bus que permite procesar las 
     * peticiones "Delete" de la entidad que procesa la operación.
     * 
     * Regresa al solicitante el objeto JSON dado de alta con el query
     * definido en "deleteQuery".
     */
    @Override
    public void defineDelete(){
        vertx.eventBus().consumer("delete_" + entityName, hndlr->{
            if(methodAllowed(deleteMethod)){
                try{
                    JsonObject deleteObject = (JsonObject) hndlr.body();
                    this.entity = deleteObject;
                    if(deleteObject.getInteger("recid") == null)
                        hndlr.fail(0, "Error deleting " + deleteObject + ". Reason: Missing id");
                    //delete
                    client.getConnection(connectionHandler->{
                        if(connectionHandler.failed())
                             hndlr.fail(0, connectionHandler.cause().getMessage());
                        SQLConnection connection = connectionHandler.result();
                        JsonArray params = initParams(deleteObject, "delete");
                        connection.updateWithParams(deleteQuery, params, deleteHandler->{
                            connection.close();
                            if(deleteHandler.failed()){
                                hndlr.fail(0, deleteHandler.cause().toString());
                            }
                            informTransaction(deleteMethod);
                            hndlr.reply(deleteObject.put("deleted", Boolean.TRUE));
                        });
                    });
                    hndlr.reply(deleteObject.put("deleted", Boolean.TRUE)); 
                }catch(Exception e){
                    hndlr.fail(0, "Error deleting " + entityName + ". Reason: " + e.getMessage());
                }
            }else{
                hndlr.fail(0, "Delete not implemented for " + entityName);
            }   
            
        });
    }
    
    // </editor-fold>
    
    /**
     * Metodo que crea los handlers sobre el eventbus para responder de manera
     * genérica a peticiones web desde cualquier Database Verticle.
     */
    public void defineCrud(){
        defineGetAll();
        defineSearch();
        defineAdd();
        defineUpdate();
        defineDelete();
        defineMoreMethods();
    }
    
    /**
     * Método sobreescrito del verticle.
     * 
     * 1.- Inicializa la información de prueba.
     * 2.- Declara el manejador para obtener todos los gastos fijos
     * 3.- Declara el manejador para agregar un gasto fijo
     * 4.- Declara el manejador para editar un gasto fijo
     * 5.- Declara el manejador para eliminar un gasto fijo
     * 6.- Declara que el arranque del verticle ha terminado.
     * 
     * @param startFuture objeto future que permite saber que el verticle terminó
     * @throws Exception 
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {    
        initInfo();
        defineCrud();
        promise.complete();
    }
}

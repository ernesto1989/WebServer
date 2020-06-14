package com.conciencia.vertx;

import com.conciencia.vertx.verticles.repository.DataRepository;
import com.conciencia.vertx.verticles.web.WebServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Vertx Web Server automatic configuration
 * 
 * @author Ernesto Cantú Valle
 * 24/05/20
 */
public class VertxWebConfig {
    
    private Vertx vertx;
        
    /* Objeto de conexión con cliente*/ 
    public static JDBCClient client;
    
    /* Variables para conexión de BD*/
    private String host;
    private String db;
    private String user;
    private String password;
    private String driverClass;

    /* Objeto de configuración para bd*/
    private JsonObject config;

    
    public VertxWebConfig(boolean requireStaticContent,boolean requireRest, 
            String host, String db, String user, String password, String driverClass) {
        this.host = host;
        this.db = db;
        this.user = user;
        this.password = password;
        this.driverClass = driverClass;
        this.vertx = Vertx.vertx();
        
        initDBClient().setHandler(hndlr->{
            if(hndlr.succeeded()){
                System.out.println("initDB complete");
            }else{
                System.out.println("initDB error!");
            }
        });
        
        vertx.deployVerticle(new WebServerVerticle(requireStaticContent,requireRest),hndlr->{
            if(hndlr.succeeded()){
                System.out.println("Web Server deployed");
            }else{
                System.out.println("Web Server error!");
            }
        });
    }
    
    public void deployRepositoy(DataRepository repo){
        AbstractVerticle v = (AbstractVerticle) repo;
        vertx.deployVerticle(v,hndlr->{
            if(hndlr.succeeded()){
                System.out.println("Web Server deployed");
            }else{
                System.out.println("Web Server error!");
            }
        });
    }
    
    /**
     * Método de apoyo para configurar cliente de datos.
     */
    private Future<Void> initDBClient(){
        Promise<Void> promise = Promise.promise();
        
        config = new JsonObject()
            .put("url", "jdbc:mysql://" + host+"/" + db + "?useSSL=false&useTimezone=true&serverTimezone=America/Mexico_City&user=" + user + "&password=" + password)
            .put("driver_class", driverClass)
            .put("max_pool_size", 30);
        
        client = JDBCClient.createNonShared(vertx, config);
        
        client.getConnection(connectionHndlr->{
            if(connectionHndlr.failed()){
                promise.fail(connectionHndlr.cause().toString());
            }else{
                connectionHndlr.result().close();
                promise.complete();
            }
        });
        
        return promise.future();
    }    
}

package com.conciencia.vertx.verticles.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Verticle que crea un servidor web
 * 
 * http://vertx.io/docs/guide-for-java-devs/#_access_control_and_authentication
 * Autenticación
 * 
 * https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/io/vertx/ext/web/handler/impl/FormLoginHandlerImpl.java
 * form login handler
 *
 * @author Ernesto Cantu
 */
public class WebServerVerticle extends AbstractVerticle {

    private static final String REST_API_CONTEXT = "/api/";
    
    private Boolean requireRestApi = true;
    
    public WebServerVerticle(Boolean requireRestApi){
        this.requireRestApi = requireRestApi;
    }
    
    // <editor-fold defaultstate="collapsed" desc="DEFINICION DE MÉTODOS HTTP REST GENERICOS">
    
    private void initRestApi(Router router){
        router.route().handler(BodyHandler.create()); // permite recibir json en el servidor
        
        //REST Api
        defineGetAll(router);
        defineSearch(router);
        definePost(router);
        definePut(router);
        defineDelete(router);
    }
    
    private void defineGetAll(Router router) {
        router.get(REST_API_CONTEXT + ":type").handler(routingContext -> {
            String type = routingContext.request().getParam("type");
            vertx.eventBus().request("get_" + type, null, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonArray results = (JsonArray) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(results.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }
    
    private void defineSearch(Router router) {
        router.post(REST_API_CONTEXT + "search").consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("search_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonArray results = (JsonArray) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(results.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    private void definePost(Router router) {
        router.post(REST_API_CONTEXT).consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("add_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonObject added = (JsonObject) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(added.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    private void definePut(Router router) {
        router.put(REST_API_CONTEXT).consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("edit_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonObject edited = (JsonObject) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(edited.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    private void defineDelete(Router router) {
        router.delete(REST_API_CONTEXT).consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("delete_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonObject deleted = (JsonObject) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(deleted.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }


    //</editor-fold>
    
    
    /**
     * Método ejecutado al arranque del verticle.
     *
     * @param promise
     * @param startFuture
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        if(requireRestApi)
            initRestApi(router);
        
        server.requestHandler(router).listen(
            //Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"), hndlr -> {
            8081, hndlr -> {
                if (hndlr.succeeded()) {
                    System.out.println("Server up n running");
                    promise.complete();
                } else {
                    promise.fail(hndlr.cause());
                }
            }//
        );
    }

    /**
     * Método llamado cuando se repliega el verticle
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        System.out.println("WebServer Verticle undeploy");
    }
}

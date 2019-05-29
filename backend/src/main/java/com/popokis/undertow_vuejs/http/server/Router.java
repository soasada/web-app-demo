package com.popokis.undertow_vuejs.http.server;

import com.popokis.undertow_vuejs.furniture.FurnitureHandler;
import com.popokis.undertow_vuejs.house.HouseHandler;
import com.popokis.undertow_vuejs.http.AuthorizationHandler;
import com.popokis.undertow_vuejs.user.UserHandler;
import io.undertow.Handlers;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import static io.undertow.Handlers.predicate;
import static io.undertow.Handlers.serverSentEvents;
import static io.undertow.predicate.Predicates.secure;

public final class Router {

  private Router() {}

  public static HttpHandler withHttpsRedirect(HttpHandler routes) {
    return Handlers.header(
        predicate(
            secure(),
            routes,
            (exchange) -> {
              exchange.getResponseHeaders().add(Headers.LOCATION, "https://" + exchange.getHostName() + ":" + (exchange.getHostPort() + 363) + exchange.getRelativePath());
              exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
            }
        ), "x-undertow-transport", ExchangeAttributes.transportProtocol()
    );
  }

  public static HttpHandler router() {
    final ServerSentEventHandler sseHandler = serverSentEvents();
    return Handlers.path()
        .addPrefixPath("/sse", sseHandler)
        // Unsecured endpoints
        .addPrefixPath("/api", Handlers.routing()
            .post("/login", UserHandler.login())
            // Health Checking
            .get("/health", Responses::ok)
            .get("/stream/users", UserHandler.streamUsers(sseHandler))
        )
        // HTTP based API
        .addPrefixPath("/api/v1", new AuthorizationHandler(
                Handlers.routing()
                    // User resources
                    .get("/users", UserHandler.all())
                    .post("/users", UserHandler.create())
                    .get("/users/{id}", UserHandler.read())
                    .put("/users", UserHandler.update())
                    .delete("/users/{id}", UserHandler.remove())
                    .get("/users/{id}/houses", UserHandler.findUserHouses())

                    // House resources
                    .get("/user/{id}/houses", HouseHandler.all())
                    .post("/houses", HouseHandler.create())
                    .get("/houses/{id}", HouseHandler.read())
                    .put("/houses", HouseHandler.update())
                    .delete("/houses/{id}", HouseHandler.remove())

                    // Furniture resources
                    .get("/house/{id}/furniture", FurnitureHandler.all())
                    .post("/furniture", FurnitureHandler.create())
                    .get("/furniture/{id}", FurnitureHandler.read())
                    .put("/furniture", FurnitureHandler.update())
                    .delete("/furniture/{id}", FurnitureHandler.remove())

                    .setFallbackHandler(Responses::notFound)
            )
        )
        // Redirect /about to root path to serve the index.html where the SPA lives
        .addExactPath("/about", Handlers.redirect("/"))
        // Serve all static files from a folder
        .addPrefixPath("/", new EncodingHandler.Builder().build(null).wrap(new ResourceHandler(
            new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), "public"))
            .setDirectoryListingEnabled(true)));
  }
}
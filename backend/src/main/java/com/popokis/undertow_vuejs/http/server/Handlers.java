package com.popokis.undertow_vuejs.http.server;

import com.popokis.undertow_vuejs.exception.Exceptions;
import com.popokis.undertow_vuejs.mapper.JsonMapper;
import com.popokis.undertow_vuejs.mapper.JsonMappers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.PathTemplateMatch;
import org.xnio.IoUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.LongFunction;

public final class Handlers {

  private Handlers() {}

  public static <R, S> HttpHandler bodyBased(Class<R> requestType, Function<R, S> f) {
    return exchange -> exchange.getRequestReceiver().receiveFullBytes((fullExchange, jsonBody) -> {
      fullExchange.dispatch(() -> {
        try {
          R request = JsonMappers.model(jsonBody, requestType);
          S response = f.apply(request);
          Responses.asJson(fullExchange, JsonMapper.getInstance().toJson(response));
        } catch (Exception e) {
          Responses.serverError(fullExchange, Exceptions.rootCause(e).getMessage());
        }
      });
    }, (errorExchange, exception) -> Responses.serverError(errorExchange, Exceptions.rootCause(exception).getMessage()));
  }

  public static <S> HttpHandler idBased(LongFunction<S> f) {
    return new BlockingHandler(
        exchange -> {
          try {
            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            long id = Long.parseLong(pathMatch.getParameters().get("id"));
            S response = f.apply(id);
            Responses.asJson(exchange, JsonMapper.getInstance().toJson(response));
          } catch (Exception e) {
            Responses.serverError(exchange, Exceptions.rootCause(e).getMessage());
          }
        }
    );
  }

  public static <S> HttpHandler listBased(Function<Void, List<S>> f) {
    return new BlockingHandler(
        exchange -> {
          try {
            List<S> response = f.apply(null);
            Responses.asJson(exchange, JsonMapper.getInstance().toJson(response));
          } catch (Exception e) {
            Responses.serverError(exchange, Exceptions.rootCause(e).getMessage());
          }
        }
    );
  }

  // See http://lists.jboss.org/pipermail/undertow-dev/2017-May/002019.html
  // https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/sse/ServerSentEventsServer.java
  // https://github.com/undertow-io/undertow/blob/master/core/src/test/java/io/undertow/server/handlers/sse/ServerSentEventTestCase.java
  public static <S> HttpHandler streamUsers(Function<Void, List<S>> f) {
    return new BlockingHandler(
        new ServerSentEventHandler(
            (connection, lastEventId) -> {
              List<S> response = f.apply(null);

              Flux.interval(Duration.ofMillis(250))
                  .zipWithIterable(response)
                  .doOnTerminate(() -> close(connection, lastEventId))
                  .subscribe(tuple -> connection.send(JsonMapper.getInstance().toJson(tuple.getT2()), "user", UUID.randomUUID().toString(), null));
            }
        ));
  }

  public static HttpHandler streamNumbers() {
    return new ServerSentEventHandler(
        (connection, lastEventId) -> Flux.interval(Duration.ofMillis(250))
            .take(10)
            .doOnTerminate(() -> close(connection, lastEventId))
            .subscribe(number -> connection.send(Long.toString(number), "number", Long.toString(number), null))
    );
  }

  private static void close(ServerSentEventConnection connection, String lastEventId) {
    connection.send("close", "close", lastEventId, new CloseSSEConnection());
  }

  private static class CloseSSEConnection implements ServerSentEventConnection.EventCallback {
    @Override
    public void done(ServerSentEventConnection connection, String data, String event, String id) {
      connection.shutdown();
    }

    @Override
    public void failed(ServerSentEventConnection connection, String data, String event, String id, IOException e) {
      IoUtils.safeClose(connection);
    }
  }
}

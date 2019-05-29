package com.popokis.undertow_vuejs.http.server;

import com.popokis.undertow_vuejs.exception.Exceptions;
import com.popokis.undertow_vuejs.mapper.JsonMapper;
import com.popokis.undertow_vuejs.mapper.JsonMappers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StringReadChannelListener;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class Handlers {

  private Handlers() {}

  public static <R, S> HttpHandler bodyBased(Class<R> requestType, Function<R, S> f) {
    return new BlockingHandler(
        exchange -> {
          try {
            String jsonBody = Requests.body(exchange);
            R request = JsonMappers.model(jsonBody, requestType);
            S response = f.apply(request);
            Responses.asJson(exchange, JsonMapper.getInstance().toJson(response));
          } catch (Exception e) {
            Responses.serverError(exchange, Exceptions.rootCause(e).getMessage());
          }
        }
    );
  }

  public static <S> HttpHandler idBased(Function<Long, S> f) {
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

  public static <S> HttpHandler streamUsers(Function<Void, List<S>> f, ServerSentEventHandler sseHandler) {
    return new BlockingHandler(
        exchange -> {
          // Get the list
          List<S> response = f.apply(null);
          new StringReadChannelListener(exchange.getConnection().getByteBufferPool()) {
            @Override
            protected void stringDone(String s) {
              for (ServerSentEventConnection h : sseHandler.getConnections()) {
                for (S user : response) {
                  try {
                    Thread.sleep(500);
                  } catch (InterruptedException e) {}
                  h.send(JsonMapper.getInstance().toJson(user), "user", UUID.randomUUID().toString(), null);
                }
              }
            }

            @Override
            protected void error(IOException e) {

            }
          }.setup(exchange.getRequestChannel());
        }
    );
  }
}
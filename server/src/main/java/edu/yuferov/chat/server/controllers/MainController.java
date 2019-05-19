package edu.yuferov.chat.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yuferov.chat.common.dto.requests.Request;
import edu.yuferov.chat.common.dto.responses.Response;
import edu.yuferov.chat.server.controllers.meta.RequestMapping;
import edu.yuferov.chat.server.security.SecurityContextHolder;
import edu.yuferov.chat.server.security.UserPrincipal;
import edu.yuferov.chat.server.io.DataListener;
import edu.yuferov.chat.server.services.SessionService;
import edu.yuferov.chat.server.services.exceptions.ServiceException;
import javafx.util.Pair;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static edu.yuferov.chat.common.Constants.EOF;

@Component
public class MainController implements DataListener {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private final SessionService sessionService;
    private final Map<String, Pair<Object, Method>> handlers = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MainController(ListableBeanFactory listableBeanFactory, SessionService sessionService) {
        this.sessionService = sessionService;
        fillHandlersInfo(listableBeanFactory);
    }

    @Override
    public byte[] parseData(byte[] data) throws IOException {
        Response.ResponseBuilder responseBuilder = Response.builder();
        try {
            Request request = objectMapper.readValue(data, Request.class);
            handleRequest(request, responseBuilder);
        } catch (IOException e) {
            responseBuilder
                    .status(400)
                    .error("bad request");
        }
        return objectMapper.writeValueAsString(responseBuilder.build()).getBytes();
    }

    private void handleRequest(Request request, Response.ResponseBuilder responseBuilder) {
        responseBuilder.path(request.getPath());
        try {
            fillSecurityContext(request);
            responseBuilder
                    .status(200)
                    .data(invokeHandler(request));
        } catch (ConstraintViolationException | ServiceException | IllegalArgumentException e) {
            log.warn("Bad request", e);
            responseBuilder
                    .status(400)
                    .error(e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while handle request", e);
            responseBuilder
                    .status(500)
                    .error("server error");
        } finally {
            sessionService.save();
            SecurityContextHolder.clearContext();
        }
    }

    private void fillSecurityContext(Request request) {
        String sessionId = request.getSession();
        if (sessionId == null) {
            return;
        }
        UserPrincipal userPrincipal = sessionService.loadUserBySessionId(sessionId);
        SecurityContextHolder.getContext().setPrincipal(userPrincipal);
    }

    private Object invokeHandler(Request request) {
        Pair<Object, Method> handler = handlers.get(request.getPath());
        if (handler == null) {
            throw new IllegalArgumentException("no such command " + request.getPath());
        }
        if (handler.getValue().getParameterCount() == 0) {
            return ReflectionUtils.invokeMethod(handler.getValue(), handler.getKey());
        }
        return ReflectionUtils.invokeMethod(handler.getValue(), handler.getKey(), request.getParams());
    }

    private void fillHandlersInfo(ListableBeanFactory listableBeanFactory) {
        Map<String, Object> controllers = listableBeanFactory.getBeansWithAnnotation(Controller.class);
        controllers.values().forEach(controller -> {
            RequestMapping classMapping = controller.getClass().getAnnotation(RequestMapping.class);
            ReflectionUtils.doWithMethods(controller.getClass(), method -> {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                String mapping = methodMapping.value();
                if (classMapping != null) {
                    mapping = classMapping.value() + mapping;
                }
                if (handlers.containsKey(mapping)) {
                    throw new Error("double mapping found: " + method.getName());
                }
                handlers.put(mapping, new Pair<>(controller, method));
            }, method -> method.isAnnotationPresent(RequestMapping.class));
        });
    }
}

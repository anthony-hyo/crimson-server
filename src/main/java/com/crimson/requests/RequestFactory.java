package com.crimson.requests;

import com.crimson.annotations.Request;
import com.crimson.interfaces.IRequest;
import com.crimson.requests.data.RequestData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class RequestFactory {

    private static final Logger log = LoggerFactory.getLogger(RequestFactory.class);
    private static final HashMap<String, RequestData> REQUESTS_DEFAULT = new HashMap<>();

    static {
        Reflections reflections = new Reflections("com.crimson.requests.calls");

        Set<Class<? extends IRequest>> requests = reflections.getSubTypesOf(IRequest.class);

        TreeSet<Class<? extends IRequest>> sortedRequests = new TreeSet<>(Comparator.comparing(Class::getName));

        sortedRequests.addAll(requests);

        sortedRequests
            .forEach(request -> {
                Request[] requestAnnotations = request.getDeclaredAnnotationsByType(Request.class);

                for (Request requestAnnotation : requestAnnotations) {
                    if (requestAnnotation == null) {
                        log.warn("Not found '{}'", request.getName());
                        continue;
                    }

                    log.info("'{}': '{}'", requestAnnotation.name(), request.getName());

                    //noinspection unchecked
                    RequestFactory.REQUESTS_DEFAULT.put(requestAnnotation.name(), new RequestData(requestAnnotation, (Class<IRequest>) request));
                }
            });
    }

    public static RequestData get(String request) {
        return RequestFactory.REQUESTS_DEFAULT.getOrDefault(request, RequestFactory.REQUESTS_DEFAULT.get("default"));
    }

}

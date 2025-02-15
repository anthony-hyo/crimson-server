package com.crimson.requests.data;

import com.crimson.annotations.Request;
import com.crimson.avatar.player.Player;
import com.crimson.interfaces.IRequest;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public record RequestData(Request annotation, Class<IRequest> request) {

    private static final Logger log = LoggerFactory.getLogger(RequestData.class);

    public IRequest requestInstance() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return request().getDeclaredConstructor().newInstance();
    }

    public void run(Player player, JSONObject jsonObject) {
        try {
            requestInstance().onRequest(player, jsonObject);
        } catch (Exception ex) {
            log.info("error on request {}", jsonObject, ex);
        }
    }

}

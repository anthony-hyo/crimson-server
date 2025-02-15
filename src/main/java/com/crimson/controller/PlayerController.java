package com.crimson.controller;

import com.crimson.avatar.player.Player;
import com.crimson.network.encoder.NetworkEncoder;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    public static final AttributeKey<Player> PLAYER_KEY = AttributeKey.valueOf("Player");

    public static final AtomicInteger COUNT = new AtomicInteger(0);

    private static final ConcurrentHashMap<Integer, Player> PLAYERS = new ConcurrentHashMap<>();

    public static void login(Channel channel, JSONObject json) {
        String name = json.getString("name");

//        Player exitingPlayer = PlayerController.find(name);
//
//        if (exitingPlayer.doesExist()) {
//            disconnect(exitingPlayer, "You logged in from a different location.");
//        }

        int networkId = PlayerController.COUNT.getAndIncrement();

        PLAYERS.put(networkId, new Player(networkId, name, channel, ));

        NetworkEncoder.dispatch(
            new JSONObject()
                .element("type", "login")
                .element("success", "message")
                .element("message", "Connected to the server"),
            channel
        );
    }

    public static void disconnect(Player player, String message) {
        PLAYERS.remove(player.data().id());

        if (player.network().channel().isActive()) {
            NetworkEncoder.dispatch(
                new JSONObject()
                    .element("type", "disconnect")
                    .element("message", message),
                player.network().channel()
            );

            player.network().channel().disconnect();
        }
    }

    public static void register(Channel channel, JSONObject json) {
        //TODO: register
    }

    public static Player find(String name) {
        String nameCase = name.toLowerCase(Locale.US);

        return PLAYERS.values()
            .stream()
            .filter(player -> player.data().name().equals(nameCase))
            .findFirst()
            .orElse(Player.NONE);
    }

    public static LinkedList<Channel> channels() {
        return PLAYERS.values()
            .stream()
            .map(player -> player.network().channel())
            .collect(Collectors.toCollection(LinkedList::new));
    }

}

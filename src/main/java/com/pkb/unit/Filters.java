package com.pkb.unit;

import static io.vavr.API.unchecked;

import java.util.Objects;

import com.pkb.unit.message.Message;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class Filters {
    public static <PT> Observable<Message> messages(Observable<Message> messages,
                                               Class<PT> payloadType,
                                               String target) {
        return messages.filter(payloadTypeFilter(payloadType))
                .filter(targetFilter(target));
    }

    public static <PT> Observable<PT> payloads(Observable<Message> messages,
                                               Class<PT> payloadType,
                                               String target) {
        return messages.filter(payloadTypeFilter(payloadType))
                .filter(targetFilter(target))
                .map(extractPayload());
    }

    public static <PT> Observable<PT> payloads(Observable<Message> messages, Class<PT> payloadType) {
        return messages.filter(payloadTypeFilter(payloadType))
                .map(extractPayload());
    }

    private static <T> Function<Message, T> extractPayload() {
        return msg ->
            unchecked(() ->
                (T)msg.payload().orElseThrow(() -> new IllegalStateException("no payload"))).get();
    }

    private static Predicate<Message> targetFilter(String target) {
        return msg ->
            (Boolean)msg.target()
                .map(msgTarget -> Objects.equals(msgTarget, target))
                .orElse(true);

    }

    private static Predicate<Message> payloadTypeFilter(Class payloadType) {
        return msg -> msg.messageType().equals(payloadType);
    }
}

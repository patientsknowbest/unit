package com.pkb.unit;

import static io.vavr.API.unchecked;

import java.util.Objects;

import com.pkb.unit.message.Message;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class Filters {

    private Filters() {}

    public static <T> Observable<Message> messages(Observable<Message> messages,
                                               Class<T> payloadType,
                                               String target) {
        return messages.filter(payloadTypeFilter(payloadType))
                .filter(targetFilter(target));
    }

    public static <T> Observable<T> payloads(Observable<Message> messages,
                                             Class<T> payloadType,
                                             String target) {
        return messages.filter(payloadTypeFilter(payloadType))
                .filter(targetFilter(target))
                .map(extractPayload());
    }

    public static <T> Observable<T> payloads(Observable<Message> messages, Class<T> payloadType) {
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

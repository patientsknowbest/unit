package com.pkb.unit;

import static io.vavr.API.unchecked;

import java.util.Objects;

import com.pkb.unit.message.Message;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * A set of commonly used filters on Observable streams of {@link Message}s.
 */
public class Filters {

    public enum MessageInclusionBehaviour {
        INCLUDE_UNTARGETED,
        EXCLUDE_UNTARGETED
    }

    private Filters() {}

    /**
     * Filters the Observable stream of messages that have the given payload type, target recepient
     * and returns the filtered stream of messages.
     * @param messages the Observable stream to filter
     * @param payloadType the payload type to filter by that is included in the message
     * @param target the identifier of the unit this message should be processed by
     * @param <T> the type of the payload this message carries
     * @return the Observable stream containing messages filtered by the given parameters
     */
    public static <T> Observable<Message> messages(Observable<Message> messages,
                                                   Class<T> payloadType,
                                                   String target) {
        return messages.filter(payloadTypeFilter(payloadType))
                .filter(targetFilter(target));
    }

    /**
     * Filters the Observable stream of messages that have the given payload type, target recepient
     * and returns the filtered stream of payloads.
     * @param messages the Observable stream to filter
     * @param payloadType the payload type to filter by that is included in the message
     * @param target the identifier of the unit this message should be processed by
     * @param <T> the type of the payload this message carries
     * @return the Observable stream containing payloads filtered by the given parameters
     */
    public static <T> Observable<T> payloads(Observable<Message> messages,
                                             Class<T> payloadType,
                                             String target) {
        return messages.filter(payloadTypeFilter(payloadType))
                .filter(targetFilter(target))
                .map(extractPayload());
    }

    /**
     * Filters the Observable stream of messages that have the given payload type, target recepient
     * and returns the filtered stream of payloads.
     * @param messages the Observable stream to filter
     * @param payloadType the payload type to filter by that is included in the message
     * @param <T> the type of the payload this message carries
     * @return the Observable stream conatining payloads filtered by the given parameters
     */
    public static <T> Observable<T> payloads(Observable<Message> messages,
                                             Class<T> payloadType) {
        return messages.filter(payloadTypeFilter(payloadType))
                .map(extractPayload());
    }

    private static <T> Function<Message, T> extractPayload() {
        return msg ->
                unchecked(() ->
                        (T)msg.payload().orElseThrow(() -> new IllegalStateException("no payload"))).get();
    }

    private static Predicate<Message> targetFilter(String target) {
        return targetFilter(target, MessageInclusionBehaviour.INCLUDE_UNTARGETED);
    }

    /**
     * Predicate that checks whether the message's target matches the provided.
     * @param target The provided target
     * @param inclusionBehaviour Whether messages without target have to be included
     * @return If the message's target matches the provided
     */
    @SuppressWarnings("unchecked")
    private static Predicate<Message> targetFilter(String target, MessageInclusionBehaviour inclusionBehaviour) {

        return msg ->
                (Boolean)msg.target()
                        .map(msgTarget -> Objects.equals(msgTarget, target))
                        .orElse(inclusionBehaviour == MessageInclusionBehaviour.INCLUDE_UNTARGETED);
    }

    private static Predicate<Message> payloadTypeFilter(Class payloadType) {
        return msg -> msg.messageType().equals(payloadType);
    }
}

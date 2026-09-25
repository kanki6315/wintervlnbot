package com.reverendracing.wintervlnbot.v2.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverendracing.wintervlnbot.util.model.DecisionNotification;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RaceControlCommandTests {
    private final DiscordApi api = mock(DiscordApi.class);
    private final Server server = mock(Server.class);
    private final ServerTextChannel channel = mock(ServerTextChannel.class);
    private final RaceControlCommand command = new RaceControlCommand(api);

    private void configureDiscord() {
        ReflectionTestUtils.setField(command, "serverId", "123");
        ReflectionTestUtils.setField(command, "protestAnnouncementChannel", "decisions");
        when(api.getServerById("123")).thenReturn(Optional.of(server));
        when(server.getTextChannelsByName("decisions")).thenReturn(List.of(channel));
        when(channel.sendMessage(any(EmbedBuilder.class))).thenReturn(CompletableFuture.completedFuture(null));
    }

    private DecisionNotification notification(String decision) {
        DecisionNotification notification = new DecisionNotification();
        notification.setIncidentNumber(42);
        notification.setDecision(decision);
        notification.setReason("Contact");
        notification.setPenalizedCarNumber("2");
        notification.setPenalizedCarName("Team Two");
        notification.setOtherCarName("Team One");
        notification.setPenalty("10 seconds");
        return notification;
    }

    @Test
    public void missingOrBlankDecisionsAreSkippedBeforeAccessingDiscord() throws Exception {
        command.handleDecisionNotification(null);
        command.handleDecisionNotification(new ObjectMapper().readValue("{\"incidentNumber\":42}", DecisionNotification.class));
        command.handleDecisionNotification(notification(null));
        command.handleDecisionNotification(notification(""));
        command.handleDecisionNotification(notification(" \t\n"));
        verifyNoInteractions(api);
    }

    @Test
    public void validDecisionsStillSendAfterMalformedNotifications() {
        configureDiscord();
        for (String decision : List.of("No Further Action", "Warning", "Penalty")) {
            command.handleDecisionNotification(notification(null));
            command.handleDecisionNotification(notification(decision));
        }
        verify(channel, times(3)).sendMessage(any(EmbedBuilder.class));
    }

    private Map<String, String> fields(EmbedBuilder embed) {
        Map<String, String> fields = new HashMap<>();
        embed.updateAllFields(field -> fields.put(field.getName(), field.getValue()));
        return fields;
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    public void blankReasonsAndPenaltiesProduceNonemptyEmbedFields(String blank) {
        configureDiscord();
        for (String decision : List.of("No Further Action", "Warning", "Penalty")) {
            var notification = notification(decision);
            notification.setReason(blank);
            notification.setPenalty(blank);
            command.handleDecisionNotification(notification);
        }
        var captor = ArgumentCaptor.forClass(EmbedBuilder.class);
        verify(channel, times(3)).sendMessage(captor.capture());
        var embeds = captor.getAllValues();
        for (var embed : embeds) {
            var values = fields(embed);
            assertEquals("Not provided", values.get("Reason"));
            values.forEach((name, value) -> {
                assertNotNull(value, name);
                assertFalse(value.isBlank(), name);
            });
        }
        assertFalse(fields(embeds.get(0)).containsKey("Penalty"));
        assertFalse(fields(embeds.get(1)).containsKey("Penalty"));
        assertEquals("Not provided", fields(embeds.get(2)).get("Penalty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"No Further Action", "Warning", "Penalty"})
    public void suppliedReasonAndPenaltyRemainUnchanged(String decision) {
        configureDiscord();
        command.handleDecisionNotification(notification(decision));
        var captor = ArgumentCaptor.forClass(EmbedBuilder.class);
        verify(channel).sendMessage(captor.capture());
        var values = fields(captor.getValue());
        assertEquals("Contact", values.get("Reason"));
        if (!"No Further Action".equals(decision)) {
            assertEquals("10 seconds", values.get("Penalty"));
        }
    }

    @Test
    public void synchronousDeliveryFailureDoesNotEscapeAndNextDecisionStillSends() {
        configureDiscord();
        when(channel.sendMessage(any(EmbedBuilder.class)))
                .thenThrow(new IllegalStateException("Discord unavailable"))
                .thenReturn(CompletableFuture.completedFuture(null));
        command.handleDecisionNotification(notification("Warning"));
        command.handleDecisionNotification(notification("Penalty"));
        verify(channel, times(2)).sendMessage(any(EmbedBuilder.class));
    }

    @Test
    public void asynchronousDeliveryFailureDoesNotEscapeAndNextDecisionStillSends() {
        configureDiscord();
        when(channel.sendMessage(any(EmbedBuilder.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Discord unavailable")))
                .thenReturn(CompletableFuture.completedFuture(null));
        command.handleDecisionNotification(notification("Warning"));
        command.handleDecisionNotification(notification("Penalty"));
        verify(channel, times(2)).sendMessage(any(EmbedBuilder.class));
    }

    @Test
    public void missingDiscordServerDoesNotEscapeTheCallback() {
        configureDiscord();
        when(api.getServerById("123")).thenReturn(Optional.empty());
        command.handleDecisionNotification(notification("Warning"));
        verifyNoInteractions(channel);
    }
}

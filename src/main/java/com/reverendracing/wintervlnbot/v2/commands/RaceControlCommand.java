package com.reverendracing.wintervlnbot.v2.commands;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.reverendracing.wintervlnbot.data.ClassRepository;
import com.reverendracing.wintervlnbot.data.DriverRepository;
import com.reverendracing.wintervlnbot.data.EntryCrewRepository;
import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.executors.RaceControlExecutor;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;
import com.reverendracing.wintervlnbot.util.model.DecisionNotification;
import com.reverendracing.wintervlnbot.util.model.ProtestNotification;
import com.reverendracing.wintervlnbot.util.model.TrackLimitsViolation;
import io.reactivex.Completable;
import me.s3ns3iw00.jcommands.Command;
import me.s3ns3iw00.jcommands.builder.SlashCommandBuilder;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Function;
import java.util.stream.Collectors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.*;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyFailed;

public class RaceControlCommand {

    private boolean qualiEnabled;

    @Value("${discord.qualifying.message_channel}")
    private String qualifyingAnnouncementChannel;
    @Value("${discord.protests.message_channel}")
    private String protestAnnouncementChannel;
    @Value("${discord.protests.race_control_channel}")
    private String raceControlPrivateChannel;

    @Value("${discord.username_listener.message_channel}")
    private String adminChannel;
    @Value("${discord.qualifying.api_endpoint}")
    private String restApiUrl;

    private DiscordApi api;
    private HubConnection socket;

    @Value("${discord.league_id}")
    private String leagueId;
    @Value("${discord.server_id}")
    private String serverId;
    @Value("${discord.roles.admin_role_id}")
    private String adminRoleId;
    @Value("${discord.roles.manager_role_id}")
    private String managerRoleId;
    @Value("${discord.roles.crew_role_id}")
    private String crewRoleId;
    @Value("${discord.roles.driver_role_id}")
    private String driverRoleId;

    private final Logger logger;

    public RaceControlCommand(DiscordApi api) {
        this.api = api;

        this.logger = LoggerFactory.getLogger(RaceControlCommand.class);

        qualiEnabled = false;
    }

    public Command generateStartSocket() {
        Server server = api.getServerById(serverId).get();
        SlashCommandBuilder startSocketCommand = new SlashCommandBuilder("startsocket", "Start Socket")
                .arguments()
                .onAction(event -> {
                    event.getResponder().respondLater().thenAccept(updater -> {
                        updater.setContent("Socket connection in progress.").update();
                        try {
                            if (startSocket(server)) {
                                makeAnnouncement("Session", "Open", getAnnouncementChannel(server));
                                event.getResponder().followUp()
                                        .setContent("Start Socket Complete!")
                                        .send();
                            } else {
                                event.getResponder().followUp()
                                        .setContent("Start Socket Failed.")
                                        .send();
                            }
                        } catch (Exception ex) {
                            logger.error("Start socket failed with " + ex.getMessage(), ex);
                            event.getResponder().followUp()
                                    .setContent("Start Socket Failed with exception " + ex.getMessage())
                                    .send();
                        }
                    });
                });

        return startSocketCommand.getCommand();
    }

    public Command generateStopSocket() {
        Server server = api.getServerById(serverId).get();
        SlashCommandBuilder stopSocketCommand = new SlashCommandBuilder("stopsocket", "Stop Socket")
                .arguments()
                .onAction(event -> {
                    event.getResponder().respondLater().thenAccept(updater -> {
                        updater.setContent("Socket connection in progress.").update();
                        try {
                            if (stopSocket(server)) {
                                makeAnnouncement("Session", "Closed", getAnnouncementChannel(server));
                                event.getResponder().followUp()
                                        .setContent("Stop Socket Complete!")
                                        .send();
                            } else {
                                event.getResponder().followUp()
                                        .setContent("Stop Socket Failed.")
                                        .send();
                            }
                        } catch (Exception ex) {
                            logger.error("Stop socket failed with " + ex.getMessage(), ex);
                            event.getResponder().followUp()
                                    .setContent("Stop Socket Failed with exception " + ex.getMessage())
                                    .send();
                        }
                    });
                });

        return stopSocketCommand.getCommand();
    }

    public Command generateRestartSocket() {
        Server server = api.getServerById(serverId).get();
        SlashCommandBuilder stopSocketCommand = new SlashCommandBuilder("restartsocket", "Restart Socket")
                .arguments()
                .onAction(event -> {
                    event.getResponder().respondLater().thenAccept(updater -> {
                        updater.setContent("Socket connection in progress.").update();
                        try {
                            if (restartSocket(server)) {
                                event.getResponder().followUp()
                                        .setContent("Restart Socket Complete!")
                                        .send();
                            } else {
                                event.getResponder().followUp()
                                        .setContent("Restart Socket Failed.")
                                        .send();
                            }
                        } catch (Exception ex) {
                            logger.error("Restart socket failed with " + ex.getMessage(), ex);
                            event.getResponder().followUp()
                                    .setContent("Restart Socket Failed with exception " + ex.getMessage())
                                    .send();
                        }
                    });
                });

        return stopSocketCommand.getCommand();
    }

    private void makeAnnouncement(final String session, final String state, final ServerTextChannel announcementChannel) {

        new MessageBuilder()
                .append(String.format("%s is now ", session))
                .append(state, MessageDecoration.BOLD)
                .append("!")
                .send(announcementChannel);
    }

    private boolean stopSocket(Server server) {

        boolean stopSocket = handleSocketConnection(HubConnection::stop, server);

        if(!stopSocket) {
            return false;
        }
        return true;
    }

    private boolean restartSocket(Server server) {

        boolean stopSocket = handleSocketConnection(HubConnection::stop, server);

        if(!stopSocket) {
            return false;
        }

        boolean startSocket = startSocket(server);

        if(!startSocket) {
            return false;
        }
        return true;
    }

    private boolean startSocket(Server server) {

        if(socket == null) {
            socket = buildConnectionAndMethods();
        }
        else if(isConnected()) {
            return true;
        }

        boolean startSocket = handleSocketConnection(HubConnection::start, server);

        if(!startSocket)
            return false;

        socket.send("AddToGroup", "Bot");
        LoggerFactory.getLogger(RaceControlExecutor.class).info(
                String.format("KeepAlive %d", socket.getKeepAliveInterval()));
        LoggerFactory.getLogger(RaceControlExecutor.class).info(
                String.format("ServerTimeout %d", socket.getServerTimeout()));
        return true;
    }

    private boolean isConnected() {
        if(socket == null)
            return false;
        return HubConnectionState.CONNECTED.equals(socket.getConnectionState());
    }

    private boolean handleSocketConnection(
            final Function<HubConnection, Completable> socketFunction,
            final Server server) {

        Completable completable = socketFunction.apply(socket);
        Throwable error = completable.blockingGet();

        if(error != null) {
            sendStackTraceToChannel(
                    "Unable to manage connection to hub.",
                    getChannelByName(adminChannel, server), error);
            return false;
        }
        return true;
    }

    private String getNumber(String str) {
        return String.format("%s%s","RC".equals(str) ? "" : "#", str);
    }

    private HubConnection buildConnectionAndMethods() {

        HubConnection connection = HubConnectionBuilder
                .create(restApiUrl)
                .build();
        connection.on("AnnounceProtest", (protestNotification) -> {
            Server server = api.getServerById(serverId).get();
            new MessageBuilder()
                    .append(String.format("#%d", protestNotification.getIncidentNumber()), MessageDecoration.BOLD)
                    .append(": Incident under investigation. Cars ")
                    .append(getNumber(protestNotification.getProtestingCarNumber()), MessageDecoration.BOLD)
                    .append(" & ")
                    .append(getNumber(protestNotification.getOffendingCarNumber()), MessageDecoration.BOLD)
                    .append(" - ")
                    .append(protestNotification.getReason())
                    .send(getAnnouncementChannel(server));
        }, ProtestNotification.class);
        connection.on("AnnounceDecision", (decisionNotification) -> {
            Server server = api.getServerById(serverId).get();

            if(decisionNotification.getDecision().equals("No Further Action")) {
                new MessageBuilder()
                        .append(String.format("#%d", decisionNotification.getIncidentNumber()), MessageDecoration.BOLD)
                        .append(": No Further Action. Cars ")
                        .append(getNumber(decisionNotification.getOtherCarNumber()), MessageDecoration.BOLD)
                        .append(" & ")
                        .append(getNumber(decisionNotification.getPenalizedCarNumber()), MessageDecoration.BOLD)
                        .append(" - ")
                        .append(decisionNotification.getReason())
                        .send(getAnnouncementChannel(server));
            } else if (decisionNotification.getDecision().equals("Warning")) {
                new MessageBuilder()
                        .append(String.format("#%d", decisionNotification.getIncidentNumber()), MessageDecoration.BOLD)
                        .append(": ")
                        .append(decisionNotification.getDecision())
                        .append(" - ")
                        .append(getNumber(decisionNotification.getPenalizedCarNumber()), MessageDecoration.BOLD)
                        .append(". ")
                        .append(decisionNotification.getReason())
                        .send(getAnnouncementChannel(server));
            }
            else {
                new MessageBuilder()
                        .append(String.format("#%d", decisionNotification.getIncidentNumber()), MessageDecoration.BOLD)
                        .append(": ")
                        .append(decisionNotification.getDecision())
                        .append(" - ")
                        .append(getNumber(decisionNotification.getPenalizedCarNumber()), MessageDecoration.BOLD)
                        .append(" : ")
                        .append(decisionNotification.getPenalty(), MessageDecoration.BOLD)
                        .append(". ")
                        .append(decisionNotification.getReason())
                        .send(getAnnouncementChannel(server));
            }
        }, DecisionNotification.class);
        connection.on("SubmitSingleTrackLimitViolationDetected", (trackLimitsViolation) -> {
            Server server = api.getServerById(serverId).get();
            new MessageBuilder()
                    .append(String.format("#%s", trackLimitsViolation.getCarNumber()), MessageDecoration.BOLD)
                    .append(": Single Track Limit Violation Detected on Lap: " + trackLimitsViolation.getLapNumbers()
                            .stream().map(l -> Long.toString(l)).collect(Collectors.joining(", ")))
                    .send(getRaceControlAnnouncementChannel(server));
        }, TrackLimitsViolation.class);
        connection.on("SubmitMultipleTrackLimitViolationsDetected", (trackLimitsViolation) -> {
            Server server = api.getServerById(serverId).get();
            new MessageBuilder()
                    .append(String.format("#%s", trackLimitsViolation.getCarNumber()), MessageDecoration.BOLD)
                    .append(": Multiple Track Limit Violations Detected on Laps: " + trackLimitsViolation.getLapNumbers()
                            .stream().map(l -> Long.toString(l)).collect(Collectors.joining(", ")))
                    .send(getRaceControlAnnouncementChannel(server));
        }, TrackLimitsViolation.class);

        return connection;
    }

    private ServerTextChannel getQualifyingChannel(Server server) {
        return getChannelByName(qualifyingAnnouncementChannel, server);
    }

    private ServerTextChannel getAnnouncementChannel(Server server) {
        return getChannelByName(protestAnnouncementChannel, server);
    }

    private ServerTextChannel getRaceControlAnnouncementChannel(Server server) {
        return getChannelByName(raceControlPrivateChannel, server);
    }
}

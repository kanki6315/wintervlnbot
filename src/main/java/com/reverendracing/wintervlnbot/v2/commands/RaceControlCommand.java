package com.reverendracing.wintervlnbot.v2.commands;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.reverendracing.wintervlnbot.util.model.DecisionNotification;
import com.reverendracing.wintervlnbot.util.model.ProtestNotification;
import com.reverendracing.wintervlnbot.util.model.TrackLimitsUpdate;
import io.reactivex.Completable;
import me.s3ns3iw00.jcommands.type.ServerCommand;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.getChannelByName;
import static com.reverendracing.wintervlnbot.util.MessageUtil.sendStackTraceToChannel;

public class RaceControlCommand {

    @Value("${discord.channels.protest_message_channel}")
    private String protestAnnouncementChannel;
    @Value("${discord.protests.race_control_channel}")
    private String raceControlPrivateChannel;

    @Value("${discord.channels.admin_channel}")
    private String adminChannel;
    @Value("${artifactracing.api_endpoint}")
    private String restApiUrl;

    private final DiscordApi api;
    private HubConnection socket;

    @Value("${discord.server_id}")
    private String serverId;
    @Value("${discord.roles.raceControl_role_id}")
    private String raceControlRoleId;
    @Value("${discord.roles.admin_role_id}")
    private String adminRoleId;
    @Value("${discord.roles.manager_role_id}")
    private String managerRoleId;
    @Value("${discord.roles.crew_role_id}")
    private String crewRoleId;
    @Value("${discord.roles.driver_role_id}")
    private String driverRoleId;

    private final static Logger logger = LoggerFactory.getLogger(RaceControlCommand.class);

    public RaceControlCommand(DiscordApi api) {
        this.api = api;
    }

    public ServerCommand generateStartSocket() {
        Server server = api.getServerById(serverId).get();
        ServerCommand startSocketCommand = new ServerCommand("startsocket", "Start Socket");

        startSocketCommand
                .setOnAction(event -> {
                    event.getResponder().respondLater().thenAccept(updater -> {
                        updater.setContent("Socket connection in progress.").update();
                        try {
                            var channel = getAnnouncementChannel(server);
                            if (startSocket(server)) {
                                var embed = new EmbedBuilder()
                                        .setTitle("Session is now open")
                                        .setDescription("Round 2 | Sebring")
                                        .setColor(Color.GREEN);
                                channel.sendMessage(embed);
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

        return startSocketCommand;
    }

    public ServerCommand generateStopSocket() {
        Server server = api.getServerById(serverId).get();
        ServerCommand stopSocketCommand = new ServerCommand("stopsocket", "Stop Socket");

        stopSocketCommand
                .setOnAction(event -> {
                    event.getResponder().respondLater().thenAccept(updater -> {
                        updater.setContent("Socket connection in progress.").update();
                        var channel = getAnnouncementChannel(server);
                        try {
                            if (stopSocket(server)) {
                                var embed = new EmbedBuilder()
                                        .setTitle("Session is now closed")
                                        .setDescription("Round 2 | Sebring")
                                        .setColor(Color.BLACK);
                                channel.sendMessage(embed);
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

        return stopSocketCommand;
    }

    public ServerCommand generateRestartSocket() {
        Server server = api.getServerById(serverId).get();
        ServerCommand stopSocketCommand = new ServerCommand("restartsocket", "Restart Socket");

        stopSocketCommand
                .setOnAction(event -> {
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

        return stopSocketCommand;
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
        LoggerFactory.getLogger(RaceControlCommand.class).info(
                String.format("KeepAlive %d", socket.getKeepAliveInterval()));
        LoggerFactory.getLogger(RaceControlCommand.class).info(
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
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(String.format("Incident Report - #%d", protestNotification.getIncidentNumber()))
                    .setDescription(protestNotification.getReason())
                    .addInlineField("Filed By", String.format("#%s %s", protestNotification.getProtestingCarNumber(), protestNotification.getProtestingCarName()))
                    .addInlineField("Under Review", String.format("#%s %s", protestNotification.getOffendingCarNumber(), protestNotification.getOffendingCarName()))
                    .addField("Description", protestNotification.getDescription())
                    .setColor(Color.YELLOW);
            var channel = getAnnouncementChannel(server);
            channel.sendMessage(embed);
        }, ProtestNotification.class);
        connection.on("AnnounceDecision", (decisionNotification) -> {
            Server server = api.getServerById(serverId).get();

            var embed = new EmbedBuilder();
            var channel = getAnnouncementChannel(server);
            if(decisionNotification.getDecision().equals("No Further Action")) {
                embed.setTitle(String.format("Incident Decision - #%d", decisionNotification.getIncidentNumber()))
                        .setDescription(decisionNotification.getDecision())
                        .addField("Reason", decisionNotification.getReason())
                        .addField("Involved Cars", String.format("%s + %s", decisionNotification.getPenalizedCarName(), decisionNotification.getOtherCarName()))
                        .setColor(Color.GREEN);
            } else if (decisionNotification.getDecision().equals("Warning")) {
                embed.setTitle(String.format("Incident Decision - #%d", decisionNotification.getIncidentNumber()))
                        .setDescription(decisionNotification.getDecision())
                        .addField("Reason", decisionNotification.getReason());
                if (StringUtils.isNotEmpty(decisionNotification.getPenalty())) {
                    embed.addInlineField("Penalty", decisionNotification.getPenalty());
                }
                embed.addInlineField("Warned Car", String.format("#%s %s", decisionNotification.getPenalizedCarNumber(), decisionNotification.getPenalizedCarName()))
                        .setColor(Color.ORANGE);
            }
            else {
                embed.setTitle(String.format("Incident Decision - #%d", decisionNotification.getIncidentNumber()))
                        .setDescription(decisionNotification.getDecision())
                        .addField("Reason", decisionNotification.getReason())
                        .addInlineField("Penalty", decisionNotification.getPenalty())
                        .addInlineField("Penalized Car", String.format("#%s %s", decisionNotification.getPenalizedCarNumber(), decisionNotification.getPenalizedCarName()))
                        .setColor(Color.RED);
            }
            channel.sendMessage(embed);
        }, DecisionNotification.class);
        connection.on("PostTrackLimitViolationDetected", (trackLimitsUpdate) -> {
            if (trackLimitsUpdate.getNumIncidents() > 0 && trackLimitsUpdate.getNumIncidents() % 10 == 0) {
                Server server = api.getServerById(serverId).get();
                AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                        .setMentionRoles(true)
                        .build();

                var role = server.getRoleById(raceControlRoleId).get();
                new MessageBuilder()
                        .setAllowedMentions(allowedMentions)
                        .append(role.getMentionTag())
                        .append(String.format(" #%s | %s", trackLimitsUpdate.getCarNumber(), trackLimitsUpdate.getTeamName()), MessageDecoration.BOLD)
                        .append(": " + (trackLimitsUpdate.isPractice() ? "Practice " : "Race ") + "Track Limit Violation Detected. Total Count: " + trackLimitsUpdate.getNumIncidents())
                        .send(getRaceControlAnnouncementChannel(server));
            }
        }, TrackLimitsUpdate.class);

        return connection;
    }

    private ServerTextChannel getAnnouncementChannel(Server server) {
        return getChannelByName(protestAnnouncementChannel, server);
    }

    private ServerTextChannel getRaceControlAnnouncementChannel(Server server) {
        return getChannelByName(raceControlPrivateChannel, server);
    }
}

package com.reverendracing.wintervlnbot.service.executors;

import com.reverendracing.wintervlnbot.data.*;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.springframework.data.domain.Sort;

import java.awt.*;
import java.util.List;

import static com.reverendracing.wintervlnbot.util.MessageUtil.*;

public class IndyQExecutor implements CommandExecutor {

    private boolean queueOpen;

    private String indyQPostChannel;
    private String indyQRequestChannel;
    private String adminChannel;

    private QueueRequestRepository queueRepository;
    private DriverRepository driverRepository;

    private long queueMessageId;

    public IndyQExecutor(
            final QueueRequestRepository queueRepository,
            final DriverRepository driverRepository,
            final String indyQPostChannel,
            final String indyQRequestChannel,
            final String adminChannel) {
        this.queueRepository = queueRepository;
        this.driverRepository = driverRepository;
        this.indyQPostChannel = indyQPostChannel;
        this.indyQRequestChannel = indyQRequestChannel;
        this.adminChannel = adminChannel;

        this.queueOpen = false;
        queueMessageId = -1;
    }

    @Command(aliases = "!leaveQueue", description = "Leave the qualifying queue", usage = "!leaveQueue [Car Number]", showInHelpPage = true)
    public void onLeaveQueueRequest(String[] args, Message message, Server server, ServerTextChannel channel, User user) {
        ServerTextChannel announcementChannel = getQueueChannel(server);
        if(channel.getId() != announcementChannel.getId())
            return;


        if(!queueOpen) {
            new MessageBuilder()
                    .append("Queue is not open right now.")
                    .send(announcementChannel);
            return;
        }

        if(args.length > 1) {
            new MessageBuilder()
                    .append("Please only provide your car number")
                    .send(announcementChannel);
            return;
        }
        if(args.length == 0) {
            new MessageBuilder()
                    .append("Please provide your car number")
                    .send(announcementChannel);
            return;
        }
        String numberString = args[0].replace("#", "");

        List<Role> userRoles = user.getRoles(server);
        if(userRoles.size() == 0 ||
                userRoles.stream()
                        .noneMatch(r -> r.getName().equalsIgnoreCase(String.format("#%s", numberString)))) {
            new MessageBuilder()
                    .append(String.format("User is not permitted to queue #%s", numberString))
                    .send(announcementChannel);
            return;
        }

        List<QueueRequest> requests = queueRepository.findByCarNumber(numberString);
        if(requests.size() == 0) {
            new MessageBuilder()
                    .append(String.format("Car %s is not in queue", numberString))
                    .send(announcementChannel);
            return;
        }
        queueRepository.deleteAll(requests);
        updateQueueEmbed(server);
        notifyChecked(message);
    }

    @Command(aliases = "!joinQueue", description = "Join the qualifying queue", usage = "!joinQueue [Car Number]", showInHelpPage = true)
    public void onJoinQueueRequest(String[] args, Message message, Server server, ServerTextChannel channel, User user) {
        ServerTextChannel announcementChannel = getQueueChannel(server);
        if(channel.getId() != announcementChannel.getId())
            return;

        if(!queueOpen) {
            new MessageBuilder()
                    .append("Queue is not open right now.")
                    .send(announcementChannel);
            return;
        }

        if(args.length > 1) {
            new MessageBuilder()
                    .append("Please only provide your car number")
                    .send(announcementChannel);
            return;
        }
        if(args.length == 0) {
            new MessageBuilder()
                    .append("Please provide your car number")
                    .send(announcementChannel);
            return;
        }
        String numberString = args[0].replace("#", "");

        List<Driver> drivers = driverRepository.findByEntry_CarNumber(numberString);
        if(drivers.size() == 0) {
            new MessageBuilder()
                    .append(String.format("Car number #%s is not registered", numberString))
                    .send(announcementChannel);
            return;
        }

        List<Role> userRoles = user.getRoles(server);
        if(userRoles.size() == 0 ||
                userRoles.stream()
                        .noneMatch(r -> r.getName().equalsIgnoreCase(String.format("#%s", numberString)))) {
            new MessageBuilder()
                    .append(String.format("User is not permitted to queue #%s", numberString))
                    .send(announcementChannel);
            return;
        }

        List<QueueRequest> requests = queueRepository.findByCarNumber(numberString);
        if(requests.size() > 0) {
            new MessageBuilder()
                    .append(String.format("Car %s is already in queue", numberString))
                    .send(announcementChannel);
            return;
        }

        QueueRequest request = new QueueRequest();
        request.setCarNumber(numberString);
        request.setCreationTimestamp(message.getCreationTimestamp().toEpochMilli());
        request.setUserMentionTag(user.getMentionTag());
        request.setDriverName(drivers.get(0).getDriverName());
        queueRepository.save(request);
        updateQueueEmbed(server);
        notifyChecked(message);
    }

    @Command(aliases = "!nextDriver", showInHelpPage = false)
    public void onNextDriverAdminCommand(String[] args, Message message, Server server, ServerTextChannel channel, User user) {
        ServerTextChannel announcementChannel = getQueueChannel(server);

        if(!hasAdminPermission(server, user))
            return;

        List<QueueRequest> requests = queueRepository.findAll(Sort.by(Sort.Direction.ASC, "creationTimestamp"));
        if(requests.size() == 0) {
            notifyChecked(message);
            return;
        }
        QueueRequest toBeDeleted = requests.get(0);
        if(requests.size() == 1) {
            queueRepository.delete(toBeDeleted);
            new MessageBuilder()
                    .append("Qualifying Queue is now empty")
                    .send(announcementChannel);
            notifyChecked(message);
            return;
        }
        QueueRequest nextDriver = requests.get(1);
        queueRepository.delete(toBeDeleted);
        new MessageBuilder()
                .append(String.format("#%s %s is next on deck for qualifying. " +
                        "Please wait until Race Control releases you to leave your pit box.", nextDriver.getCarNumber(), nextDriver.getUserMentionTag()))
                .send(announcementChannel);
        updateQueueEmbed(server);
        notifyChecked(message);
    }

    private void updateQueueEmbed(Server server) {
        List<QueueRequest> requests = queueRepository.findAll(Sort.by(Sort.Direction.ASC, "creationTimestamp"));
        ServerTextChannel postChannel = getPostChannel(server);
        if(queueMessageId != -1) {
            Message scheduleMessage = postChannel.getMessageById(queueMessageId).join();
            scheduleMessage.edit("Qualifying Run Order", constructQueueEmbed(requests));
        } else {
            new MessageBuilder()
                    .append("Qualifying Run Order")
                    .setEmbed(constructQueueEmbed(requests))
                    .send(postChannel);
        }
    }

    @Command(aliases = "!setqueuemessage", description = "Enable qualifying for all users", showInHelpPage = false)
    public void onSetQueueMessage(String[] args, Message message, Server server, User user, TextChannel channel) {

        if(!hasAdminPermission(server, user))
            return;
        this.queueMessageId = Long.parseLong(args[0]);
        notifyChecked(message);
    }

    @Command(aliases = "!enablequeue", description = "Enable qualifying for all users", showInHelpPage = false)
    public void onQualiEnable(Message message, Server server, User user, TextChannel channel) {

        if(!hasAdminPermission(server, user))
            return;

        queueOpen = true;
        makeAnnouncement("Qualifying Queue", "Open!", getQueueChannel(server));
        notifyChecked(message);
    }

    @Command(aliases = "!disablequeue", description = "Disable qualifying for all users", showInHelpPage = false)
    public void onQualiDisable(Message message, Server server, User user, TextChannel channel) {

        if(!hasAdminPermission(server, user))
            return;

        queueOpen = false;
        makeAnnouncement("Qualifying Queue", "Closed!", getQueueChannel(server));
        notifyChecked(message);
    }

    private EmbedBuilder constructQueueEmbed(List<QueueRequest> requests) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("ISOWC 500 Qualifying Queue")
                .setColor(Color.RED);
        if(requests.size() == 0) {
            builder.addField("Queue", "Qualifying Queue is Currently Empty");
        } else {
            int queueSize = Math.min(requests.size(), 25);
            for (int x = 0; x < queueSize; x++) {
                QueueRequest request = requests.get(x);
                builder.addField(String.format("%d)", x+1), String.format("#%s - %s", request.getCarNumber(), request.getDriverName()));
            }
        }

        return builder;
    }

    private void makeAnnouncement(final String session, final String state, final ServerTextChannel announcementChannel) {
        new MessageBuilder()
                .append(String.format("%s is now ", session))
                .append(state, MessageDecoration.BOLD)
                .append("!")
                .send(announcementChannel);
    }

    private ServerTextChannel getQueueChannel(Server server) {
        return getChannelByName(indyQRequestChannel, server);
    }

    private ServerTextChannel getPostChannel(Server server) {
        return getChannelByName(indyQPostChannel, server);
    }
}

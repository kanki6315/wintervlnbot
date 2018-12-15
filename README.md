# Winter-VLN Bot

A Discord bot built using Spring Boot, Javacord3 and SDCF4J. Also uses a SignalR WebSocket to connect to a website to allow for Leagues to build out iRacing Race Control procedures using the bot.

## Functionality

This bot provides tools for league organizers to manage their league discord through a Discord bot, and also enables Race Control features from within discord for league organizers.
* Nickname listener sends a message to a configured Admin channel, allowing Admins to add an emoji reaction to automatically assign a configured Role to a user
* Allows teams to convert session times to their local timezones
* Enables a "black flag clearance" request for qualifying. This is used for the Nurburbring-Nordschliefe, allowing teams to shortcut the GP circuit instead of doing a full 7+ minute outlap.

### General Command List
* `!session [Timezone Code | optional]`
* `!entryList`
* `!help`
### Qualifying Commands
* `!enableQuali` - Admin only
* `!disableQuali` - Admin only
* `!restartSocket` - Admin only
* `!q [Car Number] [S (Solo) | optional]`

## Getting Started

### Pre-Requisites

Before you can begin, you need to create a discord bot and add the token to the application.properties file.[You can find instructions here] (https://javacord.org/wiki/essential-knowledge/creating-a-bot-account/). This guide will also show you how to invite the bot into your discord server.

If you wish to have any of the prior functionality built into the bot, you will need to enter your own SignalR hub endpoint that implements the 2 socket calls we use. These are described in the Socket section later.

You should also replace the channel and roles currently listed in the application.properties file

### Building locally

Begin by cloning the repo. If you do not have gradle installed, [follow these instructions](https://gradle.org/install/).

Once you have the project locally and have inserted the relevant properties into application.properties, run the following command to build the project.

```
./gradlew build
```

Once you have the jar built, you can run the project with the following command

```
java -jar build/libs/winter-vln-bot-<VERSION>.jar
```

### Deploying to server

We recommend building the jar on your server, and creating a Discord bot specifically for the server where this token is only used on this server. If you run into errors building the jar on your server due to memory constraints, try running the following.
```
./gradlew build --no-daemon
```

To run the bot as a persistent service on your system, we recommend installing the application as a service. This does rely on your system having systemctl. If your machine does not have systemctl, you will need to run the bot persistently another way. Begin by creating a script that will start your bot for you. You will need to give this file permissions as well
```
#!/bin/sh
sudo /usr/bin/java -jar <path>/build/libs/winter-vln-bot-0.0.1-SNAPSHOT.jar
```
```
chmod u+x startBot.sh
```

Create the service start file, and configure it correctly.
```sudo vi /etc/systemd/system/wintervlnbot.service```

```
[Unit]
Description=Winter-VLN Bot
After=syslog.target

[Service]
User=root
ExecStart=/<path>/startBot
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

After this, you can update the systemd configuration to read your new service, enable it, and then start it. You can also view the logs with the last command.
```
sudo systemctl daemon-reload
sudo systemctl enable wintervlnbot
sudo systemctl start wintervlnbot
sudo journalctl -f -u wintervlnbot
```


## Socket
This bot connects to a SignalR hub. This hub powers a small website for Race Control to use to view black flag requests. On connecting to the hub, we make an initial message to add this connection to a specific group. If you do not implement this call, you will receive all the messages that the SignalR hub sends to the clients connected to the website.
```
socket.send("AddToGroup", "Bot");
```
The actual command for sending a black flag request is very simple. We use a POJO that represents the driver number, and if they are a solo driver.
```
socket.send("AddBlackFlag", new BlackFlagMessage(number, args.length > 1));
```
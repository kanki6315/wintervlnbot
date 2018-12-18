# Winter-VLN Bot

A Discord bot built using Spring Boot, Javacord3 and SDCF4J. Uses a H2 In-Memory to cache an entry list - the entry list is currently read in using the Google Sheets Java API. Uses a SignalR WebSocket to connect to a website to allow for Leagues to build out iRacing Race Control procedures using the bot.

## Functionality

This bot provides tools for league organizers to manage their league discord through a Discord bot, and also enables Race Control features from within discord for league organizers.
* Configure a certain Role as the standard Role for a registered Driver. This is used to verify the user has permission to perform certain restricted actions
* Nickname listener sends a message to a configured Admin channel, allowing Admins to add an emoji reaction to automatically assign the configured Role to a user
* Allows teams to convert session times to their local timezones
* Enables teams to request for forms as needed, instead of managing a channel of forms
* Reads entry list from Google Sheets, and allows teams to query and validate they are registered correctly.
* Enables a "black flag clearance" request for qualifying. This is used for the Nurburbring-Nordschliefe, allowing teams to shortcut the GP circuit instead of doing a full 7+ minute outlap.

### General Command List
* `!session [Timezone Code | optional]`
* `!entryList`
* `!changeForm` - Restricted to certain roles
* `!help`
### Query Commands
All these commands are restricted to the role that can be configured in the properties file.
* `!team [Team Name or Number]`
* `!driver [Team Name or Number]`
### Qualifying Commands
The qualifying managment commands can only be issued by an admin. Once qualifying is enabled, only messages sent to a specific channel that is configured in the properties file will be read by the bot.
* `!enableQuali` - Admin only
* `!disableQuali` - Admin only
* `!restartSocket` - Admin only
* `!q [Car Number] [S (Solo) | optional]`

## Getting Started

### Pre-Requisites

Before you can begin, you need to create a discord bot and add the token to the application.properties file. [You can find instructions here](https://javacord.org/wiki/essential-knowledge/creating-a-bot-account/). This guide will also show you how to invite the bot into your discord server.

If you wish to have any of the prior functionality built into the bot, you will need to enter your own SignalR hub endpoint that implements the 2 socket calls we use. These are described in the Socket section later.

The Sheets functionality is described in further detail later, but if you wish to use this functionality without updating the bot, you will need to ensure that your Sheet has the exact columns that are listed in the `CsvInputs.java` file to avoid parsing errors. To get credentials for your application, you will need to start by generating a `credentials.json` - [click on the link](https://developers.google.com/sheets/api/quickstart/java) and then the button that says `Enable the API` and download the file to your resources folder.

You should also replace the channels and role currently listed in the application.properties file with the names of the Role + channels that you have configured in your Discord Server.

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
sudo /usr/bin/java -jar <path>/build/libs/winter-vln-bot-<VERSION>.jar
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

### Failing Build
If you find your build is getting stuck, try adding the debug option to your build. It is likely that the "tests" are trying to start your application with its full context, and getting stuck because it is trying to connect to Google Sheets and waiting for its callback. If so, you can connect, and the build will finish.
```
./gradlew build --no-daemon --debug
```

If your build is failing with a null pointer or similar error, it is likely that you are missing the `credential.json` file. 

Since the "test" is spinning up the full ApplicationContext, you will get errors if you try to build with the bot already running persistently as currently configured. You will need to stop the bot in order to build it.
## Socket
This bot connects to a SignalR hub. This hub powers a small website for Race Control to use to view black flag requests. On connecting to the hub, we make an initial message to add this connection to a specific group. If you do not implement this call, you will receive all the messages that the SignalR hub sends to the clients connected to the website.
```
socket.send("AddToGroup", "Bot");
```
The actual command for sending a black flag request is very simple. We use a POJO that represents the driver number, and if they are a solo driver.
```
socket.send("AddBlackFlag", new BlackFlagMessage(number, args.length > 1));
```

## Sheets
### Model
The bot currently reads all entries direct from a Google Sheet spreadsheet that the league organizers have built to accept input from a Google Form. As described earlier, to avoid parsing errors, you will have to ensure you have the exact column names in your sheet. If you wish to just change the header for a specific column, you can do this by updating the header value in `CsvInputs`. If you wish to add or remove fields, you will need to update both the `CsvInputs` and `Entry` file to represent the data correctly.
### Connecting to Sheets
When you start up the bot for the first time, it will output an oauth url and deploy a listener on `localhost` and wait for a callback. This blocks the bot from completing startup until you finish the oauth callback step. If you are running on a server, you must copy this link to your local browser, and then copy the redirect url back to the server and run it using curl in a separate browser. After you have authenticated for the first time, a `tokens` directory is created to store a refresh token, meaning you should not have to perform this step again. 
### Queries & Output Formatting
Currently, each row of our Entry List Google Sheet is read into our local database as a single `Entry` entity. With the SDCF4J library, we get each individual argument into the query in an array. At this time, we just combine this array into a single search query, and query the team name and team number. We do this using Spring Queries which translate from a function name to an SQL query `findByCarNumberLikeOrTeamNameContainsIgnoreCase`. [For more information on Spring Repositories, click here](https://docs.spring.io/spring-data/jpa/docs/1.4.2.RELEASE/reference/html/jpa.repositories.html). Much more complicated queries could be built out, and we could even support breaking out drivers into their own entity, and allow querying on them. 

Output formatting is done using the `console-table-builder` library and has been broken out into the `QueryFormatter` class. We iterate over all matching entries to produce individual ordered Lists to store each columns information. We can then use the Java Stream API to easily determine the length of the longest value in each column to dynamically determine the width for each column.
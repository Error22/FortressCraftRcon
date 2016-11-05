# FortressCraftRcon
A basic java rcon api that supports FortressCraft Evolved. Due to FCE incorrectly implementing the rcon standard I have created this client that sends the packets in this incorrect format.

#Usage
```
FortressCraftRcon rcon = new FortressCraftRcon();
rcon.connect(IP, PORT, PASSWORD);
rcon.sendCommand("Some raw command");
rcon.sendChatMessage("Server is stopping...");
rcon.stopServer();
```

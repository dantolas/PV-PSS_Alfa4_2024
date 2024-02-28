
# Project alfa 4 -- Network P2P  chat with a web UI
## School project for SPŠE Ječná (Střední průmyslová škola elektrotechnická, Praha 2, Ječná 30)
## AUTHOR: Kuta Samuel C4b 
## [Github Repo](https://github.com/dantolas/PV-PSS_Alfa4_2024)

## Table of contents (TOC)
==========================
1. [Introduction](#introduction)
2. [Requirements](#requirements)
3. [Installation](#installation)
4. [Usage](#usage)
5. [Configurations](#configurations)
6. [Docs](#docs)
7. [Architecture and design patterns](#architecture-and-design-patterns)
8. [Application runtime behavior](#app-runtime-behaviour)
9. [Dependencies](#dependencies)
9. [Shortcomings](#shortcomings,failures+and+things+to+be+improved)

## Quick Intro
- This application runs a UDPServer and a TCPServer on selected local network.
It attempts to first find other peers through UDP Discovery (sending UDP packets to network 
broadcast)
- Once an answer has been received, a TCP Connection attempt is made to the respondent's IPv4 addr.
- After a TCP connection is established, the application provides a web UI and RESTAPI endpoints for
sending messages and seeing or receiving the message history.
## Requirements
`Java` - version *20.0.1*+
`Gradle` - version *9.4*+ 
## Installation
- Clone this repository from the command line
`git clone address <directory>`

Or download the entire repository as a zip file
See [Requirements](#requirements)

## Usage
- Navigate to the project directory and execute this command:
`./gradlew run`
- The application will start and u will see console output informing u about the program runtime
behavior. 
- After u start the program u can access the [Webpage](http://localhost:8000/)

- I didn't manage to get a .jar archive executable because the way to create them by official
Spring boot docs or any other forums suggestion I could google just did not work.

However I have provided the **chat.service** file to turn any working .jar file into a linux service
that can be started and managed with **systemctl**

The problem might be with the built in tomcat web server not packaging correctly, but honestly I
have no clue and couldn't figure it out.


## Configurations
- U can configure various aspects of the application in the **conf/config.json** file.
- Make sure this file exists and it's struture complies with the examples below.

- **config** file example:


```
{
    "peer_id":"kuta",
    "udp_timeout":30000,
    "broadcast_frequency_milis":5000,
    "tcp_client_timeout":10000,
    "tcp_listener_timeout":10000,
    "msg_limit_minute":50,
    "ipv4_addr":"auto"
}
```

- **peer_id**: Your name in app
- **udp_timeout**: Default timeout for UDP sending and receiving packets. **MILISECONDS**
- **broadcast_frequency**: How often to send the UDP broadcast. **MILISECONDS**
- **tcp_client_timeout**: Default timeout for your clients connected to other peers. **MILISECONDS**
- **tcp_listener_timeout**: Default timeout for other peers's clients connected to you**MILISECONDS**
- **msg_limit_minute**: Limits every client connected to the server to only send X msgs a minute**MILISECONDS**
- **ipv4_addr**: **Can either be **auto**,**manual** or **192.168.1.50(any valid ip string)**
    - auto : automatically selects first ip it finds
    - manual : REQUIRES USER INPUT, so won't work if the app runs as a daemon, but will run if u run
    it accordign to instructions. Allows the user to choose any ip addr currently assignet to pc.
    - ipstring: Will try to parse the ip string and check against currently assigned addresses to pick
    the one that matches. Won't actually reassign any ipv4 address to the machine.

## Docs
- **Developer documentation** 
    - If Gradle is installed on the system (check with `gradle -v`)
    the following command can be executed from the command line: 
        - **Windows**: `./gradlew build`
        - **Unix**: `./gradlew build`
    After that u can find generated Javadoc in *build/docs/javadoc/index.html*

    - Or just read the src code and documentation directly if u dare :]
- **User documentation**
    - This can be considered as user documentation and should be read thoroughly.

## Architecture and design patterns
- The entire application is developed in Java v20.
- ## Web Application
    - App uses Spring boot web framework for creating a simple tomcat web server.
    - Spring boot by default uses a MVC architecture, so that is also used in this application.
- ## TCP/UDP
    - Both TCP and UDP communication is done with builtin Java libraries, in a P2P style with all 
information being contained in RAM.
    
## App runtime behaviour
- The application first initializes and sets everything up.
- Then it will periodically broadcast UDP packets to an IPv4 broadcast address.
- If it receives a valid response or detects the same broadcast from someone else, a 2 way TCP
connection is attempted.
- Information about UDP and TCP behavior is shown in console during runtime, and the webserver
contains information about message history and established connections (refresh page to refresh info)

## Dependencies
- Google.com GSON Json parsing and serialization tool. See [GSON](https://github.com/google/gson) 
- Spring boot web framework

## Shortcomings,failures and things to be improved
- Application is quite heavy on the processor, so it will have trouble running on weaker systems.
- Didn't get to make a .jar executable file for simple startup, due to mysterious issues with 
- creating a fat jar with spring boot plugins.
- Logging is very minimal and barely does anything, could be completely overhauled, didn't care enough.
- Due to time limitations the Website couldn't be made as pretty or as smooth as I wanted. So the UX
is not great, but it works.

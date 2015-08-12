# SmartHome-Server
Development project at UTBM

Full project and documentation [HERE !](https://github.com/alexgus/SmartHome "Doc")

## Introduction

This project intends to connect your calendars with your morning's alarm. Calendars can be :
 * ical stream
 * google calendar (not supported yet, or via ical stream)
 * caldav (not supported yet)

## Alarm
You can use Raspberry Pi for this server and [alarm project](https://github.com/alexgus/SmartHome-Clock) but it can also be installed in any UNIX/LINUX environment.

## Configuration
Fully configurable with [smart.conf](https://github.com/alexgus/SmartHome-Server/blob/master/smart.conf "Smart.conf file") file

## Developer information
### Compilation
Just type `mvn install` at the root of this project. The executale jar file will be created in `target` folder with all its dependencies (required to run it corrrectly).

### library used
 * cron4j
 * ical4j
 * ical4j-connector
 * google-api-services-calendar
 * google-api-services-gmail
 * org.eclipse.paho.client.mqttv3
 
see pom.xml for full list

### Server
Receiving command MQTT and simple TCP connection :
 * `AddRing [AAAA-MM-DDHhhMmm]` : command for adding event with
     * `AAAA` : the year
     * `MM` : the month
     * `DD` : the day of the month
     * `hh` : the hour
     * `mm` : the minute
 * `Ring` command for trigger the alarm

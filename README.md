# Home alarm

![home-alarm-card](http://brea.synology.me/img/home-alarm-card.png)

Home alarm is a final degree project that develops a complete wired alarm system for a home or shop.

The project focuses on software development. However, there is a few information about electrical and electronic components.

Can't find what you're looking for? Do not panic, I'm still working on it.  ;)

![no_panic](http://brea.synology.me/img/no_panic.png)

## Centralita (alarm switchboard)

In this folder is located the source code for an `Arduino MEGA 2560` board.

## ServidorAlarma (alarm server)

In this folder is located the source code for a server who must to be installed on a GNU/Linux 24/7 machine.

For the prototype I have used a `Raspberry Pi 3` board and it worked great. There is something that worries me and should be valued: I mean that the Raspberry Pi works with a micro SD card and there is a limitation of amount of read and write cycles, therefore, an alternative such as using a RamDisk should be sought.

The directory structure used by the server software is as follows:

```
/
└── usr
    └── local
        └── bin
            └── servidoralarma
                ├── config.json
                ├── ServidorAlarma.jar
                ├── cam
                │   ├── cam_1
                │   ├── cam_2
                │   ├── ...
                │   └── cam_n
                ├── lib
                │   ├── jSerialComm-2.9.1.jar
                │   └── mysql-connector-java-8.0.28.jar
                └── sql
                    ├── insert.sql
                    └── script.sql
```

`servidoralarma` and `ServidorAlarma.jar` names can be modified by another language (they are in Spanish) but if they are modified, these changes must be transferred to the service script. Don't mind, it's quite easy. Take a look at the project document.

`config.json` is a plain text file with JSON format that contains the configuration payload for the server. This file should be configurated once per installation. This file is quite important and it must to be placed with the server `.jar` file. Its content is as follows:

```json
{
"server":{
	"rmi_port":28803,
	"serial_port":"/dev/ttyACM0",
	"prearm_code":"159"
	},
"firebase":{
	"firebase_token":"[firebase_token]",
	"url_fcm":"https://fcm.googleapis.com/fcm/send",
	"android_channel":"alarma_vivienda",
	"uri_image":"[uri_to_image]",
	"time_to_live":2419200
	},
"sql":{
	"url":"jdbc:mysql://localhost:3306/",
	"user_name":"[user_name]",
	"user_password":"[user_password]",
	"script_create":"sql/script.sql",
	"script_insert":"sql/insert.sql"
	}
}
```
`config.json` file contains critical information so it must to be owned by root user with 600 permissions' mask.

## clienteAlarma (alarm client app)

In this folder is located the source code for an Andoid mobile smartphone.

Be sure to customize the RMI service port in the `integers.xml` file and the RMI server URL in the `network_security_config.xml` and `string.xml` files (and all the languages string files you want to use, for example `string-es-rES.xml` for spanish).

You may also need to download the `google-services.json` file from Firebase. (see https://firebase.google.com/docs/android/setup)

## Hardware

In this section I am proposing a BOM (bill of materials) with the intention of generating a final product based on the prototype. For example, the alarm client and SQL database can be hosted on any hardware on the home network, and I use a Raspberry Pi for this. You can use a NAS, for example, if you already have one.

Alarm switchboard
- [ ] Arduino MEGA 2560 Rev 3
- [ ] Proto-board
- [ ] PNP transistor (2n2222 or similar)
- [ ] 1KOhm resistor

Server
- [ ] Raspberry Pi 3 B+
- [ ] SD card (example: 128GB Class10 U3 V30)

Others
- [ ] Wiegand entry controller with numeric keyboard and RFID reader (example: KR602-M)
- [ ] Movement sensors (example: Pyronix KX10DTP)
- [ ] Opening door and window sensors (example: MC-38)
- [ ] IP camera(s) ONVIF and/or RTSP compatible
- [ ] Siren (example: AS210N)
- [ ] USB cable (USB-A to USB-B)
- [ ] 6 wires cable (0,18 mm<sup>2</sup>)
- [ ] Power supply 12VDC with UPS function (example: DRC-60A)
- [ ] 12V battery (example: 12V 4.2Ah)
- [ ] Safety box (example: PAR-116)

Pay attention to the dimensioning of the power supply, the current consumption of the motion sensors, the siren when it is sounding and the battery capacity.

## Author

The author of this project is Fidel Brea (https://github.com/fidelbrea)

To contact personally email to fidelbreamontilla@gmail.com

## License

This software is created under conditions of GNU General Public License version 3.

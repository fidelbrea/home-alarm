# Home alarm

Home alarm is a final degree project that develops a complete wired alarm system for a home or shop.

The project focuses on software development. However, there is a few information about electrical and electronic components.

Can't find what you're looking for? Do not panic, I'm still working on it.  ;)

![61xuG2P8cLL _AC_SX450_](https://user-images.githubusercontent.com/55228730/164787151-0edceca5-9fa5-4367-a6a1-032ab7e3010f.jpg)

## Centralita (alarm switchboard)

In this folder is located the source code for an `Arduino MEGA 2560` board.

## ServidorAlarma (alarm server)

In this folder is located the source code for a server who must to be installed on a GNU/Linux 24/7 machine.

For the prototype I have used a `Raspberry Pi 3` board and it worked great. There is something that worries me and should be valued: I mean that the Raspberry Pi works with a micro SD card and there is a limitation of amount of read and write cycles, therefore, an alternative such as using a RamDisk should be sought.

The directory structure used by the server software is as follows:

```
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

`servidoralarma` and `ServidorAlarma.jar` names can be modified by another language (they are in Spanish) but if they are modified, these changes must be transferred to the service script. Don't mind, it's quite easy.

`config.json` is a plain text file with JSON format that contains the configuration payload for the server. This file should be configurated once per installation. Its content is as follows:

```json
{
"server":{
	"rmi_port":28803,
	"serial_port":"/dev/ttyACM0"
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

## App

In this folder is located the source code for an Andoid mobile smartphone.

## Author

The author of this project is Fidel Brea (https://github.com/fidelbrea)

To contact personally email to fidelbreamontilla@gmail.com

## License

This software is created under conditions of GNU General Public License version 3.

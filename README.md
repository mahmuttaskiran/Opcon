<p align="center">
   <img style="display:block;margin-left: auto; margin-right: auto;" src="https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/icon/outputs/mipmap/mipmapldpi.png?style=centerme">
 </p>

Chatting|Profile
:-:|:-:
![](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_chat0.jpg)  |  ![](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_profile0.jpg)

## Important note
Google has updated its Google Play Developer Policy which restricting SMS, CALL_LOG access only to default apps. So, Opcon needs read sms logs and call logs to execute **InCallCondition, OutCallCondition, InSmsCondition, OutSmsCondition**. After Google has updated its policy, Opcon has been removed from Play Store. There are two option to re-implement Opcon to successfull execute above conditions:
1) We should be filling 6-page [Permission Decleration Form](https://docs.google.com/forms/d/e/1FAIpQLSfCnRaa4b1VuHhE4gVekWJc_V0Zt4XiTlsKsTipTlPg5ECA7Q/viewform) and submitting to Google Play for review.
2) We should implement default Sms Application features.

Personally, I don't have time to do these things. If anyone has, feel free to contribute, it will be good to see Opcon on the Play Store again.

## Download APK
If you wanna examine Opcon, you can [download the .apk](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/opcon-release.apk) and install. But don't forget, some functionality needs SMS and CALL_LOG permission, will not work as expected.

## What is Opcon
It is a messaging application. And it is more than a messaging application. Opcon offers to user some new features like Notifier and Profile Updater.  Let's think about an alarm application. An alarm application wants to you to answer this question: "When and how do i wake you up?". Opcon asks "Whom shall I inform about the events whose mobile phone notifies?" That's all!

## Features
* Text messaging<br>
* Image Messaging<br>
* Special packet messaging. Includes: Battery level, last incoming call, last outgoing call, last outgoing message, last incoming message.<br>
* Locational conditions<br>
* In and Out call conditions<br>
* In and Out sms conditions<br>
* User profile
* User profile updater

## Notifier
![Image](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_add_rule_revert0.jpg)
In Opcon, an notifier is a advanced alarm mechanism. A notifier consist of two component: Condition and Operation. A condition is mobile event (in/out call, in/out sms messages, camera events, alarm, battery degree events, location listener) trigger. An operation is what user want to make when on specific condition.

Supported Conditions|Supported Operations
--- | --- |
`Alarm`<br>E.g: At 12:46PM or 01/01/2020 12:00 | Send a message to me<br>A message can contain a special packet.
`Locational`<br>E.g: When he/she near of a location you picked.| Send a message to her/his<br>A message can contain a special packet.
`IncomingCall`<br>E.g: When he/she receives a call from a phone number|Notify me with a notification sound.
`OutgoingCall`<br>E.g: When he/she calls a phone number| Notify him with a notification sound.
`IncomingMessage`<br>E.g: When he/seh receives a message from a phone number|Shara something on my profile. It is can be a special packet.
`OutgoingMessage`<br>E.g: When she/he send a message to a phone number|
`BatteryLevel`<br>E.g: When her/his phone battery level is lower than X|
`Camera`<br>When she/he capture a photo|

Any condition can work with any operation, and any operation can contain any special packet. And... When a user wanna sent a notifier to the oppos*ite side, he/she can select a any condition on any side. **Awesome right!?**

Special Packets| Description
---|--|
`BatteryLevel`|Sends battery level of user|
`LastIncomingCall`|Sends last incoming call of user. Reads from call-logs.
`LastOutgoingCall`|Sends last outgoing call of user. Reads from call-logs.
`LastReceivedMessage`|Sends last incoming message of user.
`LastSendMessage`|Sends last message user sent to.
`Location`|Sends Last known location of user
`LastCapturedImage`|Sends last captured image

## What is Profile Updater?
What do you want to share with your friends when? An profile updater consist of what you answer this question. If you want, you can show to your friends what profile updaters that makes posts.

## When does a notifier does effect?
An notifier that you send to your friends it is deactivated as default. It does not any effect until your friend active it. Also! If the notifier contains special packet, that special packets will send after approval your friends (Even if it is active).

License
-------

    Copyright 2015 Mahmut Taşkıran

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


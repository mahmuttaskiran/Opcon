<p align="center">
   <img src="https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/icon/outputs/mipmap/mipmapldpi.png" style="display:block; margin-left: auto; margin-right: auto;">
</p>

Chatting|Profile
:-:|:-:
![](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_chat0.jpg)  |  ![](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_profile0.jpg)

## Important Note
Google has updated its Google Play Developer Policy to restrict SMS and CALL_LOG access to only default apps. Therefore, Opcon requires reading SMS logs and call logs to execute **InCallCondition, OutCallCondition, InSmsCondition, OutSmsCondition**. Following Google's policy update, Opcon was removed from the Play Store. To reintroduce Opcon successfully and execute the above conditions, there are two options:
1) Complete a 6-page [Permission Declaration Form](https://docs.google.com/forms/d/e/1FAIpQLSfCnRaa4b1VuHhE4gVekWJc_V0Zt4XiTlsKsTipTlPg5ECA7Q/viewform) and submit it to Google Play for review.
2) Implement features for the default SMS Application.

Personally, I don't have the time to undertake these tasks. If anyone is willing to contribute, it would be great to see Opcon available on the Play Store again.

## Download APK
If you want to explore Opcon, you can [download the .apk](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/opcon-release.apk) and install it. But keep in mind that some functionalities requiring SMS and CALL_LOG permissions will not work as expected.

## What is Opcon?
Opcon is more than a messaging application; it introduces users to new features like Notifier and Profile Updater. Think of it like an alarm app that asks, "When and how should I wake you up?" Opcon asks, "Who should I inform about the events your mobile phone notifies you of?" That's the essence!

## History of Opcon
The Opcon was the first mobile project that I developed with Java for Android. The idea behind the Opcon comes from a project that I developed for Windows in the earlier years of my programming journey (I was 15). At that age, I was using a scripting language, AutoScript, and Microsoft's VisualBasic. With AutoScript, I developed a Windows program in which users can control their devices by setting conditions and operations, for example:
- If the program X, do kill the process [X|Y].
- If the user's screen time exceeds the duration X, then show an alert.
- If the user runs the program X for Y duration, then lock the screen.
- If I receive the data X from the Y port from the Z IP, then send back the data X. (This was very complex to set from the end user perspective because they are supposed to have different devices that run my program, have a static IP address to send data to other's device that also has the setup for my program)

There were too many conditions and operations and the user was able to create a rule in which he/she could set any condition(s) with an operation(s).

Anyway, in 2015, I wanted to learn Java and Android SDK together, and I knew the only way to learn a new programming language/paradigm was to create a new project. Before 2015, I had used AutoScript, VB, PHP, JS, and Python languages and wrote many applications with those languages. But I never experienced the OOP with those languages even though some of them are OOP languages, because I wasn't aware of OOP. I'm a self-taught programmer, I was programming to have fun, and none of my projects were a big deal. But, at the age of 22, I wanted to build something for the users, I wanted to prove my skills to everyone, I wanted to change the world, LOL.

Well, considering all these, I wanted to learn Java & And SDK, I had to have a good idea to deliver to users -to change to world, and have decided to create the same project as I did for Windows to let users have full control of their devices. But this project was an automation tool rather than an application in which users can have fun. I needed to redesign the idea of my first Android application, Opcon. Redesigning Opcon's idea was a very long process, I re-wrote the Opcon many times, maybe more than 10 times, and each fresh start came with a lot of knowledge of Java, OOP,  algorithms, data structures, and Android SDK and with a re(designed|considired) idea of the Opcon.

After 1.5 years of struggling, I was finally able to release the first stable release (stable to me, LOL, I was the only tester/designer/coder & the user of this app). I had a stressful year for this application, I had to isolate myself from life and dedicate myself to learning, and developing. Anyway, it had come to reach out to the users. But, I couldn't do this, because I was alone, I mean I wasn't in such an environment in which I could show my doings to the people who could help me, I wasn't in the software environment at all. 

After one more year of contributing the Opcon, I finally quit the application for the reason I have mentioned above, made it open source and it died. I hope the same won't happen for my dear [Polyingo](https://www.polyingo.com)

From all this, I learned that "less is more". If I could go back to 2015, I would break up Opcon into smaller, simpler apps, each one doing just one thing.

## Features
* Text messaging
* Image messaging
* Special packet messaging, including Battery level, last incoming/outgoing call, and last received/sent message.
* Location-based conditions
* Incoming and outgoing call conditions
* Incoming and outgoing SMS conditions
* User profile and profile updater

## Notifier
![Image](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_add_rule_revert0.jpg)
In Opcon, a notifier is an advanced alarm mechanism that consists of two components: Condition and Operation. A condition is a mobile event trigger (like incoming/outgoing calls, SMS messages, camera events, alarms, battery level, and location changes). An operation is what the user wants to do when a specific condition occurs.

# Profile updater
In Opcon, the users are able to set conditions to automatically update their profile statuses or to post on their profiles. For example, **when my phone battery is about to die** (less than %X) **then post**, 
"Guys, I'm about to join my phone in 'low power mode.' If I don't respond, send a charger!" 

Supported Conditions|Supported Operations
--- | --- |
`Alarm`<br>For example: At 12:46 PM or on 01/01/2020 at 12:00 PM | Send me a message<br>A message can include a special packet.
`Location`<br>For example: When near a chosen location| Send them a message<br>A message can include a special packet.
`IncomingCall`<br>For example: When receiving a call from a specific number| Notify me with a sound alert.
`OutgoingCall`<br>For example: When making a call to a specific number| Notify them with a sound alert.
`IncomingMessage`<br>For example: When receiving a message from a specific number| Share something on my profile, possibly a special packet.
`OutgoingMessage`<br>For example: When sending a message to a specific number|
`BatteryLevel`<br>For example: When the phone battery is below a certain percentage|
`Camera`<br>For example: When capturing a photo|

Any condition can work with any operation, and any operation can include any special packet. Plus, when a user wants to send a notifier to someone, they can choose any condition on any side. **Awesome, right!?**

Special Packets| Description
---|--|
`BatteryLevel`|Sends the user's battery level|
`LastIncomingCall`|Sends details of the user's last incoming call, read from call logs.
`LastOutgoingCall`|Sends details of the user's last outgoing call, read from call logs.
`LastReceivedMessage`|Sends the user's last received message.
`LastSentMessage`|Sends the last message the user sent.
`Location`|Sends the user's last known location.
`LastCaptured

# YouTube videos
For advertisement: https://www.youtube.com/watch?v=j-WoKTDGt0E&ab_channel=MahmutTa%C5%9Fkiran

Live demo: https://youtu.be/RN-wuW9stK4?si=s86MEyBnBz4eqcqB

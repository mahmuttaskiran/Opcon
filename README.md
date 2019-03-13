## Important note
Google has updated its Google Play Developer Policy which restricting SMS, CALL_LOG access only to default apps. So, Opcon needs read sms logs and call logs to execute **InCallCondition, OutCallCondition, InSmsCondition, OutSmsCondition**. After Google has updated its policy, Opcon has been removed from Play Store. There are two option to re-implement Opcon to successfull execute above conditions:
1) We should be filling 6-page [Permission Decleration Form](https://docs.google.com/forms/d/e/1FAIpQLSfCnRaa4b1VuHhE4gVekWJc_V0Zt4XiTlsKsTipTlPg5ECA7Q/viewform) and submitting to Google Play for review.
2) We should implement default Sms Application features.

Personally, I don't have time to do these things. If anyone has, feel free to contribute, it will be good to see Opcon on the Play Store again.

## Download APK
If you wanna examine Opcon, you can [download the .apk]() and install.

## Opcon
It is a messaging application. And it is more than a messaging application. Opcon offers to user some new features like Notifier and Profile Updater.  Let's think about an alarm application. An alarm application wants to you to answer this question: "When and how do i wake you up?". Opcon asks "Whom shall I inform about the events whose mobile phone notifies?" That's all!

## Notifier
![Image](https://github.com/mahmuttaskiran/Opcon/raw/master/store_presence/play-presence/outputs/en/en_add_rule_revert0.jpg)
In Opcon, an notifier is a advanced alarm mechanism. A notifier consist of two component: Condition and Operation. A condition is mobile event (in/out call, in/out sms messages, camera events, alarm, battery degree events, location listener) trigger. An operation is what user want to make when on specific condition.

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


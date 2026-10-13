@name("Message Sound")
@description("Plays a sound for new WhatsApp messages, but only on weekdays")
@category("Notifications")

variables {
    bool ok;
    int count = 0;
    dict lastSender = {"name": "", "count": 0};
}

triggers {
    notification(packageNameList: [app("com.whatsapp", "WhatsApp")]) as message;
}

constraints {
    dayOfWeek(daysOfWeek: WEEKDAYS);
}

actions {
    count++;
    lastSender["name"] = "${sys.not_title}";
    callBlock("Play Sound Effect",
              inputs: {soundFile: "message.mp3"},
              outputs: {played: ok});
    if (!ok) {
        log("Could not play the sound for message ${count}");
    }
}

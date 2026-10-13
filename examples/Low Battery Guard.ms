@name("Low Battery Guard")
@description("Warns when the battery is low. Turns off WiFi after three warnings.")
@category("Power")
variables {
    int warnings = 0;
    string lastWarning;
}
triggers {
    batteryLevel(20, decreasesTo: true) as low;
    externalPower(powerConnected: true) as plugged;
}
constraints {
    airplaneMode(enabled: false);
}
actions {
    if (triggeredBy(plugged)) {
        warnings = 0;
        return;
    }
    warnings++;
    lastWarning = "Warning ${warnings} at ${sys.datetime}";
    /** The text shows the battery level and the last warning. */
    notify(title: "Battery low", text: "${sys.battery}% left. ${lastWarning}");
    if (warnings >= 3 && externalPower(externalPower: false)) {
        setWifi(TURN_OFF);
    }
    repeat (3) {
        vibrate(SHORT_BUZZ);
        wait(500ms);
    }
}

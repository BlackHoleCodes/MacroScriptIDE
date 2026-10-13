@name("WiFi & Bluetooth Scheduler")
@description("Turns WiFi and Bluetooth on at 7am and off at 11pm, then vibrates")
triggers {
    timer(hour: 7, minute: 0, daysOfWeek: EVERY_DAY) as morning;
    timer(hour: 23, minute: 0, daysOfWeek: EVERY_DAY) as night;
}
actions {
    if (triggeredBy(morning)) {
        setWifi(TURN_ON);
        setBluetooth(TURN_ON);
    } else {
        setWifi(TURN_OFF);
        setBluetooth(TURN_OFF);
    }
    vibrate(SHORT_BUZZ);
}

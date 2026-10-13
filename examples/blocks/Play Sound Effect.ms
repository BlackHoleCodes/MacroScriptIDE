@actionBlock
@name("Play Sound Effect")
@description("Plays one sound file from a folder")

variables {
    in string soundFile = "beep.mp3";
    out bool played;
}

actions {
    playSound(staticFilename: false, useAllFilesAccess: true,
              allFilesPath: "/sdcard/sounds", allFilesFilename: "${soundFile}");
    played = true;
}

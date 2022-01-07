# Mobile Table Tennis Tracking System (M3TS) adapted for usage with HoloLens

## Goal
This project's goal is to develop a system which automatically keeps track of a ping pong match (like a "virtual referee").

Additionally the trajectories and score infos are send to a HoloLens and visualized there.

![M3TS Preview](./readme_content/m3ts_preview.gif)

The system runs on two android devices and a HoloLens:

- One smartphone is the "tracker" -> it films the ping pong table and does the tracking
- The other smartphone is the "display" -> it displays the trackers state (match status) to the players
- The HoloLens showes the trajectory and the scores to one of the players.

## Demo
Checkout this demo on youtube for a first impression of the system (w/o HoloLens) (turn on subtitles for ENG captions):
https://www.youtube.com/watch?v=QaV0DVbXExA

## Get it running
To get the app running, simply clone this repo and open it in Android Studio.
After Android Studio installed all dependencies via Gradle, you should be good to go for using the app.
Or just use the uploaded .apk file

## Architecture
Brief description of the architecture to assist further development.
### Object Detection / Tracking
Is done using the [FMO (Fast Moving Object) Android implementation](https://github.com/hrabalik/fmo-android). Basically a JNI which gets called each time the library finds detections.

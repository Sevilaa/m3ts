# Mobile Table Tennis Tracking System (M3TS) adapted for usage with HoloLens
ETH MR Lab course project AS2021: Augmented Table Tennis Game  
This repository contains the adapted Android Project associated with the project. 

Authors: Tianxu An, Pascal Chang, Matthias Koenig, Severin Laasch

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
The Android .apk file can be found [here](app/release/app-release.apk).
The HoloLens application is available [here](https://github.com/pchangmaths0327/MRTableTennis-HoloLens).

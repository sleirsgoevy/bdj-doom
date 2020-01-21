# Blu-Play DOOM I (1993) port

This repository contains a version of [DOOM videogame](https://en.wikipedia.org/wiki/Doom_(1993_video_game%29), transpiled into Blu-Ray Java using [Cibyl MIPS-to-Java compiler](https://github.com/SimonKagstrom/cibyl/). This version is based on the [official DOOM sources](https://github.com/id-Software/DOOM), not on another clone.

Note: Cibyl only runs on Unix-like systems. Windows users will have to use Cygwin to compile this.
Note #2: This project is based on [this example project](https://github.com/sleirsgoevy/bdj-cibyl). See there for more information.

## cibyl/

This subdirectiry contains the DOOM's C source code, along with some glue code, to be compiled into Java.

To build, type `make` inside the `cibyl/` directory. The Makefile expects the [Cibyl binary toolchain](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/cibyl/cibyl-bin-linux32-21.tar.gz) to be located at `../cibyl/`, relatively to the repository root.

## java/

This subdirectory contains the actual Java Xlet that starts the DOOM and forwards access to the screen. It is to be compiled using [PS3 BD-J SDK](https://mega.nz/#F!A4IFGYga!B6KAPlNBPBzGEN6j5OaDNQ). Run `./copy-cibyl.sh` before building to copy Cibyl-generated Java classes into the jar.

## native/

This directory contains mock headers that allow native compilation of the Cibyl-adapted DOOM code. It features a minimal "playable" GUI and is mostly useful for debugging the Cibyl version.

To build the native executable, type `make`. It will be compiled from exactly the same sources as the Cibyl version.

## Current status

Basically the game is playable. ~~However, elevators don't work properly, and the door at the end of E1M2 doesn't open.~~ FIXED

Currently only the video and input subsystems have been ported. There is no support for network play, sound, ~~or game saves~~.

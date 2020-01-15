# BD-J Cibyl demo

This repository contains a demo of using [Cibyl MIPS-to-Java compiler](https://github.com/SimonKagstrom/cibyl/) to run C code on BD-J by transpiling it into Java. The Cibyl transpiler may aid in porting C games to [Blu-Play](https://blu-play.com), however the transpiled code is a bit slower that the same code written directly in Java.

Note: Cibyl only runs on Unix-like systems. Windows users will have to use Cygwin.

## cibyl/

This subdirectiry contains the C code to be compiled into Java. The `cibyl/syscalls/` directory contains the declarations of static Java methods that the generated C code will be able to call.

To build the C part of the program, type `make` inside the `cibyl/` directory. The Makefile expects the [Cibyl binary toolchain](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/cibyl/cibyl-bin-linux32-21.tar.gz) to be located at `../cibyl/`, relatively to the repository root.

## java/

This subdirectory contains the actual Java Xlet that starts the C program and forwards access to the screen. It is to be compiled using [PS3 BD-J SDK](https://mega.nz/#F!A4IFGYga!B6KAPlNBPBzGEN6j5OaDNQ). Run `./copy-cibyl.sh` before building to copy Cibyl-generated Java classes into the jar.

all: build/classes

include/cibyl-syscalls.db: syscalls/bluplay/* syscalls/bluplay/include/org/homebrew.h
	python fake_bin/struct_pack_wrapper.py ../../cibyl/mips-cibyl-elf/bin/cibyl-generate-c-header syscalls bluplay -o include

build:
	mkdir build

build/main: build include/cibyl-syscalls.db src/*.c
	../../cibyl/mips-cibyl-elf/bin/mips-cibyl-elf-gcc --std=gnu99 -DNORMALUNIX -I include src/*.c src/doom/*.c -O1 -lm -o build/main

build/classes: build build/main include/cibyl-syscalls.db
	rm -rf build/classes build/classes0
	mkdir build/classes0
	PATH=$(PWD)/fake_bin:$(PATH) ../../cibyl/mips-cibyl-elf/bin/cibyl-mips2java --package-name org.homebrew -I include build/main -DNOJ2ME -d build/classes0
	rm build/classes0/CibylConfig.java # will be substituted later
	rm build/classes0/org/homebrew/CibylCallTable.class # need to be recompiled with -source 1.3
	{ echo 'package org.homebrew;'; cat build/classes0/CRunTime.java; } > build/classes0/org/homebrew/CRunTime.java
	rm build/classes0/CRunTime.java
	mv build/classes0 build/classes

clean:
	rm -rf build include/org include/cibyl-syscalls.db

all: build/classes

include/cibyl-syscalls.db: syscalls/bluplay/* syscalls/bluplay/include/org/homebrew.h
	python fake_bin/struct_pack_wrapper.py ../../cibyl/mips-cibyl-elf/bin/cibyl-generate-c-header syscalls bluplay -o include

build/main: include/cibyl-syscalls.db src/*.c src/doom/*.c src/doom/*.h include/*.h
	mkdir -p build
	../../cibyl/mips-cibyl-elf/bin/mips-cibyl-elf-gcc --std=gnu99 -DNORMALUNIX -D__BIG_ENDIAN__ -I include src/*.c src/doom/*.c -O0 -lm -lcrt0 -lj2se -o build/main

build/classes: build/main include/cibyl-syscalls.db
	rm -rf build/classes build/classes0
	mkdir -p build/classes0
	PATH=$(PWD)/fake_bin:$(PATH) ../../cibyl/mips-cibyl-elf/bin/cibyl-mips2java --only-translate --package-name org.homebrew -I include build/main -DNOJ2ME -d build/classes0
	rm build/classes0/CibylConfig.java # will be substituted later
	{ echo 'package org.homebrew;'; cat build/classes0/CRunTime.java; } > build/classes0/org/homebrew/CRunTime.java
	rm build/classes0/CRunTime.java
	sed -i 's/System\.out/MyXlet.getStdout()/g' build/classes0/org/homebrew/Syscalls.java
	sed -i 's/System\.err/MyXlet.getStdout()/g' build/classes0/org/homebrew/Syscalls.java
	#sed -i 's/System\.out/MyXlet.getNetStdout()/g' build/classes0/org/homebrew/Syscalls.java
	#sed -i 's/System\.err/MyXlet.getNetStdout()/g' build/classes0/org/homebrew/Syscalls.java
	bash rewrite_jasmin.sh
	mv build/classes0 build/classes

clean:
	rm -rf build include/org include/cibyl-syscalls.db

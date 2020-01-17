doom: src/*.c src/*.h include/*.h main.c
	gcc -m32 -I include/ -D NORMALUNIX src/*.c main.c -o doom -lm -lSDL2 -g

clean:
	rm doom

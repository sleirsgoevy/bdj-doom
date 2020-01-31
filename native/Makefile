doom: src/*.c src/*.h include/*.h main.c sockets.c
	gcc -m32 -I include/ -D NORMALUNIX src/*.c main.c sockets.c -o doom -lm -lSDL2 -g

doom-asan: src/*.c src/*.h include/*.h main.c sockets.c
	gcc -fsanitize=undefined -fsanitize=undefined -m32 -I include/ -D NORMALUNIX src/*.c main.c sockets.c -o doom-asan -lm -lSDL2 -g

clean:
	rm -f doom doom-asan

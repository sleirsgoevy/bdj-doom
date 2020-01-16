exec 2>&1

for i in src/doom/*.c
do
    if ! ../../cibyl/mips-cibyl-elf/bin/mips-cibyl-elf-gcc -D NORMALUNIX=1 --std=gnu99 -I include "$i" -O1 -lm -c -o /dev/shm/tmp
    then
        echo $i failed
        exit 1
    fi
    rm /dev/shm/tmp
done

cd build/classes0
for i in org/homebrew/Cibyl*.j
do
    sed -i 's/invokestatic CibylCallTable\/call(IIIIII)I/invokestatic org\/homebrew\/CibylCallTable\/call(IIIIII)I/g' "$i"
done
../../../../cibyl/mips-cibyl-elf/bin/cibyl-jasmin org/homebrew/Cibyl*.j

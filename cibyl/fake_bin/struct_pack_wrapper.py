import sys, runpy, struct, os.path

del sys.argv[0]
sys.path[0] = os.path.split(sys.argv[0])[0]

old_struct_pack = struct.pack
def new_struct_pack(fmt, *args):
    return old_struct_pack(fmt.replace('P', 'I'), *args)
struct.pack = new_struct_pack

old_struct_calcsize = struct.calcsize
def new_struct_calcsize(fmt):
    return old_struct_calcsize(fmt.replace('P', 'I'))
struct.calcsize = new_struct_calcsize

runpy.run_path(sys.argv[0])

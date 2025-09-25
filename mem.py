#!/usr/bin/env python3
from bcc import BPF

def print_event(cpu, data, size):
    # 定义一个回调函数
    event = b["events"].event(data)
    name = ""
    if event.type == 1:
        name = "file refault"
    elif event.type == 2:
        name = "anongmous refault"
    print(f"pid is {event.pid} ,name of pid is {event.pid_name}, type is {name}, vaddr is {event.vaddr}")


if __name__ == '__main__':
# bPF() 默认会去寻找与内核版本匹配的文件名，确保跨版本的兼容性
    b = BPF(src_file="mem.c")

    # 监测文件页
    try:
        # b.get_syscall_fnname 是用来查找系统调用的，在内核的入口函数，
#        syscall_name = b.get_syscall_fnname("do_read_fault")
#        b.attach_kprobe(event=syscall_name, fn_name="trace_execve_file")
        # 对于内核内部函数，直接指定内核函数名字就可以了
        b.attach_kprobe(event="do_fault", fn_name="trace_execve_file")
    except Exception:
        b.attach_kprobe(event="trace_file_fault", fn_name="trace_execve_file")

    # 监测匿名页
    b.attach_kprobe(event="do_anonymous_page", fn_name="trace_execve_file")

    ## 然后去跟踪在test2.c中定义的函数
    print("start trace !!")
    
    ## 打开perf——buffer，并设置回调函数, 这个是在test2.c中定义的 “events.perf_submit(stx, &data, sizeof(data));”
    b["events"].open_perf_buffer(print_event)

    while True:
        try:
            b.perf_buffer_poll()
        except KeyboardInterrupt:
            print("\n end trace!!")
            exit()
    print(1)




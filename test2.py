#!/usr/bin/env python3
from bcc import BPF

def print_event(cpu, data, size):
    # 定义一个回调函数
    event = b["events"].event(data)
    print(f"pid is {event.pid} ,filename is {event.filename}")


if __name__ == '__main__':
# bPF() 默认会去寻找与内核版本匹配的文件名，确保跨版本的兼容性
    b = BPF(src_file="test2.c")

    try:
        syscall_name = b.get_syscall_fnname("execve")
    except Exception:
        print("Warn: could not find `execve` syscall")
        syscall_name = b.get_syscall_fname("execveat")

    ## 然后去跟踪在test2.c中定义的函数
    b.attach_kprobe(event=syscall_name, fn_name="trace_execve")
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




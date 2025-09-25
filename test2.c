// 任务：实时跟踪open系统调用并输出：进程ID,进程名，打开的文件名
//, 其中的open系统调用指的是；文件的fopen操作会去触发这个操作

#include <uapi/linux/ptrace.h>
#include <linux/kernel.h>
#include <linux/sched.h>
//#include <stdio.h>

// 然后定义一个特殊的map,  用于将数据从内核态发送到用户态
BPF_PERF_OUTPUT(events);

// 定义一个结构体，用于在用户态和内核态之间传递数据
struct data_t {
	u32 pid;
	char pid_name[TASK_COMM_LEN]; //TASK_COMM_LEN通常是16个字节
	char filename[16]; 
};

// 探针函数，在execve系统调到入口处被触发
int trace_execve(struct pt_regs *ctx) {
	struct data_t data = {};
	// 然后到内核代码中对应的位置去找到bpf对应的helpers.c的说明， /kernel/bpf/helpers.c
 	data.pid = bpf_get_current_pid_tgid() >> 32; // 因为高位存储的是PID,低32位存储的是GID 	
	//data.pid_name = bpf_get_current_pid_tgid() // 这个返回的是进程在其当前命名空间内的PID和TGID
	bpf_get_current_comm(&data.pid_name, sizeof(data.pid_name));
	
	// 然后还差最后一个函数，获取文件名，因为我们这里是通过open函数进行触发，所以关键就是要去获取到触发的函数的第一个参数
	// 经过查阅，open的函数原型是 int openat(int dirfd, const char *pathname ..), 也就是说，文件名是第二个参数，它位于rsi寄存器中。
	const char*filename_ptr = (const char*)PT_REGS_PARM2(ctx); 
	// 获取第一个参数是：PT_REGS_PARM1, 获取第二个参数是：PT_REGS_PARM1
	
	// 然后要注意的是，这个file_name_ptr是用户态的指针，即 PT_REGS_PARM2在这里是从用户态拿到的数据
	// 因此要做一次转化,从用户态真正把这个数据读过来
	bpf_probe_read_user_str(&data.filename, sizeof(data.filename), (void *)filename_ptr);
	//printk(KERN_INFO "filename is %s",data.filename);

	// 将获取到的数据发送给用户，即转到py文件进行处理	
	events.perf_submit(ctx, &data, sizeof(data));
	return 0;
}



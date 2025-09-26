#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <errno.h>

#define MEMORY_SIZE (3ULL * 1024 * 1024 * 1024)  // 3GB
#define PAGE_SIZE 4096  // 4KB
#define PAGES_COUNT (MEMORY_SIZE / PAGE_SIZE)

int main() {
    printf("开始申请80GB可交换内存...\n");
    
    // 申请80GB内存，使用MAP_ANONYMOUS | MAP_PRIVATE创建私有匿名映射
    // 这样创建的内存是可交换的
    void *memory = mmap(NULL, MEMORY_SIZE, PROT_READ | PROT_WRITE, 
                       MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
    
    if (memory == MAP_FAILED) {
        perror("内存申请失败");
        return 1;
    }
    
    printf("成功申请80GB内存，地址: %p\n", memory);
    printf("总页数: %lu\n", PAGES_COUNT);
    
    // 初始化内存，写入一些数据
    printf("正在初始化内存...\n");
    for (size_t i = 0; i < PAGES_COUNT; i++) {
        char *page = (char *)memory + i * PAGE_SIZE;
        // 在每个页面的开头写入页面编号
        *(size_t *)page = i;
        // 在页面末尾写入页面编号的校验值
        *(size_t *)(page + PAGE_SIZE - sizeof(size_t)) = i;
    }
    printf("内存初始化完成\n");
    
    // 持续访问内存页面，确保页面在内存中
    printf("开始持续访问内存页面...\n");
    size_t page_index = 0;
    
    while (1) {
        // 按4KB间隔访问页面
        char *current_page = (char *)memory + (page_index % PAGES_COUNT) * PAGE_SIZE;
        
        // 读取页面开头的数据
        size_t page_num = *(size_t *)current_page;
        // 读取页面末尾的校验值
        size_t checksum = *(size_t *)(current_page + PAGE_SIZE - sizeof(size_t));
        
        // 验证数据一致性
        if (page_num != checksum) {
            printf("错误：页面 %lu 数据不一致！开头: %lu, 结尾: %lu\n", 
                   page_index % PAGES_COUNT, page_num, checksum);
        }
        
        // 更新页面索引
        page_index++;
        
        // 每访问1000个页面输出一次状态
        if (page_index % 1000 == 0) {
            printf("已访问 %lu 个页面，当前页面: %lu\n", 
                   page_index, page_index % PAGES_COUNT);
        }
    }
    
    // 程序不会到达这里，因为while(1)是无限循环
    return 0;
}

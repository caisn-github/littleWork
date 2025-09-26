#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/kprobes.h>
#include <linux/mm.h>
#include <linux/page-flags.h>

MODULE_LICENSE("GPL");
MODULE_AUTHOR("caisn");

static long file_evict_pages = 0;
static long file_refault_pages = 0;
static long anonymous_evict_pages = 0;
static long anonymous_refault_pages = 0;

// 对于evict的断点打在什么位置
static struct kprobe evict_kprobe = {
	.symbol_name = "__remove_mapping",
};

static struct kprobe refault_kprobe = {
	.symbol_name = "workingset_refault",
};


static int get_page_type(struct page *page) {
	if(PageAnon(page)) return 1; //用1表示是匿名页
	
	//if(PageSwapCache(page)) return 1;

	if(page->mapping && page->mapping->host) return 2;

	if (page->mapping == NULL) return 1;

	return 0;

}

// Kprobe前置函数，在原函数执行之前执行
static int evict_handler_pre(struct kprobe *p, struct pt_regs *regs)
{
	struct page *page = (struct page*)regs->di;
	struct folio *folio;

	if (!page) return 0;

	folio = page_folio(page);
	int type = get_page_type(&folio->page);	
	int pages = folio_nr_pages(folio);
	unsigned int order, nr_pages;

	order = folio_order(folio);
	nr_pages = folio_nr_pages(folio);

	if (type == 1) {
		anonymous_evict_pages  += pages;
		printk(KERN_INFO "ANONYMOUS REFAULT: folio=0x%lx, order=%u, nr_pages=%u, pid=%d,total_refault_pages=%ld\n",
           (unsigned long)folio, folio_order(folio),
           pages, current->pid, anonymous_evict_pages);
	
	}
	else if (type == 2) {
		file_evict_pages  += pages;
		printk(KERN_INFO "FILE REFAULT: folio=0x%lx, order=%u, nr_pages=%u, pid=%d,total_refault_pages=%ld\n",
           (unsigned long)folio, folio_order(folio),
           pages, current->pid, file_evict_pages);
	
	} 

	return 0; //返回0表示继续执行原函数

}


static int refault_handler_pre(struct kprobe *p, struct pt_regs *regs)
{
	struct folio *folio = (struct folio*)regs->di;

	if (!folio) return 0;
	
	int type = get_page_type(&folio->page);	
	int pages = folio_nr_pages(folio);
	
	if (type == 1) {
		anonymous_refault_pages  += pages;
		printk(KERN_INFO "ANONYMOUS REFAULT: folio=0x%lx, order=%u, nr_pages=%u, pid=%d,total_refault_pages=%ld\n",
           (unsigned long)folio, folio_order(folio),
           pages, current->pid, anonymous_refault_pages);
	}
	else if (type == 2) {
		file_refault_pages  += pages;
		printk(KERN_INFO "FILE REFAULT: folio=0x%lx, order=%u, nr_pages=%u, pid=%d,total_refault_pages=%ld\n",
           (unsigned long)folio, folio_order(folio),
           pages, current->pid, file_refault_pages);
	
	} 


    	return 0;
}

// 然后也可以写一些函数在原函数执行完之后调用

static int __init folio_trace_init(void)
{
	int ret;
	file_evict_pages=0;
	file_refault_pages=0;
	anonymous_evict_pages=0;
	anonymous_refault_pages=0;

	printk(KERN_INFO "Installing folio trace kprobes\n");

	// 设置probe处理函数
	evict_kprobe.pre_handler = evict_handler_pre;
	refault_kprobe.pre_handler = refault_handler_pre;

	// 注册probes
	ret = register_kprobe(&evict_kprobe);
	if (ret < 0) {
        	printk(KERN_ERR "Failed to register evict kprobe: %d\n", ret);
      		return ret;
    	}
	
	// 注册probes
	ret = register_kprobe(&refault_kprobe);
	if (ret < 0) {
        	printk(KERN_ERR "Failed to register refault kprobe: %d\n", ret);
      		return ret;
    	}

      	printk(KERN_ERR "Failed to register refault kprobe: %d\n", ret);
	return 0;
}

static void __exit folio_trace_exit(void)
{
	// 这个是退出模块的时候调用的
	unregister_kprobe(&refault_kprobe);
	unregister_kprobe(&evict_kprobe);
      	printk(KERN_ERR "kprobe exit");
}

module_init(folio_trace_init);
module_exit(folio_trace_exit);

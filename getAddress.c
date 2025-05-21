#include <linux/mm.h>
#include <asm/pgtable.h>
#include <linux/sched/mm.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/mm_types.h>
#include <linux/sched.h>
#include <linux/highmem.h>
#include <linux/uaccess.h>
#include <linux/kernel.h>
// 因为follow_page_mask没有提供接口，所以要自己仿照写一个
struct page* lookup_address(struct mm_struct *mm, unsigned long addr)
{
	// 通过自己遍历pgd,pud,pmd,pte从而获取最终的pte
	pgd_t *pgd;
	p4d_t *p4d;
	pud_t *pud;
	pmd_t *pmd;
	pte_t *pte;
    struct page* page=NULL;

	pgd = pgd_offset(mm, addr);
	if (pgd_none(*pgd) || pgd_bad(*pgd))
	    return NULL;

	p4d = p4d_offset(pgd, addr);
	if (p4d_none(*p4d) || p4d_bad(*p4d))
	    return NULL;
	
	pud = pud_offset(p4d, addr);
	if (pud_none(*pud) || pud_bad(*pud))
	    return NULL;
     
	pmd = pmd_offset(pud, addr);
	if (pmd_none(*pmd) || pmd_bad(*pmd))
	    return NULL;
	
    // pte = pte_offset_map(pmd, addr);	
	pte_t *pte_base = (pte_t *)pmd_page_vaddr(*pmd);
	if (!pte_base) {
    	pr_info("Cannot get PTE pointer\n");
		return NULL;
	} 
    pte = &pte_base[(addr >> PAGE_SHIFT) & (PTRS_PER_PTE - 1)];
	// pte = *(pte_tmp + ((addr >> PAGE_SHIFT) & (PTRS_PER_PTE - 1)));

    if (!pte)
	 	return NULL;
	
	if (!pte_present(*pte)) {
		pte_unmap(pte);
		return NULL;
	}
    
	page = pfn_to_page(pte_pfn(*pte));
	pte_unmap(pte);

	return page;
}	

void monitor_user_pages(unsigned long addr)
{
	struct mm_struct *mm = current->mm;

	struct vm_area_struct *vma = find_vma(mm, addr);

	if (vma && addr >= vma->vm_start) {
		struct page *page;
		// unsigned long page_mask = 0;

		page = lookup_address(mm, addr); 
		if (page) {
			// 获取物理页帧号	
			phys_addr_t pfn = page_to_pfn(page);
			pr_info("Got page  at offset mask %llx\n", pfn);
			put_page(page);
			

		} else {
			pr_info("Addr 0x%lx has no pgae mapped.\n", addr);
		}
	}
}


// 声明模块是GPL开源
MODULE_LICENSE("GPL");

MODULE_AUTHOR("CSN");

MODULE_DESCRIPTION("A memmory test MOdule");

MODULE_VERSION("1:1.0");

static int __init memtest_init(void)
{
	unsigned long user_base = current->mm->start_brk;
	pr_info("Start monitoring from 0x%lx\n",user_base);
	monitor_user_pages(user_base);
	
	printk(KERN_INFO "Hello: Hello, Word!\n");
	return 0;
}

static void __exit memtest_exit(void)
{
	printk(KERN_INFO "Hello: Goodbye, crazy world wuwuuwuwuwuuw!\n");
}

module_init(memtest_init);
module_exit(memtest_exit);


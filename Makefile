NAME = getAddress

KDIR := /lib/modules/$(shell uname -r)/build

obj-m := $(NAME).o

# ？源文件的名字？
#$(NAME)-objs := YourSrcFiles).o

## 当前目录
PWD := $(shell pwd)

build:
	$(MAKE) -C $(KDIR) M=$(PWD) modules

clean:
	$(MAKE) -C $(KDIR) M=$(PWD) clean
insmod:
	sudo insmod $(NAME).ko
rmmod:
	sudo remmod $(NAME)


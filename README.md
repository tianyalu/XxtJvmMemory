

# `JVM`共享区深入了解及内存抖动/泄漏排查优化

[TOC]

## 一、`JVM`共享区

首先看一下`JVM`整体结构图：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/jvm_structure.png)  

### 1.1 堆区

堆区分为年轻代和老年代，其空间大小理论比值为2：1；其中年轻代又会分为`Eden`区和`Survivor`区，其空间大小理论比值为8：2；`Surfivor`区又分为`from`区和`to`区，其空间大小理论比值为1：1。  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/jvm_heap_memory.png)  

#### 1.1.1 `gc`流程

创建对象时首先会被放入`Eden`区，该区存满时会触发`gc`，`gc`时清除可回收对象，然后把`Eden`区剩余存活对象移动到`From`区；新创建的对象会继续被放入`Eden`区，第二次`gc`时清除`Eden`区和和`From`区可回收对象，然后把`Eden`区和`From`区剩余存活对象移动到`To`区；第三次`gc`时会把`Eden`区和`To`区中剩余存活对象移动到`From`区……依次反复进行。

从年轻代进入老年代的条件：

> 1. 大对象，大对象会直接进入老年代；  
> 2. 每次`gc`时会对已存活对象进行标记（每次+1），标记达到一定次数（`Java`为**15次**，`Android`的`CMS`垃圾回收器为**6次**）时该对象会从年轻代进入老年代；  
> 3. `Survivor`区中`From`或`To`区中的相同标记（相同年龄）对象大小总和大于等于`From`或`To`区的一半时，这些对象**可以**进入老年代。  

在`Java`环境的`bin`目录下有一个**`jvisualvm`**工具，该工具可以观察到程序运行过程中内存的动态情况，从而证实上述描述。    

该工具启动后，首先需要安装`visual GC`插件：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/memory_gc_look_tool.png)  

安装完成之后点击`VisualVM`启动，然后运行我们申请大量内存的测试代码（本项目在`Java`测试模块下），通过该工具可以观察到内存`gc`的流程：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/memory_gc_process_look.png)  

#### 1.1.2  `gc`算法

`gc`三大算法：

> 1. 复制算法（Copying）：年轻代；  
> 2. 标记清除算法（Mark-Sweep）：老年代，该算法可能导致内存碎片；  
> 3. 标记整理算法。

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/gc_algorithm.png)  

## 二、 内存抖动

频繁分配与回收内存可能导致**内存抖动**问题，常伴随着频繁`gc`。

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/memory_shake.png)  

内存抖动的影响：卡顿、OOM。

### 2.1 内存抖动原因及排查工具

#### 2.1.1 为什么会卡顿？

`gc` 的时候会进行 STW( stop the world)，所以会卡顿。  

  ![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/interface_stuck_reason.png)  

#### 2.1.2 为什么会`OOM`？

`CMS`垃圾回收器的老年代标记-清除算法会导致内存碎片，从而引起`OOM`。

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/oom_reason.png)  

#### 2.1.3 排查内存抖动的工具

* `Android Studio` 的 `Profile`  
* `LeakCanary`

### 2.2 项目实操之`Profile`分析内存

#### 2.2.1 分析过程

`Profile`分析内存时(本项目使用自定义的`IOSStyleLoadingView2`为例)，可以用鼠标左键点击某个时间点的内存图像，然后向右拖动一段距离，即选择该时间段中内存情况，此时该段时间内创建的对象会展示出来：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/profile_memory_analyse_process.png)  

从图中可以看出自己定义的出现频率最高的对象`Paint`、`Path`以及`String`等创建频率高的对象具有很大的怀疑可能，然后从代码中可以分析出其是在`onDraw()`方法中创建了对象所致。

>`FinalizerReference` 对象：`Object`对象中有一个`finalize()`方法，复写该方法并添加自己实现的对象便会生成`FinalizerReference`对象。
>
>```java
>@Override
>protected void finalize() throws Throwable {
>	super.finalize();
>	//添加自己的实现
>}
>```
>

#### 2.2.2 结论

为防止内存抖动现象的发生，应把握以下原则：  

> 1. 尽量避免在循环和频繁调用的方法中创建对象；  
> 2. 合理利用对象池。  

自定义对象复用池

> 1. 怎么设计 ?  
> Handler: 单链表设计对象池  
>     	-->方便，不需要关心对象的差异；  
> Glide: Bitmap复用池(享元模式)，map集合（LruCache --> map）  
> 	  -->享元模式，对象每个都不同，不能替代（内存必须足够map<size,bitmap> 100字节 --1000字节）  
> 2. 怎么使用

## 三、内存泄漏（`OOM`）

程序中已动态分配的堆内存由于某种原因程序未释放或者无法释放，造成系统内存的浪费，从而可能引起内存泄漏。  长生命周期对象持有短生命周期对象的**强引用**，从而导致短生命周期对象无法被回收。  

### 3.1 内存泄漏分析方法

内存泄漏可以采用**可达性分析法** 分析。

可达性分析法：通过一系列称为`GC Roots`的对象作为起始点，从这些节点向下搜索，搜索所有的引用链，当一个对象到`GC Roots`没有任何引用链（即`GC Roots`到对象不可达）时，则证明此对象是不可用的。  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/leak_memory_analyse_method.png)  

### 3.2 `Java`四大引用类型

`JAVA`有四大引用类型：强、软、弱、虚。

应用场景（`Java`）：

> 软应用：内存是在不足时gc时才回收；  
> 弱引用：希望对象不用了就及时回收（只要gc就被回收）。

Tips：在`Android`中软引用和弱引用都不可靠，可能表现一致（只要gc就回收）。  

**测试代码可参考本项目`java`测试模块下`ReferenceTest`代码。**

#### 3.2.1 强引用

```java
Object object = new Object();
```

#### 3.2.2 软引用

定义一些还有用但并非必须的对象，对应软引用关联的对象，GC不会直接回收，而是在系统将要内存溢出之前才会触发GC将这些对象进行回收。  

```java
Object softObject =new Object();
SoftReference<Object> objectSoftReference = new SoftReference<>(softObject);
```

#### 3.2 3 弱引用

同样定义非必须对象，被弱引用关联的对象在GC执行时会被直接回收。 

```java
Object weakObject =new Object();
WeakReference<Object> objectWeakReference = new WeakReference<>(weakObject);
```

#### 3.2.4 虚引用

```java
Object phantomObject = new Object();
ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> objectPhantomReference = new PhantomReference<Object>(phantomObject, queue);
```

### 3.3 内存泄漏排查工具及使用

内存泄漏分析工具使用`MAT`，它除了可以分析内存泄漏之外，还可以分析大对象。

#### 3.3.1 下载`MAT`

官方下载地址：[https://www.eclipse.org/mat/downloads.php](https://www.eclipse.org/mat/downloads.php) 。

#### 3.3.2 安装`MAT`

下载的是`MAC`版，安装时遇到一个问题：  

```java
The platform metadata area could not be written: /private/var/folders/9j/zj116b2n765fkk7qm1s7ctq8000
```

解决方法：在应用程序中右键`mat.app`-->显示包内容-->`Contents/Eclipse/MemoryAnalyzer.ini`，修改内容如下：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/mat_install_problem.png)  

#### 3.3.3 获取`hprof`文件

借助`Android Studio` 的`Profile`工具，在操作页面之前`dump`（截取该时间点内存中存在的对象）一份文件，操作页面（比如进入`SecondActivity` 然后再返回主页面）之后再`dump`一份文件。然后把这两份文件（`memory1.hprof，memory2.hprof`）保存到本地。

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/dump.png)  

#### 3.3.4 转换`hprof`文件

使用`Android SDK`环境`sdk/platform-tools/`目录下的`hprof-conv`工具将3.3.3获取的`hprof`文件转换为`MAT`可以识别的文件：  

```bash
hprof-conv -z memory1.hprof memory1_after.hprof
hprof-conv -z memory2.hprof memory2_after.hprof
```

#### 3.3.5 `Mat`分析`hprof`文件

首先用`Mat`打开（`Open Heap Dump..`）两个转换后的`hprof`文件：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/mat_open_hprof.png)  

选择直方图：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/mat_histogram.png)  

排除其他引用：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/mat_exclude_other_references.png)  

定位结果：  

![image](https://github.com/tianyalu/XxtJvmMemory/raw/master/show/mat_memory_leak_result.png)  

因为我们进入`SecondActivity`之后又退出页面了，按道理其不应该存在，但此时排除其他引用之后发现它仍然存活，由此可以判断内存泄漏。从上图可以看出这是匿名内部类持有外部类引用引起的内存泄漏，需要在页面销毁时结束动画。


# 铁观音

## 为什么叫这个？

我如果说，是因为今天泡茶，铁观音不小心放多了，你们会信吗？

## 项目是做什么的？

1. 通过注解标注 Activity、Fragment 及其参数（通常我们需要手动用 Intent 来传递）和结果（通常我们需要覆写 onActivityResult 来接收）来生成一些扩展方法来帮助你安全、便捷地启动这些页面；
2. 除了提供用于启动的快捷方法之外，还提供了自动注入的支持，用注解标注的参数无需手动赋值，在 Activity/Fragment 的 onCreate 方法执行完之后这些值都会由框架完成注入。
3. 支持对 Activity、Fragment 父类标注的成员进行生成和注入，也就是说如果你有较为复杂的继承关系，而父类当中也有部分需要赋值的参数或者返回的结果，我们的框架也是支持的，这些字段会与当前类的字段一视同仁。
4. 支持传入参数的保存和恢复（通常需要在 onSaveInstanceState 这样的方法中保存，在 onCreate 或者 onViewStateRestored 中恢复）。
5. 对于接收 Activity 参数的情形，回调支持当前 Activity 被销毁后被重新恢复的情形，框架会为你更新回调的执行环境。
6. 如果以上都不能满足你得需求，你必须使用 Intent 进行参数传递，没关系，我们也提供了字段名常量，方便使用。

如果你没有饱受硬编码 Intent 中 extras 的 key 的折磨，那么你一定不会想到这个框架能给你带来什么。如果你不知道，那也没关系，迟早有一天你会知道的。

## 项目的亮点

1. 除了 Demo，源码中一行 Kotlin 都没有，但整个库对 Kotlin 似乎更友好。
2. 对 Java 也很友好啊。
3. 除了添加注解，你不需要编写任何初始化代码。

## 给你们看个例子

* 如何启动 Activity

	``` kotlin
	class MainActivity : AppCompatActivity() {
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	
	        button.setOnClickListener {
	            startKotlinActivity(1234){
	                java, kotlin ->
	                toast("Result From JavaActivity: java=$java, kotlin=$kotlin" )
	            }
	        }
	    }
	}
	```

	看看我们的目标 KotlinActivity ：

	```kotlin
	@ActivityBuilder(resultTypes = [(ResultEntity(name = "java", type = String::class)), (ResultEntity(name = "kotlin", type = Int::class))])
	class KotlinActivity : AppCompatActivity() {
	
	    @Required
	    var num: Int = 0
	
	    @Optional
	    var java: Boolean = false
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	        setTitle(this.javaClass.simpleName)
	
	        textView.text = "Finish With java='I'm Kotlin!' & kotlin=12"
	        button.setOnClickListener {
	            finishWithResult("I'm Kotlin!", 12)
	        }
	    }
	}
	```

* 如何显示 Fragment：

	```kotlin
	@ActivityBuilder
	class FragmentContainerActivity : AppCompatActivity() {
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_fragment)
	
	        showKotlinFragment(R.id.fragmentContainer, "Kotlin!!")
	    }
	}
	```

	而我们的 KotlinFragment：
	
	```kotlin
	@FragmentBuilder
	class KotlinFragment : Fragment() {
	
	    @Required
	    lateinit var text: String
	
	    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
	        return inflater.inflate(R.layout.fragment_main, container, false)
	    }
	}
	```

## 如何接入？

仓库配置：

```
repositories {
    ...
    jcenter()
    ...
}
```

依赖配置：

```
compile 'com.bennyhuo.tieguanyin:tieguanyin-runtime:0.2-rc3'
kapt 'com.bennyhuo.tieguanyin:tieguanyin-compiler:0.2-rc3'
```
如果你不用 Kotlin，那么 kapt 替换成 annotationProcessor。

## 项目状态

1. 当前项目还在迭代中，难免有些使用场景没有考虑到或者暂时没有顾及，如果你发现了问题，请任性的开 issue。
2. 目前我也已经开始在我的项目中集成使用，框架的 api 基本稳定基本不再进行调整。
3. 后续以优化为主。
	
## License

> [Apache License 2.0](https://github.com/enbandari/TieGuanYin/blob/master/LICENSE)
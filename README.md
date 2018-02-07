# 铁观音

## 为什么叫这个？

我如果说，是因为今天泡茶，铁观音不小心放多了，你们会信吗？

## 项目是做什么的？

1. 通过注解标注 Activity、Fragment 及其参数和返回值（返回值只支持 Activity）来生成一些扩展方法来帮助你安全、便捷地启动这些页面；
2. 除了提供用于启动的快捷方法之外，还提供了自动注入的支持。

如果你没有饱受硬编码 Intent 中 extras 的 key 的折磨，那么你一定不会想到这个库能给你带来什么。如果你不知道，那也没关系，迟早有一天你会知道的。

## 项目的亮点

除了 Demo，源码中一行 Kotlin 都没有，但整个库对 Kotlin 似乎更友好。

## 给你们看个例子

* 如何启动 Activity

	``` kotlin
	class MainActivity : AppCompatActivity() {
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	
	        openKotlinActivity.setOnClickListener {
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
	
	        openKotlinActivity.text = "Finish With java='I'm Kotlin!' & kotlin=12"
	        openKotlinActivity.setOnClickListener {
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
    maven { url 'https://jitpack.io' }
    ...
}
```

依赖配置：

```
compile 'com.github.enbandari.tieguanyin:runtime:master-SNAPSHOT'
kapt 'com.github.enbandari.tieguanyin:compiler:master-SNAPSHOT'
```
如果你不用 Kotlin，那么 kapt 替换成 annotationProcessor。

## 项目规划

看心情。
	
## License

> [Apache License 2.0](https://github.com/enbandari/TieGuanYin/blob/master/LICENSE)
# Tieguanyin(铁观音)

## 项目是做什么的？

### 我们遇到了怎样的问题

我们先来看个例子：

```java
public class UserActivity extends Activity {

    String name;
    int age;
    String title;
    String company;
    
    ...
}
```
我们有这样一个 `Activity`，启动它，我们需要传入四个参数，那么我们通常会怎么做呢？

```java
Intent intent = new Intent(this, UserActivity.class);
intent.putExtra("age", age);
intent.putExtra("name", name);
intent.putExtra("company", company);
intent.putExtra("title", title);
startActivity(intent);
```

仅仅是这样，还不够，所以我们还需要在 `UserActivity` 这个类当中去读取这些值：

```java
Intent intent = getIntent();
this.age = intent.getIntExtra( "age", 0);
this.name = intent.getStringExtra("name");
this.company = intent.getStringExtra("company" );
this.title = intent.getStringExtra("title");
```

如果你只有这么一个 `Activity` 那倒也还好，可是如果你有十个这样的 `Activity` 呢？

### 我们怎么去解决

其实我们仔细观察前面的代码，就会发现这两大段传参和读参的代码，都是模式化的代码，我们只需要通过注解处理器来生成就可以了，因此我们给出的解决方法是：

```java
@Builder
public class UserActivity extends Activity {

    @Required
    String name;

    @Required
    int age;

    @Optional
    String title;

    @Optional
    String company;
    
    ...
}
```

这样的话，对于 Java 代码，我们会生成 `UserActivityBuilder`，通过它启动 `UserActivity`：

```java
UserActivityBuilder.builder(30, "bennyhuo")
        .company("Kotliner")
        .title("Kotlin Developer")
        .start(this);
```
注意到，我们的 `name` 和 `age` 都是 `Required`，因此我们生成的 Builder 在构造时必须对他们进行赋值，而其他两个因为是 `Optional`，用户可以根据实际情况选择性调用。

而对于 Kotlin 来说，我们则选择为 `Context`、`View`、`Fragment` 生成扩展方法，所以我们只需要：

```kotlin
startUserActivity(30, "bennyhuo", "Kotliner", "Kotlin Developer")
```

需要注意的是，对于 `company` 和 `title` 这两个可选的字段，我们的扩展方法提供了默认参数 `null`，因此我们可以选择性提供这些参数的值：

```kotlin
startUserActivity(30, "bennyhuo",  title = "Kotlin Developer")
```

这些方便快捷的方法帮我们处理了 `Intent` 传递参数的过程，当然，我们也在运行时对 `Activity` 的声明周期进行了监听，在 `Activity` 的 `onCreate` 方法调用时，对这些参数进行了注入，因此：

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...
    nameView.setText(name);
    ...
}
```

在 `super.onCreate(savedInstanceState);` 之后，属性 `name` 就已经被合理的初始化了。

对 `Fragment` 我们也提供了类似的逻辑。

### 状态保存

在一些特定的场景下，例如转屏时，`Activity` 或者 `Fragment` 会被销毁并重新创建，销毁前会调用 `onSaveInstanceState` 来保存状态。我们同样通过监听其生命周期来实现对用户配置好的属性的值进行保存，以保证这些属性在 `Activity` 或者 `Fragment` 重新创建时能够得以恢复。

### `Activity` 转场

除了提供参数传递功能外，还支持通过注解为 `Activity` 配置 `pendingTransition`，例如：

```java
@Builder(pendingTransition = PendingTransition(enterAnim = R.anim.fade_in, exitAnim = R.anim.fade_out))
class UserActivity : AppCompatActivity() {
    ...
}
```

这样每次启动 `UserActivity` 时，我们都会在相应的方法当中调用 `overridePendingTransition` 来设置这些转场动画。

### `SharedElement` 元素动画

从 Android 5.0 开始，系统在 Activity、Fragment、View 之间支持了共享元素动画，但接口使用起来略显复杂，因此我们通过对 `Activity` 或者 `Fragment` 添加注解，在启动或者显示相应的组件时，调用相应的方法来实现共享元素动画，让页面的跳转更加连贯。

我们支持用户通过 `id`、`transitionName` 来实现元素的关联。

```kotlin
@Builder(
        sharedElements = [SharedElement(sourceId = R.id.openJavaActivity, targetName = "hello")],
        sharedElementsWithName = [(SharedElementWithName("button2"))],
        sharedElementsByNames= [(SharedElementByNames(source = "button1",target = "button3"))]
)
class DetailsActivity : AppCompatActivity() {
    ...
}
```

### `Activity` 的结果

有些情况下我们需要目标 `Activity` 在结束时回传一些结果给当前 `Activity`，例如我们为了修改用户信息，需要从 `UserActivity` 跳转到 `EditUserActivity`，编辑完成之后需要把修改后的结果返回给 `UserActivity`，我们只需要：

```java
@Builder(resultTypes = {@ResultEntity(name = "name", type = String.class),
                @ResultEntity(name = "age", type = int.class),
                @ResultEntity(name = "title", type = String.class),
                @ResultEntity(name = "company", type = String.class)})
public class EditUserActivity extends Activity {
    ...
}
```

这样我们就可以这样启动 `EditUserActivity`：

```java
EditUserActivityBuilder.builder(30, "Kotlin", "bennyhuo", "Kotlin Developer")
        .start(this, new EditUserActivityBuilder.OnEditUserActivityResultListener() {
            @Override
            public void onResult(int age, String company, String name, String title) {
                ... // handle result
            }
        });
```

编辑之后这样返回：

```java
EditUserActivityBuilder.smartFinish(this, 36, "Kotliner","bennyhuo", "Kotlin Dev");
```

如果是 Kotlin 代码，那么我们还可以使用 Lambda 表达式让代码变得简单：

```kotlin
startEditUserActivity(36, "Kotliner", "bennyhuo", "Kotlin Dev"){
    age, company, name, title ->  
    ... // handle result
}
```

>值得一提的是，对于在编辑用户信息时， `UserActivity` 的实例因各种原因（例如开发者选项中的”不保留活动“开启时）被销毁，从 `EditUserActivity` 返回时，`UserActivity` 被重新创建，导致之间的回调（匿名内部类、Lambda 表达式）持有的外部引用失效，进而使回调没有意义。为了解决这个问题，我会在页面返回，上一个页面被重新创建时尝试替换掉失效的实例以保证回调可以正常使用，但这个功能比较 Tricky，如果大家在使用过程中发现回调调用之后没有反应，那么请开 Issue 一起讨论下。

### 属性名常量

有些情况下，大家在页面跳转时不是很方便调用我们生成的方法，那么这时候为了方便使用，我们也会生成以属性名为值的常量，方便使用，例如：

```java
public final class UserActivityBuilder {
  public static final String REQUIRED_age = "age";
  public static final String REQUIRED_name = "name";
  public static final String OPTIONAL_company = "company";
  public static final String OPTIONAL_title = "title";
  ...
}
```

## 项目如何接入？

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
api "com.bennyhuo.tieguanyin:tieguanyin-runtime:$latest_version"
kapt "com.bennyhuo.tieguanyin:tieguanyin-compiler:$latest_version"
```
如果你不用 Kotlin，那么 kapt 替换成 annotationProcessor。

## 项目状态

* 当前最新版本：**2.0-beta1**
* 当前项目的 compiler 模块已经使用 Kotlin 重构，代码较 1.0 时更紧凑和灵活，部分 Api 也做了一些调整。
* 为了保证纯 Java 用户的正常使用，runtime 和 annotation 两个模块将一直使用纯 Java 开发。
	
## 其他相关

* **[Apt-Utils](https://github.com/enbandari/Apt-Utils)**：解决了类型在 Java 和 Kotlin 之间的统一性和兼容性问题，提供了注解处理器一些常用的工具方法，尤其适合同时生成 Java 和 Kotlin 代码的注解处理器项目。
* **[Apt-Tutorials](https://github.com/enbandari/Apt-Tutorials)**：基于本项目简化后并录制的一套**注解处理器**的教学视频。
	
## 为什么叫这个名字？
	
因为我比较喜好喝茶，这个框架开发期间主要喝铁观音。相应的，之前有一段时间常喝茉莉花，在公司内部做了一套框架被我命名为 "Jasmine"。

## License

> [Apache License 2.0](https://github.com/enbandari/TieGuanYin/blob/master/LICENSE)


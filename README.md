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

值得一提的是，对于在编辑用户信息时， `UserActivity` 的实例因各种原因（例如开发者选项中的”不保留活动“开启时）被销毁，从 `EditUserActivity` 返回时，`UserActivity` 被重新创建，导致之间的回调（匿名内部类、Lambda 表达式）持有的外部引用失效，进而使回调没有意义。为了解决这个问题，我会在页面返回，上一个页面被重新创建时尝试替换掉失效的实例以保证回调可以正常使用，其中主要包括：

1. 外部 `Activity` 的实例，这个通常没有问题。
2. 外部 `View` 的实例，通常也是回调所在的 `Activity` 当中的 `View`，在更新实例时，我们通过 `View` 的 id 来索引，因此如果布局当中有重复的 id，回调可能将无法更新到正确的实例而产生问题。因此请注意保持 `Activity` 的布局当中 `View` 的 id 的唯一性。
3. 外部 `Fragment` 的实例，通常也是所在的 `Activity` 当中的 `Fragment`，为了保证 `Fragment` 的唯一性，我使用了 `Fragment` 未公开的属性 `mWho` 来进行索引。

尽管从理论的角度，这个更新实例的方法较为可靠，但毕竟这个功能比较 Tricky，如果大家在使用过程中发现回调调用之后没有反应，那么请开 Issue 一起讨论解决方案。

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

### `Fragment` 支持

由于从 API 28 开始，Android 废弃了 `android.app.Fragment` 相关的 API，转而推荐使用 `support-fragment`，同时由于框架本身也需要监听 `Fragment` 的生命周期，因此我们对于 `android.app.Fragment` 不予支持，请谅解。

## 项目如何接入？

仓库配置：

```
// snapshot
repositories {
    ...
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots/"
    }
    ...
}

// release
repositories {
    ...
    mavenCentral()
    ...
}
```

依赖配置：

```
plugins {
    id 'com.android.application'
    id 'kotlin-android'

    // for ksp
    id("com.google.devtools.ksp").version("1.5.31-1.0.1")
    // for kapt
    id "kotlin-kapt"
}

dependencies {
    // for android support
    api "com.bennyhuo.tieguanyin:runtime:$latest_version"
    // for androidx
    api "com.bennyhuo.tieguanyin:runtime-androidx:$latest_version"
    // for kapt
    kapt "com.bennyhuo.tieguanyin:compiler:$latest_version"
    // for ksp
    ksp "com.bennyhuo.tieguanyin:compiler-ksp:$latest_version"
}
```

当前版本：kapt 2.0.1/ksp 2.1.0。其中，kapt 和 ksp 选一个即可；如果你不用 Kotlin，那么 kapt 替换成 annotationProcessor。需要注意的是，kapt 的版本我不准备维护了，因此请尽快迁移至 ksp 版本。

最后在 `Application` 的 `onCreate` 当中调用：

```java
Tieguanyin.init(this);
```
即可。


### NewIntent

由于 `onNewIntent` 没有相应的回调，我们无法在框架内部做到用户无感的数据注入，因此如果你需要处理这种情况，请主动调用 `processNewIntent` 方法：

在 Java  中：

```java
@Override
public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    MyActivityBuilder.processNewIntent(this, intent);
}
```

在 Kotlin 中：

```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    processNewIntent(intent)
}
```

我们也提供了参数 `updateIntent`，如果你不希望在注入数据的时候同时也调用 `setIntent(intent)` 来更新 `activity` 的 `intent`，请将它置为 `false`。

## 更新日志

### compiler-ksp & annotations 2.1.0

1. 废弃 Optional 注解当中的默认值字段，数值、字符串的默认值可以直接在声明处指定。
2. 为生成的代码添加字段文档
3. 优化生成的函数，统一调用路径
4. 为 Builder 类型添加 onIntent 回调，方便调用者自定义 intent
5. 参数和返回值的常量字段名改为全大写

## 其他相关

* **[Apt-Utils](https://github.com/enbandari/Apt-Utils)**：解决了类型在 Java 和 Kotlin 之间的统一性和兼容性问题，提供了注解处理器一些常用的工具方法，尤其适合同时生成 Java 和 Kotlin 代码的注解处理器项目。
* **[Apt-Tutorials](https://github.com/enbandari/Apt-Tutorials)**：基于本项目简化后并录制的一套**注解处理器**的教学视频。

## License

> [Apache License 2.0](https://github.com/enbandari/TieGuanYin/blob/master/LICENSE)


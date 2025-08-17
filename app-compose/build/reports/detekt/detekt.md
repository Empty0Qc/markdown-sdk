# detekt

## Metrics

* 97 number of properties

* 43 number of functions

* 12 number of classes

* 3 number of packages

* 7 number of kt files

## Complexity Report

* 1,228 lines of code (loc)

* 888 source lines of code (sloc)

* 613 logical lines of code (lloc)

* 192 comment lines of code (cloc)

* 119 cyclomatic complexity (mcc)

* 98 cognitive complexity

* 43 number of total code smells

* 21% comment source ratio

* 194 mcc per 1,000 lloc

* 70 code smells per 1,000 lloc

## Findings (43)

### complexity, LongMethod (1)

One method should have one responsibility. Long methods tend to handle many things at once. Prefer smaller methods to make them easier to understand.

[Documentation](https://detekt.dev/docs/rules/complexity#longmethod)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:61:5
```
The function MarkdownDemoScreen is too long (177). The maximum length is 60.
```
```kotlin
58 
59 @OptIn(ExperimentalMaterial3Api::class)
60 @Composable
61 fun MarkdownDemoScreen(
!!     ^ error
62     modifier: Modifier = Modifier,
63     markdownEngine: MarkdownEngine? = null,
64     fontSize: Float = 16f,

```

### complexity, LongParameterList (2)

The more parameters a function has the more complex it is. Long parameter lists are often used to control complex algorithms and violate the Single Responsibility Principle. Prefer functions with short parameter lists.

[Documentation](https://detekt.dev/docs/rules/complexity#longparameterlist)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:61:23
```
The function MarkdownDemoScreen(modifier: Modifier, markdownEngine: MarkdownEngine?, fontSize: Float, renderSpeed: ProgressiveRenderer.RenderSpeed, onThemeChange: () -> Unit, onFontSizeChange: (Float) -> Unit, onRenderSpeedChange: (ProgressiveRenderer.RenderSpeed) -> Unit) has too many parameters. The current threshold is set to 6.
```
```kotlin
58 
59 @OptIn(ExperimentalMaterial3Api::class)
60 @Composable
61 fun MarkdownDemoScreen(
!!                       ^ error
62     modifier: Modifier = Modifier,
63     markdownEngine: MarkdownEngine? = null,
64     fontSize: Float = 16f,

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:273:35
```
The function renderMarkdown(markdownEngine: MarkdownEngine?, content: String, isAsync: Boolean, onStart: () -> Unit, onComplete: () -> Unit, onError: (String) -> Unit) has too many parameters. The current threshold is set to 6.
```
```kotlin
270     }
271 }
272 
273 private suspend fun renderMarkdown(
!!!                                   ^ error
274     markdownEngine: MarkdownEngine?,
275     content: String,
276     isAsync: Boolean,

```

### complexity, TooManyFunctions (2)

Too many functions inside a/an file/class/object/interface always indicate a violation of the single responsibility principle. Maybe the file/class/object/interface wants to manage too many things at once. Extract functionality which clearly belongs together.

[Documentation](https://detekt.dev/docs/rules/complexity#toomanyfunctions)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/AutoScrollManager.kt:15:7
```
Class 'AutoScrollManager' with '16' functions detected. Defined threshold inside classes is set to '11'
```
```kotlin
12  * 自动滚动管理器
13  * 在Markdown内容流式输出过程中实现自动滚动功能
14  */
15 class AutoScrollManager {
!!       ^ error
16 
17     companion object {
18         private const val SCROLL_CHECK_INTERVAL = 100L // 检查间隔

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:17:7
```
Class 'ProgressiveRenderer' with '15' functions detected. Defined threshold inside classes is set to '11'
```
```kotlin
14  * 统一的渐进式内容渲染器
15  * 合并了流式输出和打字机效果，提供真正的渐进式内容显示
16  */
17 class ProgressiveRenderer {
!!       ^ error
18 
19     /**
20      * 渲染模式

```

### exceptions, SwallowedException (3)

The caught exception is swallowed. The original exception could be lost.

[Documentation](https://detekt.dev/docs/rules/exceptions#swallowedexception)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:197:22
```
The caught exception is swallowed. The original exception could be lost.
```
```kotlin
194                     onProgressUpdate?.invoke(100, "渲染完成")
195                     onComplete?.invoke()
196                 }
197             } catch (e: Exception) {
!!!                      ^ error
198                 isRendering = false
199                 autoScrollManager?.stopAutoScroll()
200                 textView.text = content

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:263:22
```
The caught exception is swallowed. The original exception could be lost.
```
```kotlin
260                     onProgressUpdate?.invoke(100, "渲染完成")
261                     onComplete?.invoke()
262                 }
263             } catch (e: Exception) {
!!!                      ^ error
264                 isRendering = false
265                 autoScrollManager?.stopAutoScroll()
266                 textView.text = content

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:311:18
```
The caught exception is swallowed. The original exception could be lost.
```
```kotlin
308             withContext(Dispatchers.Main) {
309                 onLoaded(content)
310             }
311         } catch (e: IOException) {
!!!                  ^ error
312             // 如果文件不存在，使用示例内容
313             withContext(Dispatchers.Main) {
314                 onLoaded(getSampleMarkdown())

```

### exceptions, TooGenericExceptionCaught (5)

The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.

[Documentation](https://detekt.dev/docs/rules/exceptions#toogenericexceptioncaught)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:197:22
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
194                     onProgressUpdate?.invoke(100, "渲染完成")
195                     onComplete?.invoke()
196                 }
197             } catch (e: Exception) {
!!!                      ^ error
198                 isRendering = false
199                 autoScrollManager?.stopAutoScroll()
200                 textView.text = content

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:263:22
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
260                     onProgressUpdate?.invoke(100, "渲染完成")
261                     onComplete?.invoke()
262                 }
263             } catch (e: Exception) {
!!!                      ^ error
264                 isRendering = false
265                 autoScrollManager?.stopAutoScroll()
266                 textView.text = content

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:170:42
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
167                                             isLoading = false
168                                         }
169                                     )
170                                 } catch (e: Exception) {
!!!                                          ^ error
171                                     errorMessage = e.message
172                                     isLoading = false
173                                 }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:209:42
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
206                                             isLoading = false
207                                         }
208                                     )
209                                 } catch (e: Exception) {
!!!                                          ^ error
210                                     errorMessage = e.message
211                                     isLoading = false
212                                 }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:295:14
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
292         }
293 
294         onComplete()
295     } catch (e: Exception) {
!!!              ^ error
296         onError(e.message ?: "未知错误")
297     }
298 }

```

### style, MagicNumber (22)

Report magic numbers. Magic number is a numeric literal that is not defined as a constant and hence it's unclear what the purpose of this number is. It's better to declare such numbers as constants and give them a proper name. By default, -1, 0, 1, and 2 are not considered to be magic numbers.

[Documentation](https://detekt.dev/docs/rules/style#magicnumber)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/MainActivity.kt:45:28
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
42 
43     private lateinit var markdownEngine: MarkdownEngine
44     private var isDarkMode = false
45     private var fontSize = 16f
!!                            ^ error
46     private var renderSpeed = ProgressiveRenderer.RenderSpeed.NORMAL
47 
48     override fun onCreate(savedInstanceState: Bundle?) {

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/MainActivity.kt:127:57
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
124             }
125 
126             R.id.action_font_size_increase -> {
127                 fontSize = (fontSize + 2f).coerceAtMost(24f)
!!!                                                         ^ error
128                 recreate()
129                 true
130             }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/MainActivity.kt:133:58
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
130             }
131 
132             R.id.action_font_size_decrease -> {
133                 fontSize = (fontSize - 2f).coerceAtLeast(12f)
!!!                                                          ^ error
134                 recreate()
135                 true
136             }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/MainActivity.kt:139:28
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
136             }
137 
138             R.id.action_font_size_reset -> {
139                 fontSize = 16f
!!!                            ^ error
140                 recreate()
141                 true
142             }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:32:14
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
29      * 渲染速度
30      */
31     enum class RenderSpeed(val delayMs: Long) {
32         SLOW(80L),
!!              ^ error
33         NORMAL(40L),
34         FAST(20L),
35         INSTANT(0L)

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:33:16
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
30      */
31     enum class RenderSpeed(val delayMs: Long) {
32         SLOW(80L),
33         NORMAL(40L),
!!                ^ error
34         FAST(20L),
35         INSTANT(0L)
36     }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:34:14
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
31     enum class RenderSpeed(val delayMs: Long) {
32         SLOW(80L),
33         NORMAL(40L),
34         FAST(20L),
!!              ^ error
35         INSTANT(0L)
36     }
37 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:129:42
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
126         when (renderMode) {
127             RenderMode.INSTANT -> {
128                 textView.text = content
129                 onProgressUpdate?.invoke(100, "渲染完成")
!!!                                          ^ error
130                 onComplete?.invoke()
131             }
132             RenderMode.PROGRESSIVE -> {

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:180:47
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
177                     textView.text = displayText
178 
179                     // 更新进度
180                     val progress = ((i + 1) * 100 / contentLength)
!!!                                               ^ error
181                     onProgressUpdate?.invoke(progress, "正在渲染... ($progress%)")
182 
183                     // 控制速度

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:194:46
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
191                     textView.text = content
192                     isRendering = false
193                     autoScrollManager?.stopAutoScroll()
194                     onProgressUpdate?.invoke(100, "渲染完成")
!!!                                              ^ error
195                     onComplete?.invoke()
196                 }
197             } catch (e: Exception) {

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:201:42
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
198                 isRendering = false
199                 autoScrollManager?.stopAutoScroll()
200                 textView.text = content
201                 onProgressUpdate?.invoke(100, "渲染完成")
!!!                                          ^ error
202                 onComplete?.invoke()
203             }
204         }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:244:46
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
241                     textView.text = builder
242 
243                     // 更新进度
244                     val progress = (endPos * 100 / contentLength)
!!!                                              ^ error
245                     onProgressUpdate?.invoke(progress, "正在渲染... ($progress%)")
246 
247                     currentPos = endPos

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:251:53
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
248 
249                     // 控制速度
250                     if (renderSpeed.delayMs > 0) {
251                         delay(renderSpeed.delayMs * 5) // 分块模式稍慢一些
!!!                                                     ^ error
252                     }
253                 }
254 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:260:46
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
257                     textView.text = content
258                     isRendering = false
259                     autoScrollManager?.stopAutoScroll()
260                     onProgressUpdate?.invoke(100, "渲染完成")
!!!                                              ^ error
261                     onComplete?.invoke()
262                 }
263             } catch (e: Exception) {

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:267:42
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
264                 isRendering = false
265                 autoScrollManager?.stopAutoScroll()
266                 textView.text = content
267                 onProgressUpdate?.invoke(100, "渲染完成")
!!!                                          ^ error
268                 onComplete?.invoke()
269             }
270         }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ProgressiveRenderer.kt:347:34
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
344     fun completeImmediately(textView: TextView, content: Spanned) {
345         stopRender()
346         textView.text = content
347         onProgressUpdate?.invoke(100, "渲染完成")
!!!                                  ^ error
348     }
349 }
350 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/theme/Color.kt:5:22
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
2 
3 import androidx.compose.ui.graphics.Color
4 
5 val Purple80 = Color(0xFFD0BCFF)
!                      ^ error
6 val PurpleGrey80 = Color(0xFFCCC2DC)
7 val Pink80 = Color(0xFFEFB8C8)
8 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/theme/Color.kt:6:26
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
3  import androidx.compose.ui.graphics.Color
4  
5  val Purple80 = Color(0xFFD0BCFF)
6  val PurpleGrey80 = Color(0xFFCCC2DC)
!                           ^ error
7  val Pink80 = Color(0xFFEFB8C8)
8  
9  val Purple40 = Color(0xFF6650a4)

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/theme/Color.kt:7:20
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
4  
5  val Purple80 = Color(0xFFD0BCFF)
6  val PurpleGrey80 = Color(0xFFCCC2DC)
7  val Pink80 = Color(0xFFEFB8C8)
!                     ^ error
8  
9  val Purple40 = Color(0xFF6650a4)
10 val PurpleGrey40 = Color(0xFF625b71)

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/theme/Color.kt:9:22
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
6  val PurpleGrey80 = Color(0xFFCCC2DC)
7  val Pink80 = Color(0xFFEFB8C8)
8  
9  val Purple40 = Color(0xFF6650a4)
!                       ^ error
10 val PurpleGrey40 = Color(0xFF625b71)
11 val Pink40 = Color(0xFF7D5260)
12 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/theme/Color.kt:10:26
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
7  val Pink80 = Color(0xFFEFB8C8)
8  
9  val Purple40 = Color(0xFF6650a4)
10 val PurpleGrey40 = Color(0xFF625b71)
!!                          ^ error
11 val Pink40 = Color(0xFF7D5260)
12 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/theme/Color.kt:11:20
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
8  
9  val Purple40 = Color(0xFF6650a4)
10 val PurpleGrey40 = Color(0xFF625b71)
11 val Pink40 = Color(0xFF7D5260)
!!                    ^ error
12 

```

### style, ReturnCount (5)

Restrict the number of return statements in methods.

[Documentation](https://detekt.dev/docs/rules/style#returncount)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/AutoScrollManager.kt:104:25
```
Function checkAndScroll has 3 return statements which exceeds the limit of 2.
```
```kotlin
101     /**
102      * 检查是否需要滚动并执行滚动
103      */
104     private suspend fun checkAndScroll() {
!!!                         ^ error
105         val scrollView = this.scrollView ?: return
106         val textView = this.targetTextView ?: return
107 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/AutoScrollManager.kt:119:17
```
Function isContentOverflowing has 3 return statements which exceeds the limit of 2.
```
```kotlin
116     /**
117      * 检查内容是否溢出可视区域
118      */
119     private fun isContentOverflowing(): Boolean {
!!!                 ^ error
120         val scrollView = this.scrollView ?: return false
121         val textView = this.targetTextView ?: return false
122 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/AutoScrollManager.kt:136:17
```
Function getTextViewBottomPosition has 3 return statements which exceeds the limit of 2.
```
```kotlin
133     /**
134      * 获取TextView的底部位置
135      */
136     private fun getTextViewBottomPosition(): Int {
!!!                 ^ error
137         val textView = this.targetTextView ?: return 0
138         val scrollView = this.scrollView ?: return 0
139 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/AutoScrollManager.kt:179:17
```
Function calculateTargetScrollY has 3 return statements which exceeds the limit of 2.
```
```kotlin
176     /**
177      * 计算目标滚动位置
178      */
179     private fun calculateTargetScrollY(): Int {
!!!                 ^ error
180         val scrollView = this.scrollView ?: return 0
181         val textView = this.targetTextView ?: return 0
182 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/AutoScrollManager.kt:198:17
```
Function getMaxScrollY has 3 return statements which exceeds the limit of 2.
```
```kotlin
195     /**
196      * 获取最大滚动距离
197      */
198     private fun getMaxScrollY(): Int {
!!!                 ^ error
199         val scrollView = this.scrollView ?: return 0
200         val child = scrollView.getChildAt(0) ?: return 0
201         return (child.height - scrollView.height).coerceAtLeast(0)

```

### style, UnusedParameter (2)

Function parameter is unused and should be removed.

[Documentation](https://detekt.dev/docs/rules/style#unusedparameter)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:275:5
```
Function parameter `content` is unused.
```
```kotlin
272 
273 private suspend fun renderMarkdown(
274     markdownEngine: MarkdownEngine?,
275     content: String,
!!!     ^ error
276     isAsync: Boolean,
277     onStart: () -> Unit,
278     onComplete: () -> Unit,

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:276:5
```
Function parameter `isAsync` is unused.
```
```kotlin
273 private suspend fun renderMarkdown(
274     markdownEngine: MarkdownEngine?,
275     content: String,
276     isAsync: Boolean,
!!!     ^ error
277     onStart: () -> Unit,
278     onComplete: () -> Unit,
279     onError: (String) -> Unit

```

### style, UnusedPrivateProperty (1)

Property is unused and should be removed.

[Documentation](https://detekt.dev/docs/rules/style#unusedprivateproperty)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app-compose/src/main/java/com/chenge/markdown/compose/ui/screen/MarkdownScreen.kt:78:9
```
Private property `progressiveRenderer` is unused.
```
```kotlin
75     var isLoading by remember { mutableStateOf(false) }
76     var errorMessage by remember { mutableStateOf<String?>(null) }
77 
78     val progressiveRenderer = remember { ProgressiveRenderer() }
!!         ^ error
79 
80     LaunchedEffect(Unit) {
81         loadMarkdownContent(context) { content ->

```

generated with [detekt version 1.23.4](https://detekt.dev/) on 2025-08-17 12:03:24 UTC

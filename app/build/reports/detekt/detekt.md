# detekt

## Metrics

* 95 number of properties

* 49 number of functions

* 12 number of classes

* 1 number of packages

* 3 number of kt files

## Complexity Report

* 1,044 lines of code (loc)

* 674 source lines of code (sloc)

* 501 logical lines of code (lloc)

* 229 comment lines of code (cloc)

* 144 cyclomatic complexity (mcc)

* 90 cognitive complexity

* 35 number of total code smells

* 33% comment source ratio

* 287 mcc per 1,000 lloc

* 69 code smells per 1,000 lloc

## Findings (35)

### complexity, TooManyFunctions (3)

Too many functions inside a/an file/class/object/interface always indicate a violation of the single responsibility principle. Maybe the file/class/object/interface wants to manage too many things at once. Extract functionality which clearly belongs together.

[Documentation](https://detekt.dev/docs/rules/complexity#toomanyfunctions)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/AutoScrollManager.kt:15:7
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:35:7
```
Class 'MainActivity' with '18' functions detected. Defined threshold inside classes is set to '11'
```
```kotlin
32  * - 多种渲染模式（同步/异步）
33 
34  */
35 class MainActivity : AppCompatActivity() {
!!       ^ error
36 
37     private lateinit var binding: ActivityMainBinding
38     private lateinit var markdownEngine: MarkdownEngine

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:17:7
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

### exceptions, SwallowedException (2)

The caught exception is swallowed. The original exception could be lost.

[Documentation](https://detekt.dev/docs/rules/exceptions#swallowedexception)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:197:22
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:263:22
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

### exceptions, TooGenericExceptionCaught (4)

The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.

[Documentation](https://detekt.dev/docs/rules/exceptions#toogenericexceptioncaught)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:158:22
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
155 
156                 // 渲染内容
157                 renderMarkdownContent(parsedMarkdown)
158             } catch (e: Exception) {
!!!                      ^ error
159                 Log.e("MainActivity", "加载内容失败", e)
160                 Toast.makeText(
161                     this@MainActivity,

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:437:18
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
434         try {
435             // 这里可以添加资源清理逻辑
436             Log.d("MainActivity", "资源清理完成")
437         } catch (e: Exception) {
!!!                  ^ error
438             Log.e("MainActivity", "资源清理失败", e)
439         }
440     }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:197:22
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:263:22
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

### style, MagicNumber (20)

Report magic numbers. Magic number is a numeric literal that is not defined as a constant and hence it's unclear what the purpose of this number is. It's better to declare such numbers as constants and give them a proper name. By default, -1, 0, 1, and 2 are not considered to be magic numbers.

[Documentation](https://detekt.dev/docs/rules/style#magicnumber)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:42:35
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
39     private var isSyncMode = true // 初始为同步模式
40     private var isDarkMode = false
41     private var isHighContrastMode = false
42     private var currentFontSize = 16f // 默认字体大小
!!                                   ^ error
43     private lateinit var progressiveRenderer: ProgressiveRenderer // 统一的渐进式渲染器
44     private var renderMode = ProgressiveRenderer.RenderMode.PROGRESSIVE // 渲染模式
45     private var renderSpeed = ProgressiveRenderer.RenderSpeed.NORMAL // 渲染速度

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:95:23
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
92             tables()
93             taskLists()
94             latex()
95             imageSize(800, 600)
!!                       ^ error
96             async()
97             debug()
98             plugin("syntax-highlight")

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:95:28
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
92             tables()
93             taskLists()
94             latex()
95             imageSize(800, 600)
!!                            ^ error
96             async()
97             debug()
98             plugin("syntax-highlight")

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:259:32
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
256             }
257 
258             R.id.action_increase_font -> {
259                 adjustFontSize(1.2f)
!!!                                ^ error
260                 true
261             }
262 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:264:32
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
261             }
262 
263             R.id.action_decrease_font -> {
264                 adjustFontSize(0.8f)
!!!                                ^ error
265                 true
266             }
267 

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:306:24
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
303     private fun adjustFontSize(factor: Float) {
304         val newSize = currentFontSize * factor
305 
306         if (newSize >= 10f && newSize <= 36f) {
!!!                        ^ error
307             currentFontSize = newSize
308             binding.contentTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, currentFontSize)
309             // 字体大小已调整

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:306:42
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
303     private fun adjustFontSize(factor: Float) {
304         val newSize = currentFontSize * factor
305 
306         if (newSize >= 10f && newSize <= 36f) {
!!!                                          ^ error
307             currentFontSize = newSize
308             binding.contentTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, currentFontSize)
309             // 字体大小已调整

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:316:27
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
313     }
314 
315     private fun resetFontSize() {
316         currentFontSize = 16f
!!!                           ^ error
317         binding.contentTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, currentFontSize)
318         // 字体大小已重置
319     }

```

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:32:14
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:33:16
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:34:14
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:129:42
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:180:47
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:194:46
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:201:42
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:244:46
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:251:53
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:260:46
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:267:42
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/ProgressiveRenderer.kt:347:34
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

### style, ReturnCount (5)

Restrict the number of return statements in methods.

[Documentation](https://detekt.dev/docs/rules/style#returncount)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/AutoScrollManager.kt:104:25
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/AutoScrollManager.kt:119:17
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/AutoScrollManager.kt:136:17
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/AutoScrollManager.kt:179:17
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

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/AutoScrollManager.kt:198:17
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

### style, UnusedPrivateProperty (1)

Property is unused and should be removed.

[Documentation](https://detekt.dev/docs/rules/style#unusedprivateproperty)

* /Users/quchen/AndroidStudioProjects/markdownsdk/app/src/main/java/com/chenge/markdownsdk/MainActivity.kt:370:13
```
Private property `speedText` is unused.
```
```kotlin
367     private fun setRenderSpeed(speed: ProgressiveRenderer.RenderSpeed) {
368         renderSpeed = speed
369         progressiveRenderer.setRenderSpeed(speed)
370         val speedText = when (speed) {
!!!             ^ error
371             ProgressiveRenderer.RenderSpeed.SLOW -> "慢速"
372             ProgressiveRenderer.RenderSpeed.NORMAL -> "正常"
373             ProgressiveRenderer.RenderSpeed.FAST -> "快速"

```

generated with [detekt version 1.23.4](https://detekt.dev/) on 2025-08-17 11:58:36 UTC

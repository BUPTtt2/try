package com.example.try3.data.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiGeneratedContent(
    val title: String,
    val content: String,
    val suggestedTag: String?
)

class AiHelper {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api-inference.modelscope.cn/v1"
    private val apiKey = "ms-6930d9e1-7f37-47da-8dae-c0fecb94b849"

    suspend fun generateRecordFromKeywords(keywords: String): AiGeneratedContent? = withContext(Dispatchers.IO) {
        try {
            val prompt = """
作为一名擅长文艺写作的生活记录者，根据以下关键词生成一段优美的生活记录。

要求：
1. 文字要文艺清新，充满画面感，像一篇优美的日记片段
2. 内容要自然流畅，避免生硬堆砌关键词
3. 用第一人称"我"来叙述
4. 特别注意：如果关键词中有地点，一定要深入细致地描写该地点的细节，包括：
   - 空间布局和环境氛围
   - 声音、气味、光影等感官细节
   - 当时的天气、时间感
5. 包含细腻的感官描写（视觉、听觉、嗅觉、触觉等）
6. 用一句简短的话概括作为标题（10-20字）
7. 正文内容50-100字
8. 从以下标签中选择一个最合适的：日常, 旅行, 美食, 工作, 学习, 运动, 心情, 其他

50个场景示例供参考：
1. 咖啡馆 - 午后的阳光透过百叶窗，咖啡香混合着甜点的气息
2. 公园长椅 - 秋日的落叶轻轻飘落，脚下是沙沙的声响
3. 海边沙滩 - 咸湿的海风拂过脸颊，远处传来浪花拍打的声音
4. 街角花店 - 清新的花香扑面而来，各色花朵在微风中摇曳
5. 书房窗边 - 台灯的暖光洒在书页上，窗外是寂静的夜
6. 古镇小巷 - 青石板路泛着岁月的光泽，巷子里飘来桂花香
7. 山顶观景台 - 俯瞰整座城市，远处的山峦在云雾中若隐若现
8. 深夜食堂 - 暖黄的灯光下，食物的香气让人心头一暖
9. 樱花树下 - 粉色的花瓣随风飘落，空气中弥漫着春天的味道
10. 城市楼顶 - 华灯初上，城市的夜景如璀璨的星河
11. 森林小径 - 阳光透过树叶洒下斑驳的光影，鸟儿在枝头歌唱
12. 湖边码头 - 湖水泛起涟漪，远处的船只缓缓划过
13. 老图书馆 - 旧书的墨香让人安心，阳光在地板上画出温柔的图案
14. 雨后街道 - 湿润的空气里飘着泥土的清香，倒映着街灯的水洼
15. 艺术画廊 - 光影在画布上流动，安静的空间里只有脚步的轻响
16. 火车站台 - 告别与相聚的故事在这里上演，空气里是离别的不舍
17. 麦田日落 - 金色的麦浪在微风中起伏，天边是温暖的橘色
18. 星空露营 - 抬头是满天的繁星，耳边是篝火燃烧的噼啪声
19. 菜市场 - 热闹的叫卖声，新鲜的蔬果色泽诱人，生活气息扑面而来
20. 旧物店 - 每件物品都带着岁月的故事，时光在这里放慢了脚步
21. 老茶馆 - 盖碗茶的热气升腾，竹椅的吱呀声，岁月在茶香里沉淀
22. 地铁站台 - 人来人往的匆忙，广播的声音，城市的脉搏在跳动
23. 露天电影院 - 晚风轻拂，银幕上的故事，周围是爆米花的香气
24. 面包店 - 刚出炉的面包香，暖黄的灯光，幸福的味道在空气中弥漫
25. 山顶寺庙 - 晨钟暮鼓，香火缭绕，心在这里慢慢静下来
26. 花店 - 各色鲜花在晨光中绽放，空气里是清新的花香
27. 旧仓库改造的工作室 - 工业风的空间，创意在这里碰撞，阳光从天窗洒下
28. 骑楼老街 - 骑楼的阴凉，怀旧的商铺，时光在这里慢下来
29. 星空下的屋顶花园 - 月光如水，晚风轻拂，城市的灯火在脚下闪烁
30. 森林中的小木屋 - 壁炉的温暖，窗外是雪落的声音，宁静而美好
31. 清晨的菜市场 - 新鲜的蔬果，热闹的叫卖，生活的烟火气扑面而来
32. 古镇石桥上 - 流水潺潺，两岸是白墙黛瓦，时间仿佛静止
33. 海边堤坝 - 海浪拍打着礁石，咸湿的海风，远处是归航的渔船
34. 深夜的便利店 - 暖黄的灯光，24小时的陪伴，夜归人的港湾
35. 茶园 - 层层叠叠的茶树，采茶人的歌声，茶香满山
36. 废弃的火车站 - 斑驳的铁轨，荒芜的站台，往日的喧嚣已成记忆
37. 旋转木马上 - 彩色的灯光，旋转的木马，童年的快乐在心头荡漾
38. 山顶悬崖边 - 脚下是万丈深渊，眼前是壮阔云海，风在耳边呼啸
39. 老巷子里的理发店 - 剪刀的咔嚓声，老式的座椅，时光在这里慢下来
40. 海边露营地 - 篝火燃烧，星空璀璨，海浪声是最好的催眠曲
41. 城市高架桥边 - 车流不息，城市的灯火在暮色中亮起，繁华而孤独
42. 古镇的夜市 - 小吃摊的香气，人流如织，暖黄的灯笼照亮夜色
43. 图书馆的角落 - 阳光洒在书架上，安静得能听到翻书声，心在这里沉淀
44. 花田 - 五彩斑斓的花海，蝴蝶在花间飞舞，空气中是花香的甜蜜
45. 老照相馆 - 复古的布景，泛黄的照片，时光在这里定格
46. 沙漠绿洲 - 椰林婆娑，泉水叮咚，在荒芜中遇见生命的奇迹
47. 城市地铁里 - 拥挤的人群，各自的心事，这是城市最真实的一面
48. 山顶的观景台 - 俯瞰整座城市，风在耳边呼啸，心胸在这一刻开阔
49. 古镇的油纸伞店 - 色彩斑斓的油纸伞，古意盎然，仿佛穿越时光
50. 海边的书店 - 面朝大海，书香与海风交融，这是最美好的时光

关键词：$keywords

请严格按以下JSON格式返回，不要有其他文字：
{
    "title": "这里是标题",
    "content": "这里是正文内容",
    "tag": "这里是选择的标签"
}
            """.trimIndent()

            Log.d("AiHelper", "开始请求AI生成，关键词：$keywords")
            
            val response = chatRequest(prompt)
            Log.d("AiHelper", "AI原始响应：$response")
            
            val result = parseGenerateResponse(response)
            if (result != null) {
                Log.d("AiHelper", "AI解析成功：${result.title}")
                result
            } else {
                Log.d("AiHelper", "AI解析失败，使用本地备用方案")
                generateLocal(keywords)
            }
        } catch (e: Exception) {
            Log.e("AiHelper", "AI请求异常", e)
            generateLocal(keywords)
        }
    }

    suspend fun suggestTags(content: String): List<String> = withContext(Dispatchers.IO) {
        listOf("日常")
    }

    private fun generateLocal(keywords: String): AiGeneratedContent {
        val tagMap = mapOf(
            "旅行" to listOf("旅行", "旅途", "旅程", "海边", "山顶", "远方", "风景", "景点"),
            "美食" to listOf("美食", "好吃", "美味", "餐厅", "咖啡", "甜点", "蛋糕", "奶茶", "吃饭"),
            "工作" to listOf("工作", "加班", "办公室", "会议", "项目"),
            "学习" to listOf("学习", "读书", "考试", "课堂", "自习"),
            "运动" to listOf("运动", "跑步", "健身", "瑜伽", "打球"),
            "心情" to listOf("开心", "难过", "感动", "幸福", "快乐", "失落")
        )
        
        var selectedTag = "日常"
        for ((tag, keywordsList) in tagMap) {
            if (keywordsList.any { keywords.contains(it, ignoreCase = true) }) {
                selectedTag = tag
                break
            }
        }

        val localTitles = listOf(
            "$keywords 的美好时光",
            "记录此刻：$keywords",
            "与 $keywords 有关的一天",
            "$keywords 时刻",
            "今日：$keywords"
        )

        val localContents = listOf(
            "今天遇到了 $keywords，感觉很美好。时光静静流淌，愿这份美好常驻心间。",
            "关于 $keywords 的一天，心里满满的都是幸福感。简单的日子，也有不一样的味道。",
            "此刻，我在感受着 $keywords 带来的一切。生活的每一个瞬间，都值得被记录。",
            "今天和 $keywords 有关，一切都是那么刚刚好。感恩生活中的小确幸。"
        )

        return AiGeneratedContent(
            title = localTitles.random(),
            content = localContents.random(),
            suggestedTag = selectedTag
        )
    }

    private suspend fun chatRequest(prompt: String): String = withContext(Dispatchers.IO) {
        val jsonBody = JSONObject().apply {
            put("model", "Qwen/Qwen3.5-27B")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 500)
            put("temperature", 0.85)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (response.isSuccessful) {
            try {
                val json = JSONObject(body)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    choices.getJSONObject(0).optJSONObject("message")?.optString("content") ?: ""
                } else {
                    ""
                }
            } catch (e: Exception) {
                Log.e("AiHelper", "JSON解析响应异常", e)
                ""
            }
        } else {
            Log.e("AiHelper", "HTTP请求失败：${response.code}，响应：$body")
            ""
        }
    }

    private fun parseGenerateResponse(response: String): AiGeneratedContent? {
        if (response.isBlank()) return null

        return try {
            val jsonStr = response.trim().let { str ->
                val jsonStart = str.indexOf('{')
                val jsonEnd = str.lastIndexOf('}')
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    str.substring(jsonStart, jsonEnd + 1)
                } else {
                    str
                }
            }

            val json = JSONObject(jsonStr)
            val title = json.getString("title")
            val content = json.getString("content")
            val tag = json.optString("tag").takeIf { 
                it.isNotBlank() && it in listOf("日常", "旅行", "美食", "工作", "学习", "运动", "心情", "其他") 
            }

            AiGeneratedContent(title, content, tag)
        } catch (e: Exception) {
            Log.e("AiHelper", "解析响应失败", e)
            null
        }
    }
}

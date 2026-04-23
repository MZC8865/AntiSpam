# AntiSpam 插件修复完成

## 问题原因

你的Paper 1.21.11插件无效的根本原因：

1. **配置文件冲突** - 项目中同时存在两个主类：
   - `org.mao.antiSpam.AntiSpam` (空实现)
   - `com.yourname.antispam.AntiSpamPlugin` (完整实现)

2. **主类指向错误** - `paper-plugin.yml` 指向了空的主类

3. **命令注册方式错误** - Paper插件不支持在YAML中定义命令

## 已修复的问题

✅ 删除了空的 `org.mao.antiSpam.AntiSpam` 类
✅ 修正了 `paper-plugin.yml` 中的主类路径
✅ 使用 `CommandMap` 直接注册命令（Paper插件的正确方式）
✅ 保留了完整的反垃圾消息功能

## 插件功能

### 1. 快速发送检测
- 阻止玩家在配置的时间间隔内（默认20ms）连续发送消息
- 只要在冷却时间内发送就会被拦截
- 提示：`请勿刷屏`

### 2. 消息相似度检测
- 使用 Levenshtein 距离算法检测消息相似度
- 如果新消息与上一条消息相似度超过阈值（默认80%），则拦截
- 可配置相似度阈值和最小检测长度
- 防止玩家通过轻微修改消息来绕过限制
- 提示：`消息过于相似`

### 3. 违规词过滤 ⭐ 新功能
- 自动检测并拦截包含违规词的消息
- 支持中文和英文，不区分大小写
- 可配置违规词列表
- 优先级最高（最先检测）
- 提示：`有违规文字`

### 4. 可配置
- 通过命令和配置文件调整所有参数
- 支持热重载配置
- 命令修改会自动保存到配置文件

## 使用方法

### 命令（新格式：/chat antispam）
- `/chat antispam delay <毫秒数>` - 设置消息间隔延迟
- `/chat antispam similarity <0.0-1.0>` - 设置相似度阈值
- `/chat antispam toggle similarity` - 开关相似度检测
- `/chat antispam toggle profanity` - 开关违规词过滤
- `/chat antispam status` - 查看当前配置
- `/chat antispam reload` - 重新加载配置

### Tab 补全支持
- `/chat` + Tab → 补全 `antispam`
- `/chat antispam` + Tab → 显示所有子命令
- `/chat antispam delay` + Tab → 显示建议值
- `/chat antispam toggle` + Tab → 显示 `similarity` 和 `profanity`

### 配置文件 (config.yml)
```yaml
anti-spam:
  delay-ms: 20                    # 消息间隔延迟（毫秒）
  
  # 相似度检测
  similarity-check: true          # 是否启用相似度检测
  similarity-threshold: 0.8       # 相似度阈值（0.0-1.0）
  min-length-for-check: 3         # 最小检测长度
  
  # 违规词过滤
  profanity-filter: true          # 是否启用违规词过滤
  blocked-words:                  # 违规词列表
    - "傻逼"
    - "fuck"
    - "shit"
    - "草泥马"
    - "sb"
    - "nmsl"
    # 添加更多...
```

## 检测优先级

消息检测按以下顺序执行：

1. **违规词检测** ← 最高优先级
2. **时间间隔检测**
3. **相似度检测**

## 功能示例

### 违规词过滤
- ✅ "你是个傻逼" → 拦截，显示"有违规文字"
- ✅ "FUCK you" → 拦截（不区分大小写）
- ❌ "你好" → 放行

### 相似度检测
- ✅ "你好" → "你好" （100%相似，拦截）
- ✅ "今天天气真好" → "今天天气真棒" （相似度约85%，拦截）
- ❌ "你好" → "再见" （低相似度，放行）

### 时间间隔检测
- ✅ 快速连续发送两条消息 → 第二条被拦截

## 测试插件

1. 构建插件：
   ```bash
   ./gradlew build
   ```

2. 插件文件位置：`build/libs/AntiSpam-3.0.jar`

3. 将jar文件复制到Paper服务器的 `plugins` 目录

4. 重启服务器，查看日志确认加载成功：
   ```
   [AntiSpam] AntiSpam enabled
   [AntiSpam] AntiSpam config loaded: delayMs=20, similarityCheck=true, threshold=0.8, minLength=3, profanityFilter=true, blockedWords=6
   [AntiSpam] AntiSpam: command '/chat' registered with tab completion
   ```

## 注意事项

- 需要Java 21
- 适用于Paper 1.21+
- 权限节点：`antispam.admin` (默认OP)
- 相似度检测对中文和英文都有效
- 违规词检测不区分大小写
- 所有命令修改都会自动保存到配置文件

## 相关文档

- [测试指南](TEST_GUIDE.md) - 详细的功能测试说明
- [违规词过滤说明](PROFANITY_FILTER.md) - 违规词功能详细文档

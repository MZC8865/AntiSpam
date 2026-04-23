# AntiSpam - Minecraft 聊天反垃圾插件

一个功能强大、高度可配置的 Minecraft Paper/Spigot 聊天反垃圾插件，提供全方位的聊天管理和保护功能。

## ✨ 主要功能

### 🚫 多层次反垃圾检测

- **时间间隔检测** - 防止玩家快速刷屏（默认20ms间隔）
- **消息相似度检测** - 使用 Levenshtein 算法检测重复或相似消息（默认80%相似度）
- **违规词过滤** - 可自定义的违规词黑名单，支持中英文
- **私信检测** - 可选择是否对私信进行反垃圾检测

### 👮 管理功能

- **禁言系统** - 临时禁止玩家发言，支持多种时间格式
- **实时配置** - 所有设置可通过命令实时调整并自动保存
- **数据持久化** - 禁言信息保存到文件，重启后依然有效

### 🎯 用户体验

- **智能提示** - 不同的拦截原因显示不同的提示信息
- **Tab 补全** - 所有命令支持 Tab 自动补全
- **详细日志** - 完整的操作日志记录

## 📋 系统要求

- **Minecraft 版本**: 1.21+
- **服务端**: Paper 1.21+ 或 Spigot 1.21+
- **Java 版本**: Java 21+

## 📦 安装

1. 下载 `AntiSpam-3.0.jar`
2. 将文件放入服务器的 `plugins` 目录
3. 重启服务器
4. 插件会自动生成配置文件

## 🎮 命令使用

### 反垃圾设置命令

```bash
# 查看当前配置
/chat antispam status

# 设置消息间隔延迟（毫秒）
/chat antispam delay <毫秒数>
例如: /chat antispam delay 50

# 设置相似度阈值（0.0-1.0）
/chat antispam similarity <阈值>
例如: /chat antispam similarity 0.8

# 开关相似度检测
/chat antispam toggle similarity

# 开关违规词过滤
/chat antispam toggle profanity

# 重新加载配置
/chat antispam reload
```

### 私信检测命令

```bash
# 启用私信检测
/chat whispercheck on

# 禁用私信检测
/chat whispercheck off

# 查看私信检测状态
/chat whispercheck
```

### 禁言管理命令

```bash
# 禁言玩家
/chat ban <玩家名> <时间>
例如: /chat ban Player 5m    # 禁言5分钟
例如: /chat ban Player 1h    # 禁言1小时
例如: /chat ban Player 1d    # 禁言1天

# 解除禁言
/chat unban <玩家名>
例如: /chat unban Player
```

**时间格式**:
- `s` 或 `秒` = 秒
- `m` 或 `分` = 分钟
- `h` 或 `时` = 小时
- `d` 或 `天` = 天

## ⚙️ 配置文件

配置文件位置: `plugins/AntiSpam/config.yml`

```yaml
anti-spam:
  # 消息间隔延迟（毫秒）
  delay-ms: 20
  
  # 相似度检测配置
  similarity-check: true          # 是否启用相似度检测
  similarity-threshold: 0.8       # 相似度阈值（0.0-1.0）
  min-length-for-check: 3         # 最小检测长度
  
  # 违规词过滤配置
  profanity-filter: true          # 是否启用违规词过滤
  blocked-words:                  # 违规词列表
    - "傻逼"
    - "fuck"
    - "shit"
    - "草泥马"
    - "sb"
    - "nmsl"
    # 添加更多违规词...
  
  # 私信检测配置
  whisper-check: true             # 是否检测私信
```

## 🔍 检测优先级

消息检测按以下顺序执行：

1. **禁言检查** ← 最高优先级
2. **违规词检测**
3. **时间间隔检测**
4. **相似度检测**

## 💡 使用示例

### 场景 1：防止快速刷屏

```bash
# 设置消息间隔为100ms
/chat antispam delay 100
```

玩家快速发送消息时会被拦截，显示：`请勿刷屏`

### 场景 2：防止重复消息

```bash
# 设置相似度阈值为90%
/chat antispam similarity 0.9
```

玩家发送相似消息时会被拦截，显示：`消息过于相似`

### 场景 3：过滤违规词

编辑 `config.yml` 添加违规词：

```yaml
blocked-words:
  - "你的违规词1"
  - "你的违规词2"
```

然后重新加载：

```bash
/chat antispam reload
```

玩家发送包含违规词的消息时会被拦截，显示：`有违规文字：你的违规词1`（会显示具体检测到的违规词）

### 场景 4：临时禁言违规玩家

```bash
# 禁言玩家5分钟
/chat ban BadPlayer 5m
```

被禁言的玩家尝试发言时会看到：`你已被禁言，剩余时间：4分钟 30秒`

### 场景 5：管理私信

```bash
# 禁用私信检测（允许玩家自由私聊）
/chat whispercheck off

# 启用私信检测（严格管理）
/chat whispercheck on
```

## 🎨 提示消息

不同的拦截原因会显示不同的提示：

| 拦截原因 | 提示消息 | 示例 |
|---------|---------|------|
| 被禁言 | `你已被禁言，剩余时间：X` | `你已被禁言，剩余时间：4分钟 30秒` |
| 违规词 | `有违规文字：<违规词>` | `有违规文字：傻逼` |
| 发送过快 | `请勿刷屏` | `请勿刷屏` |
| 消息相似 | `消息过于相似` | `消息过于相似` |

## 🔐 权限

| 权限节点 | 说明 | 默认 |
|---------|------|------|
| `antispam.admin` | 使用所有管理命令 | OP |

## 📊 支持的私信命令

插件会检测以下私信命令：

- `/w <玩家> <消息>`
- `/msg <玩家> <消息>`
- `/tell <玩家> <消息>`
- `/whisper <玩家> <消息>`
- `/m <玩家> <消息>`
- `/t <玩家> <消息>`
- `/pm <玩家> <消息>`

## 📁 文件结构

```
plugins/AntiSpam/
├── config.yml      # 主配置文件
└── mutes.yml       # 禁言数据文件（自动生成）
```

## 🚀 性能

- **轻量级设计** - 对服务器性能影响极小
- **异步处理** - 聊天检测在异步线程中进行
- **高效算法** - 使用优化的字符串匹配算法
- **内存友好** - 使用 ConcurrentHashMap 管理玩家数据

## 📝 日志示例

```
[AntiSpam] AntiSpam enabled
[AntiSpam] Registered chat and whisper listeners
[AntiSpam] Mute manager initialized
[AntiSpam] AntiSpam config loaded: delayMs=20, similarityCheck=true, threshold=0.8, minLength=3, profanityFilter=true, blockedWords=6, whisperCheck=true
[AntiSpam] AntiSpam: command '/chat' registered with tab completion
[AntiSpam] Blocked chat from Player (profanity detected: 违规词)
[AntiSpam] Blocked whisper from Player (similar message, similarity=100.00%)
[AntiSpam] Muted player BadPlayer for 300 seconds
```

## 🔧 常见问题

### Q: 如何添加自定义违规词？

A: 编辑 `plugins/AntiSpam/config.yml`，在 `blocked-words` 列表中添加，然后执行 `/chat antispam reload`

### Q: 如何调整检测严格程度？

A: 
- 更严格：降低相似度阈值 `/chat antispam similarity 0.5`
- 更宽松：提高相似度阈值 `/chat antispam similarity 0.95`

### Q: 私信检测默认是启用还是禁用？

A: 默认启用。可以使用 `/chat whispercheck off` 禁用。

### Q: 禁言数据会丢失吗？

A: 不会。禁言数据保存在 `mutes.yml` 文件中，重启服务器后依然有效。

### Q: 如何只检测公共聊天，不检测私信？

A: 使用命令 `/chat whispercheck off`

### Q: 短消息（如"ok"）重复发送为什么不被拦截？

A: 默认配置中，消息长度小于3个字符不进行相似度检测。可以在配置文件中修改 `min-length-for-check`。

## 📚 详细文档

- [违规词过滤功能](PROFANITY_FILTER.md) - 违规词功能详细说明
- [私信检测功能](私信检测功能说明.md) - 私信检测功能详细说明
- [禁言功能](禁言功能说明.md) - 禁言系统详细说明
- [测试指南](TEST_GUIDE.md) - 功能测试说明
- [如何添加违规词](如何添加违规词.md) - 违规词配置教程

## 🎯 最佳实践

### 1. 推荐配置（公共服务器）

```yaml
anti-spam:
  delay-ms: 3000                   # 3000ms间隔
  similarity-check: true
  similarity-threshold: 0.8       # 80%相似度
  profanity-filter: true
  whisper-check: true             # 检测私信
```

### 2. 推荐配置（私人服务器）

```yaml
anti-spam:
  delay-ms: 1000                    # 1000ms间隔
  similarity-check: true
  similarity-threshold: 0.9       # 90%相似度（更宽松）
  profanity-filter: true
  whisper-check: false            # 不检测私信
```

## 🛠️ 开发信息

- **版本**: 3.0
- **API 版本**: 1.21
- **构建工具**: Gradle
- **语言**: Java 21

## 📄 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📮 支持

如果遇到问题或有建议，请：
1. 查看详细文档
2. 检查配置文件
3. 查看服务器日志
4. 提交 Issue

## 📈 更新日志

### v3.0 (当前版本)
- ✨ 新增禁言系统
- ✨ 新增私信检测功能
- ✨ 新增违规词过滤
- ✨ 新增消息相似度检测
- ✨ 新增 Tab 补全支持
- ✨ 新增配置持久化
- 🐛 修复多个已知问题
- ⚡ 性能优化

---

**让你的服务器聊天环境更健康！** 🎉

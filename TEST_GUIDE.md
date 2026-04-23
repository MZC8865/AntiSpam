# AntiSpam 插件测试指南

## 功能测试场景

### 1. 时间间隔检测测试

**测试步骤：**
1. 进入游戏
2. 快速连续发送两条消息（间隔小于20ms）
3. 预期结果：第二条消息被拦截，显示"请勿刷屏"

**命令调整：**
```
/chat antispam delay 1000    # 设置为1秒间隔，更容易测试
```

### 2. 相似度检测测试

**测试场景A：完全相同的消息**
1. 发送消息："你好"
2. 等待超过20ms
3. 再次发送："你好"
4. 预期结果：第二条消息被拦截，显示"消息过于相似"（100%相似）

**测试场景B：高度相似的消息**
1. 发送消息："今天天气真好"
2. 等待超过20ms
3. 发送消息："今天天气真棒"
4. 预期结果：第二条消息被拦截，显示"消息过于相似"（相似度约85%）

**测试场景C：轻微相似的消息**
1. 发送消息："你好啊朋友"
2. 等待超过20ms
3. 发送消息："你好呀伙伴"
4. 预期结果：根据阈值决定（默认0.8会拦截）

**测试场景D：完全不同的消息**
1. 发送消息："你好"
2. 等待超过20ms
3. 发送消息："再见"
4. 预期结果：放行（相似度低）

**测试场景E：短消息**
1. 发送消息："ok"
2. 等待超过20ms
3. 再次发送："ok"
4. 预期结果：放行（消息太短，不检测相似度，但仍检测时间间隔）

### 3. 配置命令测试

**查看状态：**
```
/chat antispam status
```
预期输出：
```
AntiSpam Status:
- delayMs: 20 ms
- similarityCheck: 启用
- similarityThreshold: 80.00%
- minLengthForCheck: 3
```

**Tab 补全测试：**
1. 输入 `/chat` 然后按 Tab → 应该补全为 `/chat antispam`
2. 输入 `/chat antispam` 然后按 Tab → 显示所有子命令（delay, similarity, toggle, status, reload）
3. 输入 `/chat antispam delay` 然后按 Tab → 显示建议值（20, 50, 100, 500, 1000）
4. 输入 `/chat antispam similarity` 然后按 Tab → 显示建议值（0.5, 0.6, 0.7, 0.8, 0.9, 1.0）
5. 输入 `/chat antispam toggle` 然后按 Tab → 补全为 `similarity`

**调整相似度阈值：**
```
/chat antispam similarity 0.9    # 设置为90%相似才拦截（配置会保存）
/chat antispam similarity 0.5    # 设置为50%相似就拦截（更严格，配置会保存）
```

**开关相似度检测：**
```
/chat antispam toggle similarity    # 禁用相似度检测（配置会保存）
/chat antispam toggle similarity    # 重新启用（配置会保存）
```

**重新加载配置：**
```
/chat antispam reload
```

### 4. 配置文件持久化测试

**测试步骤：**
1. 使用命令修改配置：
   ```
   /chat antispam delay 500
   /chat antispam similarity 0.9
   ```
2. 查看 `plugins/AntiSpam/config.yml` 文件，确认值已更新
3. 重启服务器
4. 使用 `/chat antispam status` 确认配置保持不变

**手动修改配置文件：**
修改 `plugins/AntiSpam/config.yml`：
```yaml
anti-spam:
  delay-ms: 1000              # 改为1秒
  similarity-check: false     # 禁用相似度检测
  similarity-threshold: 0.9   # 改为90%
  min-length-for-check: 5     # 改为5个字符
```

然后执行：
```
/chat antispam reload
```

验证配置是否生效：
```
/chat antispam status
```

## 调试模式

启动服务器时添加JVM参数以启用调试日志：
```bash
java -Dantispam.debug=true -jar paper.jar
```

调试模式会在控制台输出：
- 每条消息的时间间隔
- 每条消息的相似度百分比
- 拦截原因的详细信息

## 预期日志输出

**正常加载：**
```
[AntiSpam] AntiSpam enabled
[AntiSpam] AntiSpam config loaded: delayMs=20, similarityCheck=true, threshold=0.8, minLength=3
[AntiSpam] AntiSpam: command '/chat' registered with tab completion
```

**拦截快速发送：**
```
[AntiSpam] Blocked chat from PlayerName (sent too fast, gap=15ms)
```

**拦截相似消息：**
```
[AntiSpam] Blocked chat from PlayerName (similar message, similarity=85.00%)
```

**保存配置：**
```
[AntiSpam] Saved config: anti-spam.delay-ms = 500
```

## 常见问题

**Q: 为什么短消息"ok"重复发送不被拦截？**
A: 默认配置中，消息长度小于3个字符不进行相似度检测。可以通过修改 `min-length-for-check` 来调整。

**Q: 如何让检测更严格？**
A: 降低 `similarity-threshold` 值，例如 `/chat antispam similarity 0.5`（50%相似就拦截）

**Q: 如何让检测更宽松？**
A: 提高 `similarity-threshold` 值，例如 `/chat antispam similarity 0.95`（95%相似才拦截）

**Q: 如何只使用时间间隔检测，不使用相似度检测？**
A: 使用命令 `/chat antispam toggle similarity` 禁用相似度检测

**Q: 配置会自动保存吗？**
A: 是的！使用命令修改的所有配置都会自动保存到 `config.yml` 文件中，重启后依然有效。

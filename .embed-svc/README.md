# Local Embedding Service

为 gbrain 提供本地 embedding 能力的 OpenAI 兼容服务。

## 启动

```bash
cd .embed-svc && npm start
```

服务运行在 `http://localhost:11434`，支持 `text-embedding-3-large` 模型。

## 与 gbrain 集成

已在 `opencode.jsonc` 中配置 `OPENAI_BASE_URL=http://localhost:11434/v1`，
启动 embedding 服务后，gbrain 会自动使用它生成 embedding。

## 生成 embedding

```bash
gbrain embed --stale
```

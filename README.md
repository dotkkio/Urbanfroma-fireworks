# Urbanforma: Fireworks

Urbanforma: Fireworks 是 Urbanforma 系列的烟花扩展模组，为 Minecraft
提供球形烟花、垂柳烟花、双重球体烟花和大型烟花效果。

## 当前公开版本

- Minecraft：`1.21.1`
- NeoForge：`21.1.234` 或兼容的更高版本
- Java：`21`
- Mod ID：`urbanforma_fireworks`
- Mod 版本：`0.3.1`
- 许可证：MIT

本仓库的 `main` 分支对应已发布的 `0.3.1` 公开基线。

## 依赖

本模组需要同时安装 Urbanforma:neo 主模组：

- 依赖 Mod ID：`urbanforma_neo`
- 兼容版本范围：`0.21.x`

请确保客户端或服务器中的 Urbanforma:neo 与 Minecraft、NeoForge 版本相匹配。

## 安装

1. 安装 Minecraft `1.21.1`。
2. 安装 NeoForge `21.1.234` 或兼容的更高版本。
3. 使用 Java `21` 启动游戏或服务器。
4. 将 Urbanforma:neo 和 Urbanforma:Fireworks 的对应 JAR 文件放入同一个 `mods` 文件夹。
5. 启动游戏或服务器，并在日志中确认两个模组都已加载。

客户端和服务器应使用相同的 Minecraft、NeoForge、Urbanforma:neo 和
Urbanforma: Fireworks 版本。

## 从源码构建

本仓库当前没有包含 Gradle Wrapper，因此需要本地安装兼容的 Gradle，并使用
Java `21`。

Fireworks 的构建脚本还需要 Urbanforma:neo 的开发 JAR。默认路径是：

```text
../Urbanforma-neo/build/libs/urbanforma_neo-0.21.jar
```

请先在同级目录准备匹配版本的 Urbanforma:neo 构建产物，然后在本仓库根目录执行：

```powershell
gradle build
```

构建产物会生成在：

```text
build/libs/
```

## 项目状态

当前 `main` 分支是面向公开使用的 `0.3.1` 基线。运行时效果、客户端画面、
服务器行为和多人联机兼容性应在目标环境中分别验证；源码发布不等同于这些运行时门禁均已通过。

## 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。

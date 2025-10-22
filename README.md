# AE2 Pattern Encoding Access Terminal

[日本語版READMEはこちら](README.ja.md)

AE2 Pattern Encoding Access Terminalは、Applied Energistics 2のパターン作成体験を強化するForge向けアドオンです。パターンアクセス端末の閲覧機能とエンコード端末の編集機能を1つのMEパーツに統合し、ネットワーク上のパターンをその場で検索・管理・編集できるようにします。

## Features
- Adds an ME Pattern Encoding Access Terminal part that combines AE2’s pattern access browsing with full encoding tools.
- Supports all AE2 encoding modes (crafting, processing, smithing table, stonecutting) with the familiar terminal UI.
- Keeps pattern provider groups, search, and sorting in sync via dedicated client mixins for responsive updates.
- Respects AE2 terminal style settings and exposes the usual server-configurable toggles.
- Designed to work alongside common AE2 companion mods such as GuideMe, JEI, EMI, and Jade.

## Requirements
| Component | Version |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.0+ |
| Java | 17 |
| Applied Energistics 2 | 15.4.9+ |

Optional but recommended at runtime: GuideMe 20.1.11+, JEI 15.20.0.112+, EMI 1.1.22+, Jade (Curse ID 324717:6855440).

## Installation
1. Install Forge for Minecraft 1.20.1.
2. Download the latest release of AE2 Pattern Encoding Access Terminal and place the JAR in your `mods/` folder.
3. Install the required dependencies (Applied Energistics 2) and any optional companion mods you prefer.
4. Launch the game; the new terminal part appears in the AE2 creative tab and can be crafted once its recipe is added.

## Getting Started
- Craft and place the Pattern Encoding Access Terminal part on an ME cable or smart cable.
- Right-click the part while connected to your ME network to open the combined access/encoding interface.
- Use the mode tabs to switch between encoding modes and the search field to filter stored patterns.
- Toggle pattern provider visibility or terminal styles from the built-in settings, mirroring vanilla AE2 behaviour.

## Building from Source
1. Install JDK 17 and ensure `JAVA_HOME` points to it.
2. Clone the repository and import it as a Gradle project; the ModDev Gradle setup ships with ready-to-use run configurations.
3. Use `./gradlew runClient` for a quick in-game test environment.
4. Use `./gradlew build` to produce a distributable JAR under `build/libs/`.
5. Data generators are available via `./gradlew runData`, which outputs resources into `src/generated/`.

### IntelliJ IDEA Tips
- Import the project as a Gradle build; ModDev will generate client/server/data run configurations automatically.
- Enable annotation processing to satisfy mixin-generated sources.

## Contributing
Bug reports and feature requests are welcome on the [GitHub issue tracker](https://github.com/yuuki1293/AE2PatternEncodingAccessTerminal/issues). Pull requests should target the `main` branch and include:
- A concise description of the change.
- Testing notes (e.g., `runClient` launch or added unit tests).
- Updates to the changelog when applicable.

## License
The project license will be published in this repository. Until then, please refrain from redistributing modified builds.

## Japanese Summary / 日本語概要
- Applied Energistics 2のパターンアクセス端末とエンコード端末を統合した新しいMEパーツを追加します。
- エンコードモード（クラフト、加工、鍛冶、石切）をタブで切り替えでき、ネットワーク上のパターンを即座に編集可能です。
- Modの導入にはMinecraft 1.20.1、Forge 47.4.0以降、Applied Energistics 2 15.4.9以降が必要です。
- ビルドや開発にはJDK 17とGradleを使用します。

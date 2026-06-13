# PlayerBuff23 (net.azisaba.playerbuff23) 仕様書

## 概要

- **パッケージ**: `net.azisaba.playerbuff23`
- **メインクラス**: `PlayerBuff23` (`org.bukkit.plugin.java.JavaPlugin` を継承)
- **プラグイン名**: `PlayerBuff23`
- **APIバージョン**: Paper 1.15.2
- **外部依存**: MythicMobs (4.12.0)

## アーキテクチャ

- **Constructor Injection パターン**: 各リスナー・コマンドクラスはコンストラクタで `PlayerBuff23` インスタンスを受け取る。
- **Stateless 設計**: コマンドクラスは状態を持たず、毎回引数から処理。
- **高度な Duration 管理**: `BukkitTask` + `HashMap` による複数エンティティ・複数 BuffType の同時管理。

## クラス一覧

| クラス | 役割 |
|--------|------|
| `PlayerBuff23` | メインクラス |
| `command/PlayerBuffCommand` | `/playerbuff` コマンド |
| `command/PlayerBuffSetCommand` | `/playerbuffset` コマンド + TabCompleter |
| `listener/PlayerBuffDuration` | 高度な効果時間管理 (BuffType/DurationMode) |
| `listener/PlayerBuffTest` | ワールド限定防具判定 (HW2022 + HW2023) |
| `listener/WorldDreamTest` | Dream ワールド限定 mainhand 武器で攻撃力バフ |
| `listener/WorldDreamSystem` | Dream ワールド限定 Health 3倍 + Speed 半減 |
| `PlayerBuffSetHealth` | MaxHealth 属性操作 |
| `PlayerBuffSetDamage` | AttackDamage 属性操作 |
| `PlayerBuffSetArmor` | Armor 属性操作 |
| `PlayerBuffSetArmorToughness` | ArmorToughness 属性操作 |
| `PlayerBuffSetSpeed` | MovementSpeed 属性操作 |

## メインクラス: PlayerBuff23

### フィールド

- `private static PlayerBuff23 instance` — static singleton（`onEnable` で設定）

### onEnable()

1. `instance = this`
2. `saveDefaultConfig()`
3. `/playerbuff` → `PlayerBuffCommand` (executor)
4. `/playerbuffset` → `PlayerBuffSetCommand` (executor + tabCompleter)
5. リスナー登録:
   - `PlayerBuffTest`
   - `PlayerBuffDuration`
   - `WorldDreamTest`
   - `WorldDreamSystem`
6. 有効化ログ: `"PlayerBuff23 has been enabled."`

### onDisable()

- 無効化ログ: `"PlayerBuff23 has been disabled."`

### getInstance()

- static singleton getter

## コマンド

### `/playerbuff`

| 項目 | 内容 |
|------|------|
| **Executor** | `PlayerBuffCommand` |
| **パーミッション** | `playerbuff.command.main` |
| **引数** | 0個 または `reload` |
| **動作** | `args[0]` が `reload` の場合 `plugin.reloadConfig()` + メッセージ |
| **メッセージ** | `"Usage: /playerbuff <subcommand>"` / `"Configuration reloaded."` (英語) |
| **戻り値** | `false` (usage をサーバー側で表示させる) |

### `/playerbuffset` (aliases: `pbset`, `pbs`)

| 項目 | 内容 |
|------|------|
| **Executor** | `PlayerBuffSetCommand` (`CommandExecutor` + `TabCompleter`) |
| **パーミッション** | `playerbuff.command.pbs` |
| **構文** | `/playerbuffset <playerName> <buffType> <level> <duration> [mode]` |
| **buffType** | `Health`, `Damage`, `Armor`, `Toughness`, `Speed` (大文字小文字不問) |
| **level** | double |
| **duration** | long（秒） |
| **mode** | `add`（残り時間に加算、デフォルト）/ `replace`（今回の秒数に上書き） |

**TabComplete**:
- 1引数目: オンラインプレイヤー名を補完
- 2引数目: BuffType (`Health`, `Damage`, `Armor`, `Toughness`, `Speed`) を補完
- 5引数目: mode (`add`, `replace`) を補完

**動作**:
1. 権限チェック (`playerbuff.command.pbs`)
2. 引数が4個未満なら usage を表示
3. `level` / `duration` を `Double.parseDouble` / `Long.parseLong` でパース
4. 対象プレイヤーがオフラインなら日本語メッセージ
5. `BuffType.fromString()` で型解決、不正値なら日本語エラー
6. mode 指定がなければ `REPLACE` デフォルト
7. `PlayerBuffDuration.applyTimedBuff()` を呼び出す

**usage メッセージ**:
```
&a/pbs <playerName> <buffType> <level> <duration> [mode]
&6buffType: &fHealth, Damage, Armor, Toughness, Speed のいずれかを指定してください。
&6duration: &f効果時間(秒)を指定してください。
&6mode: &fadd で残り時間に加算、replace で今回の秒数に上書きします。省略時は add です。
```

## パーミッション

| ノード | 子ノード | デフォルト |
|--------|---------|-----------|
| `playerbuff.command.*` | `playerbuff.command.main`, `playerbuff.command.set` | op |
| `playerbuff.command.main` | — | op |
| `playerbuff.command.set` | — | op |
| `playerbuff.command.pbs` | — | 同上（`set` の実体） |

## 効果時間管理: PlayerBuffDuration

### 設計

- **`BuffType` enum**: `HEALTH`, `DAMAGE`, `ARMOR`, `TOUGHNESS`, `SPEED` + `fromString()` で大文字小文字不問のパース
- **`DurationMode` enum**: `ADD`（残り時間に加算）, `REPLACE`（上書き） + `fromString()`
- **`applyTimedBuff()`**: 統一エントリポイント。すべてのバフ付与と時間管理をこの1メソッドで行う
- **マルチエンティティ対応**: `Map<String, BukkitTask> BUFF_TASKS` と `Map<String, Long> BUFF_EXPIRES_AT` で管理
  - キー: `UUID:BUFFTYPE` 形式

### applyTimedBuff(LivingEntity, BuffType, double, long, DurationMode)

1. entity が null または duration <= 0 なら何もしない
2. キーを生成: `UUID + ":" + BuffType.name()`
3. 現在の満了時刻を計算:
   - `REPLACE`: `now + durationSeconds * 1000`
   - `ADD`: `currentExpiresAt + durationSeconds * 1000`
4. 既存のタスクがあればキャンセル
5. 既存の同名バフがあれば削除 → 新たに付与
6. 満了時刻をマップに保存
7. `BukkitRunnable` (20 tick周期) で満了を監視:
   - エンティティ不在 → タスク・満了時刻を削除して終了
   - 満了時刻切れ → バフを削除、タスク・満了時刻を削除して終了
8. タスクをマップに保存

### イベント

**`PlayerJoinEvent`**:
- 参加1秒後 (`20L ticks`) に `clearAllBuffs()` を実行

**`clearAllBuffs(LivingEntity)`**:
- 全 BuffType をループ:
  - バフがあれば削除
  - 関連タスクをキャンセル・マップから削除
  - 満了時刻をマップから削除

### 内部ヘルパー

- `hasBuff(entity, buffType)`: BuffType に応じて適切な `PlayerBuffSet*.hasBuff*()` を呼ぶ
- `addBuff(entity, buffType, level)`: 同様に `PlayerBuffSet*.add*()` を呼ぶ
- `removeBuff(entity, buffType)`: 同様に `PlayerBuffSet*.remove*()` を呼ぶ
- `getBuffKey(UUID, BuffType)`: マップキー生成
- `getLatestEntity(UUID)`: `Bukkit.getPlayer(uuid)` でエンティティ再取得

## ワールド防具バフ: PlayerBuffTest

### 機能

- config の `worldName` ワールド内で、特定 MythicMobs 防具を着用したプレイヤーに体力バフを付与
- HW2023 と HW2022 の2セットの防具をサポート

### 使用する config キー

| キー | デフォルト値 |
|------|-------------|
| `worldName` | — |
| `Head` | `HW2023_FF_Head` |
| `Chest` | `HW2023_FF_Chest` |
| `Legs` | `HW2023_FF_Legs` |
| `Boots` | `HW2023_FF_Boots` |
| `Head22` | `HW2022_FF_Head` |
| `Chest22` | `HW2022_FF_Chest` |
| `Legs22` | `HW2022_FF_Legs` |
| `Boots22` | `HW2022_FF_Boots` |
| `health_Amount` | — |

### イベント

**`PlayerJoinEvent`**: 参加時、`temp_health_boost_number` があれば削除

**`PlayerMoveEvent`**:
- `worldName` が設定されていて現在ワールドと一致する場合のみ処理:
  1. Equipment == null → バフ削除
  2. いずれかの防具が HW2023 でない／装備していない → バフ削除
  3. 以下の条件でバフ付与（HW2023 防具を1つ装備、かつ他の3部位が HW2022 でない場合）:
     - Head のみ HW2023 装備 → バフ付与
     - Chest のみ HW2023 装備 → バフ付与
     - Legs のみ HW2023 装備 → バフ付与
     - Boots のみ HW2023 装備 → バフ付与

**`PlayerChangedWorldEvent`**: `worldName` 以外のワールドへ移動 → バフ削除

## Dream ワールド: WorldDreamTest

### 機能

- config の `worldName2` ワールド内で、特定 MythicMobs アイテムをメインハンドに持っているプレイヤーに攻撃力バフを付与

### 使用する config キー

| キー | デフォルト値 |
|------|-------------|
| `worldName2` | — |
| `MainHand` | `FFGGR_Ex` |
| `damage_Amount` | — |

### 属性

- 属性: `GENERIC_ATTACK_DAMAGE`
- 修飾子名: `"PlayerBuff23.temp_attack_damage_number"`
- オペレーション: `ADD_NUMBER`

### イベント

**`PlayerJoinEvent`**: 参加時、`temp_attack_damage_number` があれば削除

**`PlayerMoveEvent`**:
- `worldName2` かつ MainHand が MythicItem と一致する場合 → `addAttributes`（ない場合のみ）
- アイテムが一致しない場合 → バフ削除

**`PlayerChangedWorldEvent`**: `worldName2` 以外へ移動 → バフ削除

## Dream ワールド: WorldDreamSystem

### 機能

- config の `worldName2` ワールド内で常時:
  - MaxHealth を3倍 (`ADD_SCALAR 3.0`)
  - MovementSpeed を半減 (`ADD_SCALAR -0.5`)

### 属性

| 効果 | 属性 | 修飾子名 | Operation |
|------|------|---------|-----------|
| Health 3x | `GENERIC_MAX_HEALTH` | `PlayerBuff23.dream_health_scalar` | `ADD_SCALAR (3.0F)` |
| Speed 0.5x | `GENERIC_MOVEMENT_SPEED` | `PlayerBuff23.dream_speed_scalar` | `ADD_SCALAR (-0.5F)` |

### イベント

**`PlayerJoinEvent`**: 参加時、両方のバフがあれば削除

**`PlayerMoveEvent`**:
- `worldName2` 以外 → Health/Speed バフを削除
- `worldName2` かつバフがない場合 → Health 3x + Speed 0.5x を付与

**`PlayerChangedWorldEvent`**:
- `worldName2` 以外へ移動 → 該当バフを削除

## 属性操作クラス群

全5クラスとも同じ構造:

| クラス | 属性 | 修飾子名 |
|--------|------|---------|
| `PlayerBuffSetHealth` | `GENERIC_MAX_HEALTH` | `PlayerBuff23.SetHealth` |
| `PlayerBuffSetDamage` | `GENERIC_ATTACK_DAMAGE` | `PlayerBuff23.SetDamage` |
| `PlayerBuffSetArmor` | `GENERIC_ARMOR` | `PlayerBuff23.SetArmor` |
| `PlayerBuffSetArmorToughness` | `GENERIC_ARMOR_TOUGHNESS` | `PlayerBuff23.SetArmor_Toughness` |
| `PlayerBuffSetSpeed` | `GENERIC_MOVEMENT_SPEED` | `PlayerBuff23.SetSpeed` |

各クラスに3つの static メソッド:
- `hasBuff*(LivingEntity)`: 修飾子名で一致する AttributeModifier が存在するか判定
- `add*(LivingEntity, double)`: `ADD_NUMBER` オペレーションで属性値を加算
- `remove*(LivingEntity)`: `new ArrayList<>(attr.getModifiers())` でコピーを取ってから一致する全 AttributeModifier を除去（ConcurrentModificationException 対策）

`attr != null` の場合のみ処理。

## config.yml

```yaml
health_Amount: 40
worldName: "free_field"
Head: "HW2023_FF_Head"
Chest: "HW2023_FF_Chest"
Legs: "HW2023_FF_Legs"
Boots: "HW2023_FF_Boots"
Head22: "HW2022_FF_Head"
Chest22: "HW2022_FF_Chest"
Legs22: "HW2022_FF_Legs"
Boots22: "HW2022_FF_Boots"

damage_Amount: 120
worldName2: "dream"
MainHand: "FFGGR_Ex"
```

## plugin.yml

```yaml
name: PlayerBuff23
version: '${version}'
main: net.azisaba.playerbuff23.PlayerBuff23
api-version: '1.15'
authors: [ Azisaba Network ]

commands:
  playerbuff:
    description: PlayerBuff23 main command
    permission: playerbuff.command.main
  playerbuffset:
    description: PlayerBuff set command
    aliases:
      - pbset
      - pbs
    permission: playerbuff.command.set

permissions:
  playerbuff.command.*:
    description: Permission to use PlayerBuff23 commands
    children:
      playerbuff.command.main: true
      playerbuff.command.set: true
    default: op
  playerbuff.command.main:
    description: Permission to use PlayerBuff23 main command
    default: op
  playerbuff.command.set:
    description: Permission to set PlayerBuff23
    default: op
```

## playerbuff (azuazu3939) との主要な差異

| 項目 | PlayerBuff (azuazu3939) | PlayerBuff23 (net.azisaba) |
|------|------------------------|---------------------------|
| パッケージ | `azuazu3939.playerbuff` | `net.azisaba.playerbuff23` |
| メインクラス | `PlayerBuff` | `PlayerBuff23` |
| インスタンス化 | コンストラクタで static 設定 | `onEnable` で static 設定 |
| コマンド注入 | `Objects.requireNonNull(getServer().getPluginCommand(...))` | `this.getCommand(...)` |
| 引数パース | try-catch 全体 | try-catch 数値のみ |
| BuffType 判定 | 大文字小文字を個別比較 | `fromString()` で統一 |
| TabCompleter | なし | あり（プレイヤー名/BuffType/mode） |
| DurationMode | なし（常に replace） | ADD / REPLACE |
| 時間管理 | `scheduleSyncDelayedTask` 単発 | `runTaskTimer` ポーリング + 満了時刻管理 |
| 複数エンティティ | 不可（static フィールドで1件のみ） | 可（UUID:BUFFTYPE キーで管理） |
| onJoin 時 | バフ削除判定のみ | 1秒後に全バフクリア |
| onQuit 時 | 全バフ削除 | なし |
| 防具セット | HW2022 のみ | HW2022 + HW2023 |
| DreamWorld | なし | あり（攻撃力 + 体力倍率 + 速度減衰） |
| メッセージ言語 | 日本語 | 英語 + 日本語 |
| Permission 名 | `reload`, `pbs` | `main`, `set`（子ノード `pbs`） |
| config 保存 | `saveConfig()` も別途呼ぶ | `saveDefaultConfig()` のみ |
| `reload()` | 独自メソッド | `plugin.reloadConfig()` 直接呼び |

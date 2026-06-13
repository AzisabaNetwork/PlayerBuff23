# PlayerBuff (azuazu3939.playerbuff) 仕様書

## 概要

- **パッケージ**: `azuazu3939.playerbuff`
- **メインクラス**: `PlayerBuff` (`org.bukkit.plugin.java.JavaPlugin` を継承)
- **プラグイン名**: 独自の `plugin.yml` を持たない（後述の統合対象）
- **APIバージョン**: Paper 1.15.2 (ビルド依存)
- **外部依存**: MythicMobs (4.12.0)

## アーキテクチャ

- **Static Singleton パターン**: メインクラス `PlayerBuff` はコンストラクタで `instance` を設定。
  `PlayerBuff.getInstance()` でどこからでもアクセス可能。
- **Stateful Command**: `PlayerBuffSetCommand` が static シングルトンとして状態（`type`, `level`, `duration`, `target`）を保持。
  コマンド実行時にこれらのフィールドに値をセットし、`PlayerBuffDuration` のメソッドがそれを参照する。

## クラス一覧

| クラス | 役割 |
|--------|------|
| `PlayerBuff` | メインクラス |
| `PlayerBuffCommand` | `/playerbuff` コマンド |
| `PlayerBuffSetCommand` | `/pbs` コマンド + 状態保持 |
| `PlayerBuffDuration` | 効果時間管理 + ログアウト時クリア |
| `PlayerBuffTest` | ワールド限定防具判定 + 体力バフ |
| `PlayerBuffSetHealth` | MaxHealth 属性操作 |
| `PlayerBuffSetDamage` | AttackDamage 属性操作 |
| `PlayerBuffSetArmor` | Armor 属性操作 |
| `PlayerBuffSetArmorToughness` | ArmorToughness 属性操作 |
| `PlayerBuffSetSpeed` | MovementSpeed 属性操作 |

## メインクラス: PlayerBuff

### フィールド

- `private static PlayerBuff instance` — static singleton

### onEnable()

1. `instance = this` (コンストラクタで既に設定済み)
2. `/playerbuff` コマンドに `PlayerBuffCommand` をセット
3. `/pbs` コマンドに `PlayerBuffSetCommand` をセット
4. `PlayerBuffTest` をイベントリスナーとして登録
5. `saveDefaultConfig()` — config.yml を初回生成
6. `saveConfig()` — 明示的に保存

### onDisable()

- 何もしない

### reload()

- `saveDefaultConfig()` + `reloadConfig()` を呼ぶ

## コマンド

### `/playerbuff`

| 項目 | 内容 |
|------|------|
| **Executor** | `PlayerBuffCommand` |
| **パーミッション** | `playerbuff.command.reload` |
| **引数** | なし（常にリロード実行） |
| **動作** | `PlayerBuff.getInstance().reload()` を呼び、`"コンフィグをリロードしました。"` と送信 |
| **備考** | 権限がない場合 `return false` を返す |

### `/pbs` (setter)

| 項目 | 内容 |
|------|------|
| **Executor** | `PlayerBuffSetCommand` (シングルトン、状態保持) |
| **パーミッション** | `playerbuff.command.pbs` |
| **構文** | `/pbs <PlayerName> <BuffType> <level> <duration>` |
| **BuffType** | `Health`, `Damage`, `Armor`, `Toughness`, `Speed` (大文字小文字不問) |
| **level** | double 値 |
| **duration** | long 値（秒） |
| **TabCompleter** | なし |

**状態フィールド**（コマンド実行後に設定）:
- `String type` — BuffType (case-preserved from input)
- `double level` — バフ量
- `long duration` — 効果時間（秒）
- `LivingEntity target` — 対象エンティティ
- `String string` — プレイヤー名（生の入力）

**動作**:
1. 権限チェック (`playerbuff.command.pbs`)
2. 引数をパース: `args[0]=PlayerName`, `args[1]=BuffType`, `args[2]=level`, `args[3]=duration`
3. パースエラー時はヘルプメッセージ（日本語）を表示
4. 対象プレイヤーがオフラインならメッセージを表示して終了
5. BuffType に応じて対応する `PlayerBuffDuration.PlayerBuffSetDurationStart*` と `PlayerBuffDuration.PlayerBuffSetDuration*` を順次呼ぶ

**ヘルプメッセージ**:
```
正しく入力しよう！ /pbs <PlayerName> <BuffType> <level> <duration>
PlayerNameはオンラインプレイヤーのみ。
BuffTypeは(Health, Damage, Armor, Toughness, Speed)の内から。
durationは秒数。ログアウトすると効果がはがれるよ。
注意、Speedは0.2がAZISAVIOR(100%SpeedUp)と同等。
```

## 効果時間管理: PlayerBuffDuration

### 設計

- 各 BuffType ごとに独立した static メソッドを持つ
- シングルターゲット: `PlayerBuffSetCommand.getInstance().target` と `PlayerBuffSetCommand.getInstance().level` を参照
- `Bukkit.getScheduler().scheduleSyncDelayedTask` を使った単発遅延

### メソッド群

**`PlayerBuffSetDuration*Health/Damage/Armor/Toughness/Speed(long duration)`**:
- `duration` 秒後に該当バフを剥がす（`duration * 20L` ticks の遅延タスク）
- 既存のバフがある場合のみ `remove*Attributes` を実行

**`PlayerBuffSetDurationStart*Health/Damage/Armor/Toughness/Speed(LivingEntity entity)`**:
- 対象にすでに同名バフがある場合は何もしない
- なければ `PlayerBuffSetCommand.getInstance().level` の値でバフを付与

### イベント

- **`PlayerQuitEvent`**: ログアウト時に全バフ（Health, Damage, Armor, ArmorToughness, Speed）を剥がす
  - 各バフの `hasBuff*` で確認後、`remove*Attributes` で削除

### 注意点

- バフの重ねがけ防止は `PlayerBuffSetDurationStart*` の中でのみ行われ、コマンド側で制御
- 時間経過による剥がしは一度きり（複数回の duration に対応していない）

## ワールド防具バフ: PlayerBuffTest

### 機能

- config の `worldName` で指定されたワールド内で、特定の MythicMobs 防具を着用しているプレイヤーに体力バフを付与

### 使用する config キー

| キー | デフォルト値 |
|------|-------------|
| `worldName` | —（設定必須） |
| `Head` | `HW2022_FF_Head` |
| `Chest` | `HW2022_FF_Chest` |
| `Legs` | `HW2022_FF_Legs` |
| `Boots` | `HW2022_FF_Boots` |
| `health_Amount` | —（設定必須） |

### イベント

**`PlayerJoinEvent`**:
- 参加時に `temp_health_boost_number` バフがあれば削除

**`PlayerMoveEvent`**:
1. `worldName` と一致しないワールドなら何もしない（早期 return）
2. Equipment が null ならバフ削除
3. HW2022 防具を **すべて同時に装備していない**（いずれかが非類似）場合、バフ削除
4. 上記チェックを通過し、まだバフがない場合、部位ごとに以下を判定:
   - `mythicItem.isSimilar(itemStack)` が真 → `addAttributes`（バフを付与）

**`PlayerChangedWorldEvent`**:
- `worldName` 以外のワールドに移動した場合、バフがあれば削除

## 属性操作クラス群

全5クラスとも同じ構造:

| クラス | 属性 | 修飾子名 |
|--------|------|---------|
| `PlayerBuffSetHealth` | `GENERIC_MAX_HEALTH` | `PlayerBuff.SetHealth` |
| `PlayerBuffSetDamage` | `GENERIC_ATTACK_DAMAGE` | `PlayerBuff.SetDamage` |
| `PlayerBuffSetArmor` | `GENERIC_ARMOR` | `PlayerBuff.SetArmor` |
| `PlayerBuffSetArmorToughness` | `GENERIC_ARMOR_TOUGHNESS` | `PlayerBuff.SetArmor_Toughness` |
| `PlayerBuffSetSpeed` | `GENERIC_MOVEMENT_SPEED` | `PlayerBuff.SetSpeed` |

各クラスに3つの static メソッド:
- `hasBuff*(LivingEntity)`: 修飾子名で一致する AttributeModifier が存在するか判定
- `add*(LivingEntity, double)`: `ADD_NUMBER` オペレーションで属性値を加算
- `remove*(LivingEntity)`: 一致する全 AttributeModifier を除去

すべてのメソッドで `attr == null` の場合は早期 return / return false。

## config.yml

```
health_Amount: <double>        # PlayerBuffTest で使う体力加算値
worldName: "<world name>"      # 防具バフが有効なワールド名
Head: "HW2022_FF_Head"         # MythicMobs 防具ID（デフォルト）
Chest: "HW2022_FF_Chest"
Legs: "HW2022_FF_Legs"
Boots: "HW2022_FF_Boots"
```

## パーミッション

| ノード | 用途 |
|--------|------|
| `playerbuff.command.reload` | `/playerbuff` コマンドの実行 |
| `playerbuff.command.pbs` | `/pbs` コマンドの実行 |
